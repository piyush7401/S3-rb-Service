
package com.samsung.batch.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.model.S3ObjectReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class S3EventParserTest {

    private final S3EventParser parser =
            new S3EventParser(new ObjectMapper());

    @Test
    void shouldParseS3ObjectCreatedEvent() throws Exception {
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
                      "key": "providers%2Fgame%2Bdata.json"
                    }
                  }
                }
              ]
            }
            """;

        List<S3ObjectReference> result = parser.parse(message);

        assertEquals(1, result.size());
        assertEquals("my-test-bucket", result.get(0).bucketName());
        assertEquals("providers/game+data.json",
                result.get(0).objectKey());
    }
}
