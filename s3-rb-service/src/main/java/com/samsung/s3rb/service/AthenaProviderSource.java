package com.samsung.s3rb.service;

import com.samsung.s3rb.source.ProviderDataSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AthenaProviderSource implements ProviderDataSource {

    @Override
    public boolean supports(boolean preview) {
        return preview;
    }

    @Override
    public Mono<Map<String, Object>> fetchProviders() {
        Map<String, Object> response = Map.of(
                "source", "S3/Athena (mock)",
                "preview", true,
                "providers", new String[]{"Provider-A", "Provider-B"}
        );

        return Mono.just(response);
    }
}
