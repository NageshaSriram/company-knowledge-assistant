package com.example.knowledge.service;

import com.openai.errors.OpenAIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author nageshasriramappa
 **/
@Service
public class LlmService {
    private static final Logger log =
            LoggerFactory.getLogger(LlmService.class);

    private static final int MAX_ATTEMPTS = 5;

    private final ChatClient chatClient;

    public LlmService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Mono<String> callWithRetries(List<Message> messages) {
        List<Message> requestMessages = List.copyOf(messages);

        return Mono.fromCallable(() ->
                Objects.requireNonNull(
                        chatClient.prompt()
                                .messages(requestMessages)
                                .call()
                                .content(),
                        "The model returned no text"
                )
        ).subscribeOn(Schedulers.boundedElastic())
                .retryWhen(
                        Retry.backoff(
                                MAX_ATTEMPTS - 1,
                                Duration.ofSeconds(1)
                        )
                                .maxBackoff(Duration.ofSeconds(10))
                                .jitter(0.3)
                                .filter(this::isRetryable)
                                .doBeforeRetry(retrySignal ->
                                        log.warn(
                                                "Retrying LLM call: retry {}/{}",
                                                retrySignal.totalRetries() + 1,
                                                MAX_ATTEMPTS - 1
                                        )
                                )
                                .onRetryExhaustedThrow(
                                        (policy, signal) -> signal.failure()
                                )
                );
    }

    private boolean isRetryable(Throwable error) {
        // Inspect causes too, in case another layer wraps the exception.

        for ( Throwable cause = error; cause != null; cause = cause.getCause()) {
            if ( cause instanceof OpenAIServiceException apiError) {
                int status = apiError.statusCode();

                return status == 429 || (status >= 500 && status < 600);
            }
        }

        return false;
    }
}
