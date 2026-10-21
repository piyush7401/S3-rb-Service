package com.samsung.batch.model;

import java.util.List;

public record ProviderData(
        String providerId,
        String name,
        List<GameData> games
) {
    public record GameData(
            String id,
            String title
    ) {
    }
}