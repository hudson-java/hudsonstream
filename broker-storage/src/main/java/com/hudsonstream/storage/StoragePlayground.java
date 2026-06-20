package com.hudsonstream.storage;

import com.hudsonstream.storage.handler.FileStorageEngine;
import com.hudsonstream.storage.handler.StorageEngine;
import com.hudsonstream.storage.manager.DefaultSegmentManager;
import com.hudsonstream.storage.manager.SegmentManager;
import com.hudsonstream.storage.model.Message;
import com.hudsonstream.storage.model.PartitionId;
import com.hudsonstream.storage.model.ReadResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class StoragePlayground {

    public static void main(String[] args) throws IOException {
        SegmentManager segmentManager = new DefaultSegmentManager(Path.of("./data"));
        StorageEngine storage =
                new FileStorageEngine(
                        segmentManager
                );

        PartitionId partitionId =
                new PartitionId(
                        "order-created",
                        0
                );

//        for (int i = 0; i < 24; i++) {
//            storage.append(
//                    partitionId,
//                    new Message(
//                            "ORDER-" + i,
//                            ("payload-" + i).getBytes(),
//                            Map.of()
//                    )
//            );
//        }

        ReadResult result =
                storage.read(
                        partitionId,
                        34,
                        100
                );

        result.messages()
                .forEach(message -> {
                    System.out.printf(
                            "offset=%d, key=%s, payload=%s%n",
                            message.offset(),
                            message.key(),
                            new String(
                                    message.payload(),
                                    StandardCharsets.UTF_8
                            )
                    );
                });
    }
}
