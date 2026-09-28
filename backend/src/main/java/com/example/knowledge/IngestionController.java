package com.example.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.print.DocFlavor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author nageshasriramappa
 **/
@RestController
public class IngestionController {

    private static final List<String> SOURCES = List.of(
            "leave-policy.md",
            "remote-work-policy.md");

    private final VectorStore vectorStore;

    public IngestionController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/api/documents/ingest-sample")
    public IngestionReply ingestSample() throws IOException {

        List<Document> chunks = new ArrayList<>();

        for (String source : SOURCES) {
            chunks.addAll(readChunks(source));
        }
        vectorStore.add(chunks);

        return new IngestionReply(SOURCES, chunks.size());
    }

    private List<Document> readChunks(String source) throws IOException {
        String text = new ClassPathResource("documents/" + source)
                .getContentAsString(StandardCharsets.UTF_8);

        String[] sections = text.split("(?m)(?=^## )");

        if (sections.length !=3) {
            throw new IllegalStateException(
                    "Expected an introduction and exactly two ## sections in "
                    + source);
        }

        String introduction = sections[0].strip();
        List<Document> chunks = new ArrayList<>();

        for (int i=1; i < sections.length; i++) {
            String section = sections[i].strip();

            String heading = section.lines()
                    .findFirst()
                    .orElseThrow()
                    .substring(3)
                    .strip();

            String id = UUID.nameUUIDFromBytes(
                    ("sample:" + source + ":" + i)
                            .getBytes(StandardCharsets.UTF_8))
                    .toString();

            Document chuck = Document.builder()
                    .id(id)
                    .text(introduction + "\n\n" + section)
                    .metadata("source", source)
                    .metadata("chunk_number", i)
                    .metadata("section", heading)
                    .build();
            chunks.add(chuck);
        }

        return chunks;
    }

    public record IngestionReply(List<String> source, int chunksWritten) {}
}
