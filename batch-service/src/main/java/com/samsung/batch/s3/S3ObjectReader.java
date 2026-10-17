package com.samsung.batch.s3;
import reactor.core.publisher.Mono;

public interface S3ObjectReader {

    Mono<String> readObject(
            String bucketName,
            String objectKey
    );
}
