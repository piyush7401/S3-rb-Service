package com.samsung.batch.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.model.ProviderData;
import org.springframework.stereotype.Component;

@Component
public class ProviderDataTransformer {

    private final ObjectMapper objectMapper;

    public ProviderDataTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProviderData transform(String rawJson)
            throws JsonProcessingException {

        return objectMapper.readValue(
                rawJson,
                ProviderData.class
        );
    }
}