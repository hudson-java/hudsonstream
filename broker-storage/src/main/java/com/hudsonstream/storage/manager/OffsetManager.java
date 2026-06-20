package com.hudsonstream.storage.manager;

import com.hudsonstream.storage.model.PartitionId;

public interface OffsetManager {
    long nextOffset(
            PartitionId partitionId
    );
}
