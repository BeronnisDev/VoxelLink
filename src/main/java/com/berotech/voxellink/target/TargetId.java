package com.berotech.voxellink.target;

public record TargetId(String namespace, String localId) {
    public String encode() {
        return namespace + ":" + localId;
    }

    public static TargetId parse(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Target id must not be empty");
        }

        int separator = targetId.indexOf(':');
        if (separator <= 0 || separator == targetId.length() - 1) {
            throw new IllegalArgumentException("Target id must use namespace:local form, e.g. cc:pos:minecraft:overworld:0:64:0");
        }

        return new TargetId(targetId.substring(0, separator), targetId.substring(separator + 1));
    }
}
