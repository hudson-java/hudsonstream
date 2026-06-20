package com.hudsonstream.storage.handler;

import com.hudsonstream.storage.model.*;

import java.io.IOException;

public interface StorageEngine {

    AppendResult append(
            PartitionId partitionId,
            Message message
    ) throws IOException;

    ReadResult read(
            PartitionId partitionId,
            long offset,
            int maxRecords
    );
}
