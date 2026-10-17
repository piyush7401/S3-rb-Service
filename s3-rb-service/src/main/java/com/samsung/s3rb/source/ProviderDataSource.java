package com.samsung.s3rb.source;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface ProviderDataSource {

    boolean supports(boolean preview);

    Mono<Map<String, Object>> fetchProviders();
}
