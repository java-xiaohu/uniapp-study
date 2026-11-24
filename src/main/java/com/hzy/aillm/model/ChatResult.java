package com.hzy.aillm.model;

/**
 * 统一对话返回。
 *
 * @param model            实际使用的模型标识
 * @param content          模型输出
 * @param fallbackUsed     是否由降级模型返回
 * @param latencyMs        耗时（毫秒）
 * @param promptTokens     输入 token
 * @param completionTokens 输出 token
 * @param totalTokens      总 token
 */
public record ChatResult(String model,
                         String content,
                         boolean fallbackUsed,
                         long latencyMs,
                         Integer promptTokens,
                         Integer completionTokens,
                         Integer totalTokens) {
}
