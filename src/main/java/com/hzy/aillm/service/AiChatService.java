package com.hzy.aillm.service;

import com.hzy.aillm.exception.AiCallException;
import com.hzy.aillm.model.ChatChunk;
import com.hzy.aillm.model.ChatRequest;
import com.hzy.aillm.model.ChatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一对话服务：屏蔽厂商差异，提供同步/流式调用与降级能力。
 */
@Service
@Slf4j
public class AiChatService {

    private final AiModelRegistry registry;
    private final Advisor memoryAdvisor;

    public AiChatService(AiModelRegistry registry, ChatMemory chatMemory) {
        this.registry = registry;
        this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    /**
     * 同步对话，按配置的降级链依次尝试。
     */
    public ChatResult chat(ChatRequest request) {
        validate(request);
        List<String> chain = registry.resolveChain(request.model());
        RuntimeException lastError = null;
        for (int i = 0; i < chain.size(); i++) {
            String modelId = chain.get(i);
            long start = System.nanoTime();
            try {
                ChatResponse response = buildSpec(modelId, request).call().chatResponse();
                ChatResult result = toResult(modelId, response, elapsedMillis(start), i > 0);
                log.info("模型 [{}] 调用成功，耗时 {} ms", modelId, result.latencyMs());
                return result;
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("模型 [{}] 调用失败，尝试降级：{}", modelId, e.getMessage());
            }
        }
        throw new AiCallException("所有模型调用失败：" + (lastError == null ? "unknown" : lastError.getMessage()), lastError);
    }

    /**
     * 流式对话。降级仅在第一块内容产出前生效，避免同一回答混入两个模型的内容。
     */
    public Flux<ChatChunk> stream(ChatRequest request) {
        validate(request);
        List<String> chain = registry.resolveChain(request.model());
        return attemptStream(chain, 0, request, new AtomicBoolean(false));
    }

    private Flux<ChatChunk> attemptStream(List<String> chain, int index, ChatRequest request, AtomicBoolean emitted) {
        if (index >= chain.size()) {
            return Flux.error(new AiCallException("所有模型调用失败"));
        }
        String modelId = chain.get(index);
        AtomicReference<Usage> usage = new AtomicReference<>();
        return buildSpec(modelId, request).stream().chatResponse()
                .doOnNext(response -> {
                    emitted.set(true);
                    usage.set(extractUsage(response));
                })
                .map(response -> ChatChunk.content(modelId, textOf(response)))
                .concatWith(Mono.fromSupplier(() -> ChatChunk.done(modelId, usage.get())))
                .onErrorResume(e -> {
                    log.warn("模型 [{}] 流式调用失败：{}", modelId, e.getMessage());
                    if (emitted.get()) {
                        return Flux.error(e);
                    }
                    return attemptStream(chain, index + 1, request, emitted);
                });
    }

    private ChatClient.ChatClientRequestSpec buildSpec(String modelId, ChatRequest request) {
        ChatClient client = registry.chatClient(modelId);
        ChatClient.ChatClientRequestSpec spec = client.prompt();
        if (StringUtils.hasText(request.systemPrompt())) {
            spec = spec.system(request.systemPrompt());
        }
        spec = spec.user(request.message());
        if (StringUtils.hasText(request.conversationId())) {
            String conversationId = request.conversationId();
            spec = spec.advisors(a -> a.advisors(memoryAdvisor).param(ChatMemory.CONVERSATION_ID, conversationId));
        }
        return spec;
    }

    private static void validate(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.message())) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    private static ChatResult toResult(String modelId, ChatResponse response, long latencyMs, boolean fallbackUsed) {
        Usage usage = extractUsage(response);
        return new ChatResult(
                modelId,
                textOf(response),
                fallbackUsed,
                latencyMs,
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                usage == null ? null : usage.getTotalTokens());
    }

    private static Usage extractUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return null;
        }
        return response.getMetadata().getUsage();
    }

    private static String textOf(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
