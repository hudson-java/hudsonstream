package com.hudsonstream.storage.model;

import java.nio.file.Path;

public record Segment(Path logFile, long baseOffset) {
}
