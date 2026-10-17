package com.samsung.s3rb.service;


import com.samsung.s3rb.source.ProviderDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ProviderService {

    private final List<ProviderDataSource> dataSources;

    public ProviderService(List<ProviderDataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public Mono<Map<String, Object>> getProvider(boolean preview) {

        return dataSources.stream()
                .filter(source -> source.supports(preview))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No data source configured for preview=" + preview
                        ))
                .fetchProviders();
    }
}

