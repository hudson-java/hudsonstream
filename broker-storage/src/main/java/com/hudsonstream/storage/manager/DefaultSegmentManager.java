package com.hudsonstream.storage.manager;

import com.hudsonstream.storage.config.StorageConfig;
import com.hudsonstream.storage.model.PartitionId;
import com.hudsonstream.storage.model.Segment;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.hudsonstream.storage.utils.FileUtils.openDataInputStream;

public class DefaultSegmentManager implements SegmentManager {

    private static final String PREFIX = "segment-";
    private static final String SUFFIX = ".log";

    private final Path dataDir;

    public DefaultSegmentManager(
            Path dataDir
    ) {
        this.dataDir = dataDir;
    }

    /*
     * active segment = last segment
     */
    @Override
    public Segment activeSegment(
            PartitionId partitionId
    ) {
        List<Segment> segments =
                listSegments(partitionId);

        if (segments.isEmpty()) {
            return createInitialSegment(
                    partitionId
            );
        }

        return segments.getLast();
    }

    @Override
    public List<Segment> listSegments(
            PartitionId partitionId
    ) {
        try {
            Path partitionDir =
                    resolvePartitionDir(
                            partitionId
                    );

            if (!Files.exists(partitionDir)) {
                return List.of();
            }

            try (Stream<Path> stream = Files.list(partitionDir)) {
                return stream
                        .filter(
                                Files::isRegularFile
                        )
                        .filter(
                                this::isSegmentFile
                        )
                        .map(
                                this::toSegment
                        )
                        .sorted(
                                Comparator.comparingLong(
                                        Segment::baseOffset
                                )
                        )
                        .toList();
            }

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    public Segment rotateSegment(
            PartitionId partitionId,
            long nextBaseOffset
    ) {

        try {

            Path partitionDir =
                    resolvePartitionDir(
                            partitionId
                    );

            Files.createDirectories(
                    partitionDir
            );

            Path segmentPath =
                    partitionDir.resolve(
                            fileName(
                                    nextBaseOffset
                            )
                    );

            Files.createFile(
                    segmentPath
            );

            return new Segment(
                    segmentPath,
                    nextBaseOffset
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldRotate(Segment segment) {
        return countRecords(segment) >= StorageConfig.maxRecordsPerSegment;
    }

    @Override
    public Segment findSegmentForOffset(PartitionId partitionId, long offset) {
        List<Segment> segments = listSegments(partitionId);
        Segment result = null;
        for (Segment segment : segments) {
            if (segment.baseOffset() <= offset) {
                result = segment;
            } else {
                break;
            }
        }
        return result;
    }

    private Segment createInitialSegment(
            PartitionId partitionId
    ) {
        return rotateSegment(
                partitionId,
                0
        );
    }

    private boolean isSegmentFile(
            Path path
    ) {

        String fileName =
                path.getFileName()
                        .toString();

        return fileName.startsWith(
                PREFIX
        ) &&
                fileName.endsWith(
                        SUFFIX
                );
    }

    private Segment toSegment(
            Path path
    ) {
        String fileName = path.getFileName().toString();
        String offsetText = fileName
                .substring(
                        PREFIX.length(),
                        fileName.length()
                                - SUFFIX.length()
                );

        long baseOffset =
                Long.parseLong(
                        offsetText
                );

        return new Segment(path, baseOffset);
    }

    private Path resolvePartitionDir(
            PartitionId partitionId
    ) throws IOException {
        Path partitionDir =
                dataDir
                        .resolve(
                                partitionId.topic()
                        )
                        .resolve(
                                "partition-" +
                                        partitionId.partition()
                        );

        Files.createDirectories(
                partitionDir
        );

        return partitionDir;
    }

    private String fileName(
            long baseOffset
    ) {

        return PREFIX +
                baseOffset +
                SUFFIX;
    }

    private long countRecords(
            Segment segment
    ) {
        long count = 0;
        try (DataInputStream in = openDataInputStream(segment.logFile())) {
            while (in.available() > 0) {
                in.readLong(); // offset
                in.readLong(); // timestamp
                long keyLength = in.readLong();
                long payloadLength = in.readLong();
                in.skipBytes((int) (keyLength + payloadLength));
                count++;
            }
            return count;
        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }
}
