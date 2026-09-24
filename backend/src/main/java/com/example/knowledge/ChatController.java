package com.example.knowledge;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nageshasriramappa
 **/
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant. Answer briefly.")
                .build();

    }

    @PostMapping("/api/chat")
    public ChatReply chat(@RequestBody ChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.question())
                .call()
                .content();
        return new ChatReply(answer);
    }

    public record ChatRequest(String question) {}

    public record ChatReply(String answer) {}
}
