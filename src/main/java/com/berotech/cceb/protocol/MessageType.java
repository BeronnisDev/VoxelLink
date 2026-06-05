package com.berotech.cceb.protocol;

public enum MessageType {
    PING("ping"),
    PONG("pong"),
    ERROR("error"),
    HELLO("hello"),
    AUTH("auth"),
    AUTH_OK("auth_ok");

    private final String wireName;

    MessageType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static MessageType fromWireName(String wireName) {
        if (wireName == null || wireName.isBlank()) {
            throw new IllegalArgumentException("Message type must not be empty");
        }

        for (MessageType type : values()) {
            if (type.wireName.equalsIgnoreCase(wireName)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown message type: " + wireName);
    }
}
