package com.samsung.batch.s3;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(
        name = "app.s3.mode",
        havingValue = "aws"
)
public class AwsS3ObjectReader implements S3ObjectReader {

    private final S3Client s3Client;

    public AwsS3ObjectReader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public Mono<String> readObject(
            String bucketName,
            String objectKey
    ) {
        return Mono.fromCallable(() -> {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try (ResponseInputStream<GetObjectResponse> response =
                         s3Client.getObject(request)) {

                return new String(
                        response.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }
        });
    }
}