package com.hzy.aillm.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    /**
     * 会话记忆：默认进程内窗口记忆，多实例部署时可换成基于 Redis/JDBC 的 ChatMemoryRepository。
     */
    @Bean
    public ChatMemory chatMemory(AiProperties properties) {
        int maxMessages = Math.max(properties.getMaxHistoryMessages(), 2);
        return MessageWindowChatMemory.builder().maxMessages(maxMessages).build();
    }
}
