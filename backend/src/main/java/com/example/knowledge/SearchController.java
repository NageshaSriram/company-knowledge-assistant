package com.example.knowledge;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * @author nageshasriramappa
 **/
@RestController
public class SearchController {

    private final VectorStore vectorStore;

    public SearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/api/search")
    public List<SearchHit> searchHits(@RequestBody SearchQuery query) {
        if (query.question() == null || query.question().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "question must not be blank");
        }

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query.question())
                .topK(3)
                .similarityThreshold(0.0)
                .build();
        return vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(document -> new SearchHit(
                        document.getMetadata()
                                .getOrDefault("source", "unknown")
                                .toString(),
                        document.getScore(),
                        document.getText()
                )).toList();
    }

    public record SearchQuery(String question) {}

    public record SearchHit(String source, Double score, String text) {}
}
