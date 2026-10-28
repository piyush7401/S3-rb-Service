package com.samsung.batch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.batch.model.ProviderData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class VersionedCacheService {

    private static final Logger log =
            LoggerFactory.getLogger(VersionedCacheService.class);
    private static final Duration STAGING_TTL = Duration.ofHours(24);

    private static final long OLD_VERSION_MIN_TTL_MS = 5 * 60 * 1000L;
    private static final long OLD_VERSION_MAX_TTL_MS = 10 * 60 * 1000L;

    private static final DefaultRedisScript<String> READ_ACTIVE_SCRIPT =
            new DefaultRedisScript<>("""
            local versionKey = redis.call('GET', KEYS[1])

            if not versionKey then
                return nil
            end

            return redis.call('GET', versionKey)
            """, String.class);


    private static final DefaultRedisScript<Long> PUBLISH_SCRIPT =
            new DefaultRedisScript<>("""
            local activeKey = KEYS[1]
            local newVersionKey = KEYS[2]
            local sequencerKey = KEYS[3]

            local newSequencer = ARGV[2]
            local oldSequencer = redis.call('GET', sequencerKey)

            if redis.call('EXISTS', newVersionKey) == 0 then
                return -1
            end

            -- Reject stale or duplicate events.
            if oldSequencer then
                local width = math.max(#newSequencer, #oldSequencer)
                local newPadded =
                    string.rep('0', width - #newSequencer) .. newSequencer
                local oldPadded =
                    string.rep('0', width - #oldSequencer) .. oldSequencer

                if newPadded <= oldPadded then
                    return 0
                end
            end

            local oldVersionKey = redis.call('GET', activeKey)

            redis.call('PERSIST', newVersionKey)
            redis.call('SET', activeKey, newVersionKey)
            redis.call('SET', sequencerKey, newSequencer)

            if oldVersionKey
               and oldVersionKey ~= newVersionKey
               and redis.call('EXISTS', oldVersionKey) == 1 then
                redis.call('PEXPIRE', oldVersionKey, ARGV[1])
            end

            return 1
            """, Long.class);

    private String sequencerKey(String providerId) {
        return "s3Providers:{" + providerId + "}:sequencer";
    }

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public VersionedCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean publish(
            ProviderData providerData,
            String sequencer
    ) {
        if (providerData == null
                || providerData.providerId() == null
                || providerData.providerId().isBlank()) {
            throw new IllegalArgumentException(
                    "Provider data and providerId must not be empty");
        }

        if (sequencer == null || sequencer.isBlank()
                || !sequencer.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException(
                    "A valid S3 sequencer is required"
            );
        }

        sequencer = sequencer.toUpperCase(java.util.Locale.ROOT);

        String providerId = providerData.providerId();
        String version = UUID.randomUUID().toString();

        String activeKey = activeKey(providerId);
        String versionKey = versionKey(providerId, version);

        try {
            String json = objectMapper.writeValueAsString(providerData);

            // Stage the new version. It expires automatically if
            // publishing never completes.
            redisTemplate.opsForValue().set(
                    versionKey, json, STAGING_TTL);

            long oldVersionTtlMs = ThreadLocalRandom.current()
                    .nextLong(
                            OLD_VERSION_MIN_TTL_MS,
                            OLD_VERSION_MAX_TTL_MS + 1);

            // Atomically activate the new version and retire the old one.
            Long result = redisTemplate.execute(
                    PUBLISH_SCRIPT,
                    List.of(
                            activeKey,
                            versionKey,
                            sequencerKey(providerId)
                    ),
                    Long.toString(oldVersionTtlMs),
                    sequencer
            );

            if (result == null || result == -1L) {
                throw new IllegalStateException(
                        "Failed to publish provider version: " + providerId
                );
            }

            if (result == 0L) {
                log.info(
                        "Ignoring stale or duplicate event: providerId={}, sequencer={}",
                        providerId,
                        sequencer
                );
                return false;
            }

            return true;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize provider: " + providerId, e);
        }
    }

    public String getActiveProviderJson(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException(
                    "providerId must not be empty");
        }

        String activeKey = activeKey(providerId);

        return redisTemplate.execute(
                READ_ACTIVE_SCRIPT,
                List.of(activeKey)
        );
    }

    private String activeKey(String providerId) {
        return "s3Providers:{" + providerId + "}:active";
    }

    private String versionKey(String providerId, String version) {
        return "s3Providers:{" + providerId + "}:v:" + version;
    }

}
