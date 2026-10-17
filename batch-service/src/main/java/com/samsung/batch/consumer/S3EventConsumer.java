package com.samsung.batch.consumer;

import com.samsung.batch.model.S3ObjectReference;
import com.samsung.batch.parser.S3EventParser;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.sqs.enabled", havingValue = "true")
public class S3EventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(S3EventConsumer.class);

    private final S3EventParser s3EventParser;

    public S3EventConsumer(S3EventParser s3EventParser) {
        this.s3EventParser = s3EventParser;
    }

    @SqsListener("${app.sqs.queue-name}")
    public void consumeS3Event(String message) throws IOException {

        List<S3ObjectReference> references =
                s3EventParser.parse(message);

        for (S3ObjectReference reference : references) {
            log.info(
                    "S3 object created: bucket={}, key={}",
                    reference.bucketName(),
                    reference.objectKey()
            );

            // Next step: read this object from S3.
        }
    }
}
