package com.hudsonstream.storage.handler;

import com.hudsonstream.storage.manager.SegmentManager;
import com.hudsonstream.storage.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hudsonstream.storage.utils.FileUtils.openDataInputStream;
import static com.hudsonstream.storage.utils.FileUtils.openDataOutputStream;

public class FileStorageEngine implements StorageEngine {
    private final SegmentManager segmentManager;

    public FileStorageEngine(SegmentManager segmentManager) {
        this.segmentManager = segmentManager;
    }

    @Override
    public AppendResult append(PartitionId partitionId, Message message) {
        try {
            Segment activeSegment = segmentManager.activeSegment(partitionId);
            long nextOffset = lastOffset(activeSegment.logFile()) + 1;
            if (segmentManager.shouldRotate(activeSegment)) {
                activeSegment = segmentManager.rotateSegment(partitionId, nextOffset);
            }
            Path targetSegmentPath = activeSegment.logFile();
            long timestamp = System.currentTimeMillis();
            try (DataOutputStream outputStream = openDataOutputStream(targetSegmentPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                byte[] keyBytes = message.key().getBytes(StandardCharsets.UTF_8);
                byte[] payload = message.payload();

                // nextOffset
                outputStream.writeLong(nextOffset);

                // timestamp
                outputStream.writeLong(timestamp);

                // payload length
                outputStream.writeInt(
                        keyBytes.length
                );

                outputStream.writeInt(
                        payload.length
                );
                // keyBytes
                outputStream.write(
                        keyBytes
                );

                // payload
                outputStream.write(
                        payload
                );
                return new AppendResult(
                        partitionId,
                        nextOffset,
                        timestamp
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadResult read(
            PartitionId partitionId,
            long offset,
            int maxRecords
    ) {

        try {

            Segment startSegment =
                    segmentManager
                            .findSegmentForOffset(
                                    partitionId,
                                    offset
                            );

            if (startSegment == null) {

                return new ReadResult(
                        List.of(),
                        offset
                );
            }

            List<Segment> segments =
                    segmentManager
                            .listSegments(
                                    partitionId
                            );

            int startIndex =
                    segments.indexOf(
                            startSegment
                    );

            List<StoredMessage> messages =
                    new ArrayList<>();

            for (
                    int i = startIndex;
                    i < segments.size()
                            && messages.size() < maxRecords;
                    i++
            ) {

                Segment segment =
                        segments.get(i);

                try (
                        DataInputStream inputStream =
                                openDataInputStream(
                                        segment.logFile()
                                )
                ) {

                    while (
                            inputStream.available() > 0
                                    && messages.size() < maxRecords
                    ) {

                        long storedOffset =
                                inputStream.readLong();

                        long timestamp =
                                inputStream.readLong();

                        int keyLength =
                                inputStream.readInt();

                        int payloadLength =
                                inputStream.readInt();

                        byte[] keyBytes =
                                inputStream.readNBytes(
                                        keyLength
                                );

                        byte[] payload =
                                inputStream.readNBytes(
                                        payloadLength
                                );

                        if (
                                storedOffset >= offset
                        ) {

                            messages.add(
                                    new StoredMessage(
                                            storedOffset,
                                            timestamp,
                                            new String(
                                                    keyBytes,
                                                    StandardCharsets.UTF_8
                                            ),
                                            payload,
                                            Map.of()
                                    )
                            );
                        }
                    }
                }
            }

            long nextOffset =
                    messages.isEmpty()
                            ? offset
                            : messages.getLast()
                            .offset() + 1;

            return new ReadResult(
                    messages,
                    nextOffset
            );

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    private long lastOffset(Path targetSegmentPath) {
        if (!Files.exists(targetSegmentPath)) {
            return -1;
        }
        long last = -1;
        try (DataInputStream inputStream = openDataInputStream(targetSegmentPath)) {
            while (inputStream.available() > 0) {
                last = inputStream.readLong();

                // skip timestamp
                inputStream.readLong();

                int keyLength =
                        inputStream.readInt();

                int payloadLength =
                        inputStream.readInt();
                inputStream.skipBytes(
                        keyLength +
                                payloadLength
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return last;
    }
}
