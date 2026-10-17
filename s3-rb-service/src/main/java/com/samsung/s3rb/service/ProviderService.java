package com.samsung.s3rb.service;


import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ProviderService {

    public Mono<Map<String, Object>> getProvider(boolean preview) {
        Map<String, Object> response;

        if (preview) {
            response = Map.of(
                    "source", "S3/Athena (mock)",
                    "preview", true,
                    "providers", new String[]{"Provider-A", "Provider-B"}
            );
        } else {
            response = Map.of(
                    "source", "Redis (mock)",
                    "preview", false,
                    "providers", new String[]{"Provider-A"}
            );
        }

        return Mono.just(response);
    }
}
