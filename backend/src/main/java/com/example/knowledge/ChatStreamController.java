package com.example.knowledge;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.print.DocFlavor;

/**
 * @author nageshasriramappa
 **/
@RestController
public class ChatStreamController {

    private final ChatClient chatClient;

    public ChatStreamController(ChatClient.Builder builder, TextToSpeechModel textToSpeechModel) {
        this.chatClient = builder.build();
    }

    public record Chunk(String text) {}

    @GetMapping(
            value = "/api/chat-stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Chunk>> chat(
            @RequestParam("message") String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .map(text -> event("delta", text))
                .concatWithValues(event("done", ""))
                .onErrorResume(error ->
                        Flux.just(event(
                                "failure",
                                "Unable to complete the response. Please try again."
                        ))
                );
    }

    private ServerSentEvent<Chunk> event(String name, String text) {
        return ServerSentEvent.<Chunk>builder()
                .event(name)
                .data(new Chunk(text))
                .build();
    }

}
