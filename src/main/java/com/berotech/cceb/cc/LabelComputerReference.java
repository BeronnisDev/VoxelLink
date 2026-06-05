package com.berotech.cceb.cc;

public record LabelComputerReference(String label) implements ComputerReference {
    public static final String PREFIX = "label:";

    public static LabelComputerReference of(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Computer label must not be empty");
        }
        return new LabelComputerReference(label);
    }

    @Override
    public String encode() {
        return PREFIX + label;
    }

    public static LabelComputerReference parse(String computerId) {
        if (computerId == null || !computerId.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Computer id must start with '" + PREFIX + "'");
        }

        String label = computerId.substring(PREFIX.length());
        if (label.isBlank()) {
            throw new IllegalArgumentException("Computer label must not be empty");
        }

        return new LabelComputerReference(label);
    }
}
