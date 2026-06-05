package com.berotech.cceb.protocol;

public enum MessageType {
    PING("ping"),
    PONG("pong"),
    ERROR("error"),
    HELLO("hello"),
    AUTH("auth"),
    AUTH_OK("auth_ok"),
    FILE_CREATED("file_created"),
    FILE_MODIFIED("file_modified"),
    FILE_DELETED("file_deleted"),
    FILE_LIST("file_list"),
    FILE_LIST_OK("file_list_ok"),
    FILE_READ("file_read"),
    FILE_READ_OK("file_read_ok"),
    FILE_WRITE("file_write"),
    FILE_WRITE_OK("file_write_ok"),
    FILE_DELETE("file_delete"),
    FILE_DELETE_OK("file_delete_ok"),
    COMPUTER_LIST("computer_list"),
    COMPUTER_LIST_OK("computer_list_ok"),
    OPEN_FILE("open_file");

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
