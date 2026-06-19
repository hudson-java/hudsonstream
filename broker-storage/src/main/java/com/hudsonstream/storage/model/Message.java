package com.hudsonstream.storage.model;

import java.util.Map;

public record Message(
        String key,
        byte[] payload,
        Map<String, String> headers
) {
}
