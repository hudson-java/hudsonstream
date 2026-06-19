package com.hudsonstream.storage.handler;

import com.hudsonstream.storage.model.*;

public interface StorageEngine {

    AppendResult append(
            PartitionId partitionId,
            Message message
    );

    ReadResult read(
            PartitionId partitionId,
            long offset,
            int maxRecords
    );
}
