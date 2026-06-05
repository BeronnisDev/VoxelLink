package com.berotech.cceb.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class EditorMessageCodecTest {
    @Test
    void roundTripPing() {
        EditorMessage original = EditorMessage.ping();
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.PING, decoded.type());
    }

    @Test
    void roundTripAuth() {
        EditorMessage original = EditorMessage.auth("secret");
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.AUTH, decoded.type());
        assertEquals("secret", decoded.token());
    }

    @Test
    void roundTripFileListOk() {
        EditorMessage original = EditorMessage.fileListOk("label:test", "/", List.of("startup.lua", "programs"));
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.FILE_LIST_OK, decoded.type());
        assertEquals("label:test", decoded.computerId());
        assertEquals(List.of("startup.lua", "programs"), decoded.files());
    }

    @Test
    void roundTripFileWriteRequestFields() {
        String json = "{\"type\":\"file_write\",\"computerId\":\"label:test\",\"path\":\"foo.lua\",\"content\":\"print('hi')\"}";
        EditorMessage decoded = EditorMessageCodec.decode(json);
        assertEquals(MessageType.FILE_WRITE, decoded.type());
        assertEquals("label:test", decoded.computerId());
        assertEquals("foo.lua", decoded.path());
        assertEquals("print('hi')", decoded.content());
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> EditorMessageCodec.decode("{"));
    }

    @Test
    void rejectsMissingType() {
        assertThrows(IllegalArgumentException.class, () -> EditorMessageCodec.decode("{\"message\":\"hello\"}"));
    }
}
