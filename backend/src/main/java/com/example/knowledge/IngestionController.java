package com.example.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.print.DocFlavor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * @author nageshasriramappa
 **/
@RestController
public class IngestionController {

    private static final String SOURCE = "leave-policy.md";

    private final VectorStore vectorStore;

    public IngestionController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/api/documents/ingest-sample")
    public IngestionReply ingestSample() throws IOException {
        String text = new ClassPathResource("documents/" + SOURCE)
                .getContentAsString(StandardCharsets.UTF_8);

        String id = UUID.nameUUIDFromBytes(
                ("sample:" + SOURCE + ":1")
                        .getBytes(StandardCharsets.UTF_8))
                .toString();

        Document document = Document.builder()
                .id(id)
                .text(text)
                .metadata("source", SOURCE)
                .metadata("chunk_number", 1)
                .build();

        vectorStore.add(List.of(document));

        return new IngestionReply(SOURCE, 1);
    }

    public record IngestionReply(String source, int chunksWritten) {}
}
