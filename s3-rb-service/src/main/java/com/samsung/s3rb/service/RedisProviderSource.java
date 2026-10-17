package com.samsung.s3rb.service;

import com.samsung.s3rb.source.ProviderDataSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RedisProviderSource implements ProviderDataSource {

    @Override
    public boolean supports(boolean preview) {
        return !preview;
    }

    @Override
    public Mono<Map<String, Object>> fetchProviders() {
        Map<String, Object> response = Map.of(
                "source", "Redis (mock)",
                "preview", false,
                "providers", new String[]{"Provider-A"}
        );

        return Mono.just(response);
    }
}
