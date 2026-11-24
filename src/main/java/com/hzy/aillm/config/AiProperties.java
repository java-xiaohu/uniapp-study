package com.hzy.aillm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模型接入配置。key 为对外暴露的模型标识（业务侧只关心这个 id，不关心具体厂商）。
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 默认模型标识，请求未指定 model 时使用 */
    private String defaultModel;

    /** 会话记忆保留的消息条数 */
    private int maxHistoryMessages = 20;

    /** 模型清单 */
    private Map<String, ModelSpec> models = new LinkedHashMap<>();

    @Data
    public static class ModelSpec {

        /** 协议类型，决定用哪个 Spring AI 模块构建客户端 */
        private Provider provider = Provider.OPENAI;

        /** 服务地址；OpenAI 兼容服务一般需要带 /v1 */
        private String baseUrl;

        /** 密钥；除 OLLAMA 外为空则跳过该模型 */
        private String apiKey;

        /** 服务商侧的模型名，如 qwen-plus、deepseek-chat、claude-sonnet-4-6 */
        private String model;

        /** 模型级系统提示词 */
        private String systemPrompt;

        /** 采样温度 */
        private Double temperature = 0.7d;

        /** 最大输出 token 数 */
        private Integer maxTokens;

        /** 单次请求超时时间 */
        private Duration timeout = Duration.ofSeconds(60);

        /** 失败重试次数（SDK 层） */
        private Integer maxRetries = 2;

        /** 是否启用 */
        private boolean enabled = true;

        /** 主模型失败后依次尝试的模型标识 */
        private List<String> fallback = new ArrayList<>();
    }

    public enum Provider {
        /** OpenAI 协议，覆盖 OpenAI / DeepSeek / 通义 / Kimi / 智谱 / vLLM 等 */
        OPENAI,
        /** Anthropic Claude 专有协议 */
        ANTHROPIC,
        /** 本地 Ollama */
        OLLAMA
    }
}
