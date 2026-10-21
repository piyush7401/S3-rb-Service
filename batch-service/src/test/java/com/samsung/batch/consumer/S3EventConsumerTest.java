package com.samsung.batch.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.parser.S3EventParser;
import com.samsung.batch.s3.S3ObjectReader;
import com.samsung.batch.transformer.ProviderDataTransformer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class S3EventConsumerTest {

    @Test
    void shouldReadS3ObjectFromCreatedEvent() throws Exception {

        // Arrange: dependencies prepare karo
        S3EventParser parser =
                new S3EventParser(new ObjectMapper());

        S3ObjectReader reader = mock(S3ObjectReader.class);

        String mockJson = """
                {
                  "providerId": "provider-101",
                  "name": "Gaming Provider",
                  "games": [
                    {
                      "id": "game-1",
                      "title": "Racing Game"
                    }
                  ]
                }
                """;

        // Mock reader ko JSON return karne ke liye configure karo
        when(reader.readObject(
                "my-test-bucket",
                "incoming/game+data.json"
        )).thenReturn(Mono.just(mockJson));

        ProviderDataTransformer transformer =
                new ProviderDataTransformer(new ObjectMapper());

        S3EventConsumer consumer =
                new S3EventConsumer(parser, reader, transformer);

        String message = """
                {
                  "Records": [
                    {
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "my-test-bucket"
                        },
                        "object": {
                          "key": "incoming%2Fgame%2Bdata.json"
                        }
                      }
                    }
                  ]
                }
                """;

        // Act: simulated SQS message process karo
        assertDoesNotThrow(
                () -> consumer.consumeS3Event(message)
        );

        // Assert: reader ko correct bucket aur decoded key mili
        verify(reader).readObject(
                "my-test-bucket",
                "incoming/game+data.json"
        );
    }

    @Test
    void shouldSkipFolderMarker() throws Exception {

        S3EventParser parser =
                new S3EventParser(new ObjectMapper());

        S3ObjectReader reader = mock(S3ObjectReader.class);

        ProviderDataTransformer transformer =
                new ProviderDataTransformer(new ObjectMapper());

        S3EventConsumer consumer =
                new S3EventConsumer(parser, reader, transformer);

        String message = """
                {
                  "Records": [
                    {
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "my-test-bucket"
                        },
                        "object": {
                          "key": "incoming/"
                        }
                      }
                    }
                  ]
                }
                """;

        // Folder marker ko process nahi karna chahiye
        assertDoesNotThrow(
                () -> consumer.consumeS3Event(message)
        );

        // S3 reader call nahi hona chahiye
        verifyNoInteractions(reader);
    }
}