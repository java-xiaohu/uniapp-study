package com.hzy.aillm.model;

import org.springframework.ai.chat.metadata.Usage;

/**
 * 流式输出片段。done=true 的片段不再携带内容，只回传 token 统计。
 */
public record ChatChunk(String model,
                        String content,
                        boolean done,
                        Integer promptTokens,
                        Integer completionTokens,
                        Integer totalTokens) {

    public static ChatChunk content(String model, String content) {
        return new ChatChunk(model, content, false, null, null, null);
    }

    public static ChatChunk done(String model, Usage usage) {
        if (usage == null) {
            return new ChatChunk(model, "", true, null, null, null);
        }
        return new ChatChunk(model, "", true,
                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
