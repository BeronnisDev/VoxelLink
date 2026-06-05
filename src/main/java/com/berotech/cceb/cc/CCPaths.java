package com.berotech.cceb.cc;

public final class CCPaths {
    private CCPaths() {}

    public static String normalize(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "";
        }

        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isEmpty()) {
            return "";
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
