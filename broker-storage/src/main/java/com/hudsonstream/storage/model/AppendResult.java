package com.hudsonstream.storage.model;

public record AppendResult(
        PartitionId partitionId,
        long offset,
        long timestamp
) {
}
