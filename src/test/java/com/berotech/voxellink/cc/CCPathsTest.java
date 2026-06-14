package com.berotech.voxellink.cc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CCPathsTest {
    @Test
    void normalizeRootPaths() {
        assertEquals("", CCPaths.normalize(null));
        assertEquals("", CCPaths.normalize(""));
        assertEquals("", CCPaths.normalize("/"));
        assertEquals("", CCPaths.normalize("///"));
    }

    @Test
    void normalizeRegularPath() {
        assertEquals("startup.lua", CCPaths.normalize("startup.lua"));
        assertEquals("programs/foo.lua", CCPaths.normalize("/programs/foo.lua"));
        assertEquals("programs/foo.lua", CCPaths.normalize("\\programs\\foo.lua"));
    }

    @Test
    void rejectsTraversal() {
        assertThrows(IllegalArgumentException.class, () -> CCPaths.normalize("../startup.lua"));
        assertThrows(IllegalArgumentException.class, () -> CCPaths.normalize("programs/../startup.lua"));
        assertThrows(IllegalArgumentException.class, () -> CCPaths.normalize("programs/./startup.lua"));
    }

    @Test
    void rejectsOversizedPath() {
        String longPath = "a".repeat(CCPaths.MAX_PATH_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> CCPaths.normalize(longPath));
    }

    @Test
    void rejectsNullCharacter() {
        assertThrows(IllegalArgumentException.class, () -> CCPaths.normalize("startup\u0000.lua"));
    }

    @Test
    void childJoinsPaths() {
        assertEquals("foo.lua", CCPaths.child("", "foo.lua"));
        assertEquals("programs/foo.lua", CCPaths.child("programs", "foo.lua"));
    }
}
