
package com.samsung.batch.consumer;

import com.samsung.batch.model.S3ObjectReference;
import com.samsung.batch.model.ProviderData;
import com.samsung.batch.parser.S3EventParser;
import com.samsung.batch.s3.S3ObjectReader;
import com.samsung.batch.transformer.ProviderDataTransformer;
import com.samsung.batch.service.CacheKeyClient;
import com.samsung.batch.model.CacheKeyResponse;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.sqs.enabled",
        havingValue = "true"
)
public class S3EventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(S3EventConsumer.class);

    private final S3EventParser s3EventParser;
    private final S3ObjectReader s3ObjectReader;
    private final ProviderDataTransformer transformer;
    private final CacheKeyClient cacheKeyClient;

    public S3EventConsumer(
            S3EventParser s3EventParser,
            S3ObjectReader s3ObjectReader,
            ProviderDataTransformer transformer,
            CacheKeyClient cacheKeyClient
    ) {
        this.s3EventParser = s3EventParser;
        this.s3ObjectReader = s3ObjectReader;
        this.transformer = transformer;
        this.cacheKeyClient = cacheKeyClient;
    }

    @SqsListener("${app.sqs.queue-name}")
    public void consumeS3Event(String message) throws IOException {

        log.info("Received SQS message body: {}", message);

        // Ignore AWS S3 notification test events
        if (message.contains("\"Event\":\"s3:TestEvent\"")) {
            log.info("Skipping S3 test event");
            return;
        }

        List<S3ObjectReference> references =
                s3EventParser.parse(message);

        for (S3ObjectReference reference : references) {

            String bucketName = reference.bucketName();
            String objectKey = reference.objectKey();

            // Ignore S3 folder-marker objects, e.g. "incoming/"
            if (objectKey == null
                    || objectKey.isBlank()
                    || objectKey.endsWith("/")) {

                log.info("Skipping folder marker: bucket={}, key={}",
                        bucketName, objectKey);
                continue;
            }

            // Process only objects inside the incoming/ prefix
            if (!objectKey.startsWith("incoming/")) {
                log.info("Skipping object outside incoming/: key={}",
                        objectKey);
                continue;
            }

            log.info("S3 object created: bucket={}, key={}",
                    bucketName, objectKey);

            String rawJson = s3ObjectReader
                    .readObject(bucketName, objectKey)
                    .doOnError(error ->
                            log.error(
                                    "Failed to read S3 object: bucket={}, key={}",
                                    bucketName, objectKey, error
                            ))
                    .block();

            if (rawJson == null || rawJson.isBlank()) {
                throw new IllegalStateException(
                        "S3 object returned empty content: " + objectKey
                );
            }

            ProviderData providerData = transformer.transform(rawJson);

            CacheKeyResponse cacheKeyResponse =
                    cacheKeyClient.getCacheKey(providerData.providerId());

            String cacheKey = cacheKeyResponse.cacheKey();

            log.info(
                    "Provider processed: id={}, name={}, games={}, cacheKey={}",
                    providerData.providerId(),
                    providerData.name(),
                    providerData.games() == null
                            ? 0
                            : providerData.games().size(),
                    cacheKey
            );

            log.info(
                    "Provider processed: id={}, name={}, games={}",
                    providerData.providerId(),
                    providerData.name(),
                    providerData.games() == null
                            ? 0
                            : providerData.games().size()
            );
        }
    }
}
