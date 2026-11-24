package com.hzy.aillm.model;

/**
 * 统一对话入参。业务只依赖模型标识，不感知厂商。
 *
 * @param model          模型标识，为空时使用默认模型
 * @param message        用户输入
 * @param systemPrompt   本次请求的系统提示词，会覆盖模型级配置
 * @param conversationId 会话标识，传入后启用多轮记忆
 */
public record ChatRequest(String model, String message, String systemPrompt, String conversationId) {
}
