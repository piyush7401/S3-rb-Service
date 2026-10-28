package com.samsung.batch.service;

import com.samsung.batch.constant.Key;

public enum CacheDestination {
    LIVE,
    NON_LIVE;

    public static CacheDestination fromCacheKey(String cacheKey) {
        if (Key.LIVEKEY.equals(cacheKey)) {
            return LIVE;
        }

        if (Key.NONLIVEKEY.equals(cacheKey)) {
            return NON_LIVE;
        }

        throw new IllegalArgumentException(
                "Unsupported cacheKey: " + cacheKey
        );
    }
}