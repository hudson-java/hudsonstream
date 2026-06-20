package com.hudsonstream.storage.resolver;

import com.hudsonstream.storage.model.PartitionId;

import java.nio.file.Path;

public class PartitionDirectoryResolver {
    private final Path dataDir;

    public PartitionDirectoryResolver(
            Path dataDir
    ) {
        this.dataDir = dataDir;
    }

    public Path resolve(
            PartitionId partitionId
    ) {
        return dataDir
                .resolve(partitionId.topic())
                .resolve(
                        "partition-" +
                                partitionId.partition()
                );
    }
}
