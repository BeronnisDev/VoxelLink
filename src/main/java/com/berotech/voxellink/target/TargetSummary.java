package com.berotech.voxellink.target;

public record TargetSummary(String id, String label, String backend, String kind) {
    public static TargetSummary of(String id, String label, String backend, String kind) {
        return new TargetSummary(id, label == null ? "" : label, backend, kind);
    }
}
