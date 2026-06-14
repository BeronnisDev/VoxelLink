package com.berotech.voxellink.target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TargetIdTest {
    @Test
    void parseCcPositionTarget() {
        TargetId targetId = TargetId.parse("cc:pos:minecraft:overworld:10:64:-5");
        assertEquals("cc", targetId.namespace());
        assertEquals("pos:minecraft:overworld:10:64:-5", targetId.localId());
        assertEquals("cc:pos:minecraft:overworld:10:64:-5", targetId.encode());
    }

    @Test
    void parseSfmPositionTarget() {
        TargetId targetId = TargetId.parse("sfm:pos:minecraft:overworld:3:64:4");
        assertEquals("sfm", targetId.namespace());
        assertEquals("pos:minecraft:overworld:3:64:4", targetId.localId());
    }

    @Test
    void rejectsMissingLocalId() {
        assertThrows(IllegalArgumentException.class, () -> TargetId.parse("cc:"));
    }
}
