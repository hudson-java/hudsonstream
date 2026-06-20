package com.hudsonstream.storage.manager;

import com.hudsonstream.storage.model.PartitionId;
import com.hudsonstream.storage.model.Segment;

import java.util.List;

public interface SegmentManager {
    Segment activeSegment(
            PartitionId partitionId
    );

    List<Segment> listSegments(
            PartitionId partitionId
    );

    Segment rotateSegment(
            PartitionId partitionId,
            long nextBaseOffset
    );

    boolean shouldRotate(
            Segment segment
    );

    Segment findSegmentForOffset(
            PartitionId partitionId,
            long offset
    );
}
