package com.berotech.voxellink.sfm;

public final class SFMPaths {
    public static final String PROGRAM_FILE = "program.sfml";

    private SFMPaths() {}

    public static String normalize(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return PROGRAM_FILE;
        }

        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Path traversal is not allowed");
        }
        if (!PROGRAM_FILE.equals(normalized)) {
            throw new IllegalArgumentException("SFM targets expose a single file: " + PROGRAM_FILE);
        }
        return normalized;
    }
}
