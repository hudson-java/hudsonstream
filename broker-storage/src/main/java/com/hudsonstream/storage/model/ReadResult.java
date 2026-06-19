package com.hudsonstream.storage.model;

import java.util.List;

public record ReadResult(
        List<StoredMessage> messages,
        long nextOffset
) {
}
