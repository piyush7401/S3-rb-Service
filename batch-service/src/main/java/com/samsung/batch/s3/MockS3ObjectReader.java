package com.samsung.batch.s3;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(
        name = "app.s3.mode",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockS3ObjectReader implements S3ObjectReader {
    @Override
    public Mono<String> readObject(
            String bucketName,
            String objectKey
    ) {
        String mockJson = """
                {
                  "providerId": "provider-101",
                  "name": "Mock Gaming Provider",
                  "games": [
                    {
                      "id": "game-1",
                      "title": "Racing Game"
                    },
                    {
                      "id": "game-2",
                      "title": "Puzzle Game"
                    }
                  ]
                }
                """;

        return Mono.just(mockJson);
    }
}
