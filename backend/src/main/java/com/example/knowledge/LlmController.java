package com.example.knowledge;

import com.example.knowledge.service.LlmService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author nageshasriramappa
 **/
@RestController
public class LlmController {

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    public record ChatRequest(String message) {}
    public record ChatReply(String answer) {}

    @PostMapping("/api/chat/complete")
    public Mono<ChatReply> chatReplyMono(@RequestBody ChatRequest request) {
        return llmService.callWithRetries(List.of(new UserMessage(request.message()))).map(ChatReply::new);
    }
}
