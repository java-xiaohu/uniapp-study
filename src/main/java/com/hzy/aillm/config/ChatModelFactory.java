package com.hzy.aillm.config;

import com.anthropic.client.AnthropicClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.AnthropicSetup;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 按配置动态构建各厂商的 ChatModel。
 * <p>Spring AI 的自动装配只支持每种协议一个实例，这里手工构建以支持同一协议下多个厂商/多套密钥。
 */
@Component
@Slf4j
public class ChatModelFactory {

    private static final String DEFAULT_ANTHROPIC_URL = "https://api.anthropic.com";
    private static final String DEFAULT_OLLAMA_URL = "http://localhost:11434";

    public ChatModel create(String modelId, AiProperties.ModelSpec spec) {
        ChatModel chatModel = switch (spec.getProvider()) {
            case OPENAI -> createOpenAi(spec);
            case ANTHROPIC -> createAnthropic(spec);
            case OLLAMA -> createOllama(spec);
        };
        log.info("已注册模型 [{}] provider={} model={} baseUrl={}",
                modelId, spec.getProvider(), spec.getModel(), spec.getBaseUrl());
        return chatModel;
    }

    /**
     * OpenAI 兼容协议：OpenAI、DeepSeek、通义千问、Kimi、智谱、vLLM、OneAPI 等。
     */
    private ChatModel createOpenAi(AiProperties.ModelSpec spec) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder();
        options.model(spec.getModel());
        options.temperature(spec.getTemperature());
        if (StringUtils.hasText(spec.getBaseUrl())) {
            options.baseUrl(spec.getBaseUrl());
        }
        if (StringUtils.hasText(spec.getApiKey())) {
            options.apiKey(spec.getApiKey());
        }
        if (spec.getMaxTokens() != null) {
            options.maxTokens(spec.getMaxTokens());
        }
        if (spec.getMaxRetries() != null) {
            options.maxRetries(spec.getMaxRetries());
        }
        if (spec.getTimeout() != null) {
            options.timeout(spec.getTimeout());
        }
        // 只传 options，builder 会依据 baseUrl/apiKey 自行创建底层客户端
        return OpenAiChatModel.builder().options(options.build()).build();
    }

    private ChatModel createAnthropic(AiProperties.ModelSpec spec) {
        String baseUrl = StringUtils.hasText(spec.getBaseUrl()) ? spec.getBaseUrl() : DEFAULT_ANTHROPIC_URL;
        AnthropicChatOptions.Builder options = AnthropicChatOptions.builder();
        options.model(spec.getModel());
        options.temperature(spec.getTemperature());
        options.baseUrl(baseUrl);
        options.apiKey(spec.getApiKey());
        if (spec.getMaxTokens() != null) {
            options.maxTokens(spec.getMaxTokens());
        }
        if (spec.getMaxRetries() != null) {
            options.maxRetries(spec.getMaxRetries());
        }
        if (spec.getTimeout() != null) {
            options.timeout(spec.getTimeout());
        }
        AnthropicClient client = AnthropicSetup.setupSyncClient(
                baseUrl, spec.getApiKey(), spec.getTimeout(), spec.getMaxRetries(), null, Map.of());
        return AnthropicChatModel.builder().anthropicClient(client).options(options.build()).build();
    }

    private ChatModel createOllama(AiProperties.ModelSpec spec) {
        String baseUrl = StringUtils.hasText(spec.getBaseUrl()) ? spec.getBaseUrl() : DEFAULT_OLLAMA_URL;
        OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();
        OllamaChatOptions.Builder options = OllamaChatOptions.builder();
        // model(String) 来自父类 Builder，返回类型为父类，故单独调用不再链式
        options.model(spec.getModel());
        options.temperature(spec.getTemperature());
        if (spec.getMaxTokens() != null) {
            options.numPredict(spec.getMaxTokens());
        }
        // 不指定时 Spring AI 会使用 10 次重试 + 1s 退避的默认策略，故障时会长时间挂起
        long maxRetries = spec.getMaxRetries() == null ? 1 : Math.max(spec.getMaxRetries(), 0);
        RetryTemplate retryTemplate = new RetryTemplate(RetryPolicy.withMaxRetries(maxRetries));
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .options(options.build())
                .retryTemplate(retryTemplate)
                .build();
    }
}
