package com.hudsonstream.storage.model;

import java.util.Map;

public record StoredMessage(
        long offset,
        long timestamp,
        String key,
        byte[] payload,
        Map<String, String> headers
) {
}
