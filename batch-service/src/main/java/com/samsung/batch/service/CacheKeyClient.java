package com.samsung.batch.service;

import com.samsung.batch.model.CacheKeyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CacheKeyClient {

    private final WebClient webClient;

    public CacheKeyClient(
            @Value("${app.s3rb-service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public CacheKeyResponse getCacheKey(String providerId) {
        CacheKeyResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/config/cache-key")
                        .queryParam("providerId", providerId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CacheKeyResponse.class)
                .block();

        if (response == null || response.cacheKey() == null
                || response.cacheKey().isBlank()) {
            throw new IllegalStateException(
                    "Cache key API returned an empty response for provider: "
                            + providerId);
        }

        return response;
    }
}