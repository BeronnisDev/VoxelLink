package com.berotech.cceb.cc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ComputerReferenceTest {
    @Test
    void parseAndEncodePositionId() {
        String encoded = "pos:minecraft:overworld:10:64:-5";
        ComputerReference reference = ComputerReference.parse(encoded);
        assertEquals(encoded, reference.encode());
    }

    @Test
    void parseAndEncodeLabelId() {
        String encoded = "label:my-controller";
        ComputerReference reference = ComputerReference.parse(encoded);
        assertEquals(encoded, reference.encode());
    }

    @Test
    void labelAllowsColonsInName() {
        String encoded = "label:factory:main";
        ComputerReference reference = ComputerReference.parse(encoded);
        assertEquals(encoded, reference.encode());
    }

    @Test
    void rejectsUnknownPrefix() {
        assertThrows(IllegalArgumentException.class, () -> ComputerReference.parse("id:123"));
    }
}
