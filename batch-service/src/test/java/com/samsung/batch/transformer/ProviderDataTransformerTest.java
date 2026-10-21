package com.samsung.batch.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.model.ProviderData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderDataTransformerTest {

    private final ProviderDataTransformer transformer =
            new ProviderDataTransformer(new ObjectMapper());

    @Test
    void shouldTransformJsonIntoProviderData() throws Exception {

        String rawJson = """
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

        ProviderData result = transformer.transform(rawJson);

        assertEquals("provider-101", result.providerId());
        assertEquals("Mock Gaming Provider", result.name());
        assertEquals(2, result.games().size());
        assertEquals("Racing Game", result.games().get(0).title());
    }
}