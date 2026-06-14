package com.berotech.voxellink.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.berotech.voxellink.target.TargetSummary;

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
        EditorMessage original = EditorMessage.fileListOk("cc:label:test", "/", List.of("startup.lua", "programs"));
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.FILE_LIST_OK, decoded.type());
        assertEquals("cc:label:test", decoded.targetId());
        assertEquals(List.of("startup.lua", "programs"), decoded.files());
    }

    @Test
    void roundTripFileWriteRequestFields() {
        String json = "{\"type\":\"file_write\",\"targetId\":\"cc:label:test\",\"path\":\"foo.lua\",\"content\":\"print('hi')\"}";
        EditorMessage decoded = EditorMessageCodec.decode(json);
        assertEquals(MessageType.FILE_WRITE, decoded.type());
        assertEquals("cc:label:test", decoded.targetId());
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

    @Test
    void roundTripOpenFile() {
        EditorMessage original = EditorMessage.openFile("cc:label:test", "startup.lua");
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.OPEN_FILE, decoded.type());
        assertEquals("cc:label:test", decoded.targetId());
        assertEquals("startup.lua", decoded.path());
    }

    @Test
    void roundTripTargetListOk() {
        EditorMessage original = EditorMessage.targetListOk(List.of(
                TargetSummary.of("cc:pos:minecraft:overworld:1:64:2", "controller", "cc", "computer"),
                TargetSummary.of("sfm:pos:minecraft:overworld:3:64:4", "", "sfm", "manager")
        ));
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.TARGET_LIST_OK, decoded.type());
        assertEquals(2, decoded.targets().size());
        assertEquals("controller", decoded.targets().get(0).label());
        assertEquals("sfm", decoded.targets().get(1).backend());
    }

    @Test
    void roundTripHelloBackends() {
        EditorMessage original = EditorMessage.hello("VoxelLink voxellink", List.of("cc", "sfm"));
        EditorMessage decoded = EditorMessageCodec.decode(EditorMessageCodec.encode(original));
        assertEquals(MessageType.HELLO, decoded.type());
        assertEquals(List.of("cc", "sfm"), decoded.backends());
    }
}
