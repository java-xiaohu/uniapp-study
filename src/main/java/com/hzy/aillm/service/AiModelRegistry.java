package com.hzy.aillm.service;

import com.hzy.aillm.config.AiProperties;
import com.hzy.aillm.config.ChatModelFactory;
import com.hzy.aillm.model.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型注册表：启动时把配置里的每个模型构建成 ChatClient，并按 id 对外提供。
 */
@Component
@Slf4j
public class AiModelRegistry {

    private final Map<String, Registration> registry = new LinkedHashMap<>();
    private final String defaultModelId;

    public AiModelRegistry(AiProperties properties, ChatModelFactory factory) {
        properties.getModels().forEach((id, spec) -> {
            if (!spec.isEnabled()) {
                log.info("模型 [{}] 已禁用，跳过注册", id);
                return;
            }
            if (!StringUtils.hasText(spec.getModel())) {
                log.warn("模型 [{}] 未配置 model，跳过注册", id);
                return;
            }
            if (spec.getProvider() != AiProperties.Provider.OLLAMA && !StringUtils.hasText(spec.getApiKey())) {
                log.warn("模型 [{}] 未配置 api-key，跳过注册", id);
                return;
            }
            try {
                ChatModel chatModel = factory.create(id, spec);
                registry.put(id, new Registration(id, spec, toChatClient(chatModel, spec)));
            } catch (RuntimeException e) {
                log.error("模型 [{}] 构建失败，已忽略：{}", id, e.getMessage(), e);
            }
        });

        String configured = properties.getDefaultModel();
        if (StringUtils.hasText(configured) && registry.containsKey(configured)) {
            this.defaultModelId = configured;
        } else {
            this.defaultModelId = registry.keySet().stream().findFirst().orElse(null);
            if (this.defaultModelId != null) {
                log.warn("默认模型 [{}] 未注册，回退为 [{}]", configured, this.defaultModelId);
            }
        }
        log.info("多模型注册完成，可用模型={}，默认模型={}", registry.keySet(), this.defaultModelId);
    }

    private static ChatClient toChatClient(ChatModel chatModel, AiProperties.ModelSpec spec) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        if (StringUtils.hasText(spec.getSystemPrompt())) {
            builder.defaultSystem(spec.getSystemPrompt());
        }
        return builder.build();
    }

    public ChatClient chatClient(String modelId) {
        Registration registration = registry.get(modelId);
        if (registration == null) {
            throw new IllegalArgumentException("未知模型: " + modelId + "，可选: " + registry.keySet());
        }
        return registration.client();
    }

    /**
     * 解析调用链：主模型 + 配置的降级模型（只展开一层，避免循环依赖）。
     */
    public List<String> resolveChain(String requestedModel) {
        String head = StringUtils.hasText(requestedModel) ? requestedModel : defaultModelId;
        if (head == null) {
            throw new IllegalStateException("没有可用模型，请检查 ai.models 配置");
        }
        Registration registration = registry.get(head);
        if (registration == null) {
            throw new IllegalArgumentException("未知模型: " + head + "，可选: " + registry.keySet());
        }
        List<String> chain = new ArrayList<>();
        chain.add(head);
        for (String fallback : registration.spec().getFallback()) {
            if (registry.containsKey(fallback) && !chain.contains(fallback)) {
                chain.add(fallback);
            }
        }
        return chain;
    }

    public List<ModelInfo> infos() {
        return registry.values().stream()
                .map(r -> new ModelInfo(
                        r.id(),
                        r.spec().getProvider().name(),
                        r.spec().getModel(),
                        r.spec().getBaseUrl(),
                        r.id().equals(defaultModelId),
                        List.copyOf(r.spec().getFallback())))
                .toList();
    }

    public String defaultModelId() {
        return defaultModelId;
    }

    private record Registration(String id, AiProperties.ModelSpec spec, ChatClient client) {
    }
}
