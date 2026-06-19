package com.hudsonstream.storage.handler;

import com.hudsonstream.storage.model.Message;
import com.hudsonstream.storage.model.PartitionId;
import com.hudsonstream.storage.model.ReadResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileStorageEngineTest {
    StorageEngine storage = new FileStorageEngine();

    @Test
    void shouldAppendAndRead() {
        PartitionId partitionId = new PartitionId("order-created", 0);

        storage.append(
                partitionId,
                new Message(
                        "ORDER-1",
                        "hello".getBytes(),
                        Map.of()
                )
        );

        ReadResult result =
                storage.read(
                        partitionId,
                        0,
                        100
                );

        assertEquals(
                1,
                result.messages().size()
        );

        assertEquals(
                "hello",
                new String(
                        result.messages()
                                .getFirst()
                                .payload()
                )
        );
    }
}
