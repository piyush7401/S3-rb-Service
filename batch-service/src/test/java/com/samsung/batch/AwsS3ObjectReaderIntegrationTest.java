package com.samsung.batch;


import com.samsung.batch.s3.S3ObjectReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.s3.mode=aws",
        "app.sqs.enabled=false",
        "spring.cloud.aws.region.static=ap-south-1"
})
public class AwsS3ObjectReaderIntegrationTest {

    @Autowired
    private S3ObjectReader s3ObjectReader;

    @Test
    void shouldReadProviderJsonFromRealS3() {
        String json = s3ObjectReader
                .readObject(
                        "piyush-s3-rb-dev-2026",
                        "incoming/provider.json"
                )
                .block(Duration.ofSeconds(30));

        assertNotNull(json);
        assertTrue(json.contains("provider-101"));

        System.out.println("Successfully read JSON from S3:");
        System.out.println(json);
    }
}
