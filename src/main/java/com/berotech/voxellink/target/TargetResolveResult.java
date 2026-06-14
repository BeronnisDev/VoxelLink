package com.berotech.voxellink.target;

public sealed interface TargetResolveResult {
    record Success(ResolvedTarget target) implements TargetResolveResult {}

    record Failure(String message) implements TargetResolveResult {}
}
