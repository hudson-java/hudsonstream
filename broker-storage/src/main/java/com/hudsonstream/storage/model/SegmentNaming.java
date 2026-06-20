package com.hudsonstream.storage.model;

public final class SegmentNaming {
    private static final String format = "segment-%s.log";

    private SegmentNaming() {
    }

    public static String fileName(
            long baseOffset
    ) {
        return format.formatted(baseOffset);
    }
}
