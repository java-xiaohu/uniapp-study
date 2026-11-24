package com.hzy.aillm.controller;

import com.hzy.aillm.model.ChatChunk;
import com.hzy.aillm.model.ChatRequest;
import com.hzy.aillm.model.ChatResult;
import com.hzy.aillm.model.ModelInfo;
import com.hzy.aillm.service.AiChatService;
import com.hzy.aillm.service.AiModelRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService chatService;
    private final AiModelRegistry registry;

    /** 同步对话 */
    @PostMapping("/chat")
    public ChatResult chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    /** 流式对话（SSE），event=message 为内容块，event=done 为结束块（含 token 统计） */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatChunk>> stream(@RequestBody ChatRequest request) {
        return chatService.stream(request)
                .map(chunk -> ServerSentEvent.<ChatChunk>builder()
                        .event(chunk.done() ? "done" : "message")
                        .data(chunk)
                        .build());
    }

    /** 已注册的模型清单 */
    @GetMapping("/models")
    public List<ModelInfo> models() {
        return registry.infos();
    }
}
