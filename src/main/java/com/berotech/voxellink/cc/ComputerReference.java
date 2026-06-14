package com.berotech.voxellink.cc;

public sealed interface ComputerReference permits PositionComputerReference, LabelComputerReference {
    String encode();

    static ComputerReference parse(String computerId) {
        if (computerId == null || computerId.isBlank()) {
            throw new IllegalArgumentException("Computer id must not be empty");
        }

        if (computerId.startsWith(PositionComputerReference.PREFIX)) {
            return PositionComputerReference.parse(computerId);
        }
        if (computerId.startsWith(LabelComputerReference.PREFIX)) {
            return LabelComputerReference.parse(computerId);
        }

        throw new IllegalArgumentException("Computer id must start with '"
                + PositionComputerReference.PREFIX + "' or '" + LabelComputerReference.PREFIX + "'");
    }
}
