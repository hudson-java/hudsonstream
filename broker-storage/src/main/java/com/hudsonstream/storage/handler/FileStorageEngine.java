package com.hudsonstream.storage.handler;

import com.hudsonstream.storage.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileStorageEngine implements StorageEngine {
    private final Map<PartitionId, List<StoredMessage>> storage = new ConcurrentHashMap<>();

    @Override
    public AppendResult append(PartitionId partitionId, Message message) {
        List<StoredMessage> records = storage.computeIfAbsent(partitionId, ignored -> new CopyOnWriteArrayList<>());
        long offset = records.size();
        StoredMessage storedMessage = new StoredMessage(
                offset,
                System.currentTimeMillis(),
                message.key(),
                message.payload(),
                message.headers()
        );
        records.add(storedMessage);
        return new AppendResult(partitionId, offset, storedMessage.timestamp());
    }

    @Override
    public ReadResult read(PartitionId partitionId, long offset, int maxRecords) {
        List<StoredMessage> records = storage.getOrDefault(partitionId, List.of());

        List<StoredMessage> result = records.stream()
                .filter(m -> m.offset() >= offset)
                .limit(maxRecords)
                .toList();

        long nextOffset = result.isEmpty()
                            ? offset
                            : result.getLast().offset() + 1;

        return new ReadResult(result, nextOffset);
    }
}
