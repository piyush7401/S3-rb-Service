package com.samsung.batch.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.model.S3ObjectReference;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class S3EventParser {

    private final ObjectMapper objectMapper;

    public S3EventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<S3ObjectReference> parse(String message)
            throws IOException {

        JsonNode root = objectMapper.readTree(message);
        JsonNode records = root.path("Records");

        if (!records.isArray()) {
            throw new IllegalArgumentException(
                    "Invalid S3 event: Records array not found"
            );
        }

        List<S3ObjectReference> references = new ArrayList<>();

        for (JsonNode record : records) {
            String eventName = record.path("eventName").asText("");

            // Ignore events other than object creation.
            if (!eventName.startsWith("ObjectCreated:")) {
                continue;
            }

            JsonNode s3 = record.path("s3");
            String bucketName =
                    s3.path("bucket").path("name").asText("");

            String encodedKey =
                    s3.path("object").path("key").asText("");

            if (bucketName.isBlank() || encodedKey.isBlank()) {
                throw new IllegalArgumentException(
                        "S3 event is missing bucket name or object key"
                );
            }

            String objectKey = URLDecoder.decode(
                    encodedKey.replace("+", "%2B"),
                    StandardCharsets.UTF_8
            );

            references.add(
                    new S3ObjectReference(bucketName, objectKey)
            );
        }

        return references;
    }
}
