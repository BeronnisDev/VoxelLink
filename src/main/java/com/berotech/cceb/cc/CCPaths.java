package com.berotech.cceb.cc;

public final class CCPaths {
    public static final int MAX_PATH_LENGTH = 512;

    private CCPaths() {}

    public static String normalize(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "";
        }

        if (path.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException("Invalid path: exceeds maximum length of " + MAX_PATH_LENGTH);
        }
        if (path.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Invalid path: contains null character");
        }

        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException("Invalid path: exceeds maximum length of " + MAX_PATH_LENGTH);
        }

        for (String segment : normalized.split("/")) {
            if (segment.isEmpty()) {
                throw new IllegalArgumentException("Invalid path: empty segment in '" + path + "'");
            }
            if (".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("Invalid path: '" + path + "' contains '" + segment + "'");
            }
        }

        return normalized;
    }

    public static String child(String directory, String name) {
        if (directory == null || directory.isEmpty()) {
            return name;
        }
        return directory + "/" + name;
    }
}
