package com.samsung.batch.service;

import com.samsung.batch.model.ProviderData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProviderProcessingRouter {

    private final VersionedCacheService versionedCacheService;
    private static final Logger log = LoggerFactory.getLogger(ProviderProcessingRouter.class);

    public ProviderProcessingRouter(
            VersionedCacheService versionedCacheService
            // Existing constructor dependencies yahan preserve karna
    ) {
        this.versionedCacheService = versionedCacheService;
        // Existing dependencies ki assignments bhi preserve karna
    }

    public void route(ProviderData providerData, String cacheKey, String sequencer) {
        CacheDestination destination =
                CacheDestination.fromCacheKey(cacheKey);

        switch (destination) {
            case LIVE -> processLive(providerData, sequencer);
            case NON_LIVE -> processNonLive(providerData);
        }
    }

    private void processLive(ProviderData providerData, String sequencer) {
        log.info(
                "Routing provider to LIVE processing: providerId={}, sequencer={}",
                providerData.providerId(),
                sequencer
        );

        boolean published =
                versionedCacheService.publish(providerData, sequencer);

        if (!published) {
            log.info(
                    "Ignoring stale or duplicate provider event: providerId={}",
                    providerData.providerId()
            );
            return;
        }

        log.info(
                "Provider version published successfully: providerId={}",
                providerData.providerId()
        );

        // Next step: write provider data to Redis temporary hash.
    }

    private void processNonLive(ProviderData providerData) {
        log.info(
                "Routing provider to NON_LIVE processing: providerId={}",
                providerData.providerId()
        );



        // Next step: implement the Non-Live S3/Athena flow.
    }
}
