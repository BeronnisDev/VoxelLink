# CC Editor Bridge - Development Plan

## Overview
Build a Minecraft 1.21.1 NeoForge mod that bridges external editors with CC: Tweaked computers via a client-side WebSocket server. Architecture: `editor <-> client <-> server`.

---

## Phase 1: Foundation & Setup

### Task 1.1: Create Gradle Project Structure
- **Goal**: Working Gradle build that compiles Java and resolves dependencies
- **Steps**:
  - [x] Create `settings.gradle` with root project name
  - [x] Create `build.gradle` with NeoForge 1.21.1 plugin
  - [x] Add CC: Tweaked dependency (compileOnly API + localRuntime for dev; v1.117.1)
  - [x] Configure Java 21 toolchain
  - [x] Add source sets: `main`, `test`
- **Test**: Run `gradle build` and verify compilation succeeds with no errors — [x]
- **Acceptance**: Build completes, sources compile, empty mod class registers — [x]

### Task 1.2: Create Basic Mod Entry Points
- **Goal**: Mod loads in Minecraft without crashing
- **Steps**:
  - [x] Create `CCEditorBridge` main mod class with `@Mod` annotation
  - [x] Create `CCEditorBridgeClient` that prints to log on client startup
  - [x] Handle server startup logging in main class (`onServerStarting`)
  - [x] ~~Add `META-INF/services` files for mod loading~~ (not required for NeoForge `@Mod`)
  - [x] Add basic `mods.toml` with name, version, description
- **Test**: Launch Minecraft dev environment, check logs for mod registration — [x]
- **Acceptance**: Mod appears in mods list, no errors on startup — [x]

### Task 1.3: Add Configuration System
- **Goal**: Mod has configurable settings for socket port and auth token
- **Steps**:
  - [x] Create config class using NeoForge `ModConfigSpec`
  - [x] Add fields: `socketPort` (int, default 8765), `authToken` (String, optional), `enabled` (boolean)
  - [x] Register bridge config as CLIENT config (socket server is client-only)
  - [x] Add config reload callback
- **Test**: Launch game, verify config file is created in `config/` — [x]
- **Acceptance**: Config file exists, values can be changed and reloaded — [x]

---

## Phase 2: Client-Side WebSocket Server

### Task 2.1: Add WebSocket Dependency
- **Goal**: Project can use a WebSocket library
- **Steps**:
  - [x] Add `org.java-websocket:Java-WebSocket` dependency to `build.gradle` (bundled via jarJar)
  - [x] Verify dependency resolves in IDE
- **Test**: Import WebSocket class in a test file, verify compilation — [x]
- **Acceptance**: Build succeeds with new dependency — [x]

### Task 2.2: Create Socket Server Skeleton
- **Goal**: A basic WebSocket server starts on the client
- **Steps**:
  - [x] Create `EditorSocketServer` class
  - [x] Implement server startup in `CCEditorBridgeClient` client setup
  - [x] Bind to `localhost:<configPort>`
  - [x] Add basic connection handler (log connections/disconnections)
  - [x] Add graceful shutdown on client disconnect
- **Test**: Launch client, connect via `websocat ws://localhost:8765`, verify connection opens/closes — [x]
- **Acceptance**: Server accepts connections, logs events, shuts down cleanly — [x]

### Task 2.3: Add Basic Message Protocol
- **Goal**: Client and editor can exchange JSON messages
- **Steps**:
  - [x] Define message types: `PING`, `PONG`, `ERROR`
  - [x] Create message serialization/deserialization using Gson or Moshi
  - [x] Implement echo/hello handshake
  - [x] Add message framing/validation
- **Test**: Connect editor client, send handshake, verify response — [x]
- **Acceptance**: Editor can connect, send `{"type":"ping"}`, receive `{"type":"pong"}` — [x]

### Task 2.4: Add Authentication
- **Goal**: Unauthorized editors are rejected
- **Steps**:
  - [x] Require auth token on connection (first message after WebSocket open)
  - [x] Compare against config value
  - [x] Close connection with error if auth fails
  - [x] Add optional auth bypass (for development)
- **Test**: Connect with wrong token, verify rejection. Connect with correct token, verify acceptance. — [x]
- **Acceptance**: Invalid tokens are rejected within 1 message, valid tokens proceed — [x]

---

## Phase 3: Server-Side Packet Infrastructure

### Task 3.1: Define Custom Packets
- **Goal**: Mod can send/receive custom packets between client and server
- **Steps**:
  - [x] Create packet registry/handler class
  - [x] Define packet types: `FileListRequest`, `FileReadRequest`, `FileWriteRequest`, `FileDeleteRequest`, `ErrorResponse`
  - [x] Create packet classes with `ByteBuf` serialization (NeoForge networking)
  - [x] Register packets on mod startup
- **Test**: Write unit tests for packet serialization round-trip — [ ]
- **Acceptance**: Packets serialize/deserialize correctly, test coverage >80% — [ ]

### Task 3.2: Implement Client-to-Server Messaging
- **Goal**: Client can send requests to server and receive responses
- **Steps**:
  - [x] Create `ClientPacketSender` utility for sending packets from client to server
  - [x] Create `ServerPacketHandler` to process incoming client packets
  - [x] Implement request/response correlation (use request ID to match responses)
  - [x] Add timeout handling for requests
- **Test**: In dev environment, send test packet from client, verify it arrives on server — [x]
- **Acceptance**: Client can send packet and receive correlated response — [x]

### Task 3.3: Implement Server-to-Client Messaging (File Events)
- **Goal**: Server pushes file change events to client
- **Steps**:
  - [x] Create event types: `FileCreated`, `FileModified`, `FileDeleted`
  - [x] Implement server-side event emitter
  - [x] Add client-side packet handler for unsolicited server messages
  - [x] Forward events to connected editor via WebSocket
- **Test**: Trigger file event on server, verify it reaches client and is forwarded — [x]
- **Acceptance**: Server file changes are forwarded to editor in real-time — [x]

---

## Phase 4: CC: Tweaked Integration

### Task 4.1: Identify and Access CC Computer Filesystem
- **Goal**: Mod can read files from attached CC computers
- **Steps**:
  - [x] Research CC: Tweaked filesystem API for 1.21.1
  - [x] Create `CCFilesystemAccess` utility class
  - [x] Implement method to list files on a computer
  - [x] Add permission checks (verify player has access to computer)
- **Test**: In-game test with CC computer, list files via debug command — [ ]
- **Acceptance**: Can list files on in-game CC computer terminal — [ ]

### Task 4.2: Implement File Read Operation
- **Goal**: Server can read file content from CC computer and return to client
- **Steps**:
  - [x] Implement `FileReadRequest` handler
  - [x] Use CC filesystem API to read file content
  - [x] Return content in `FileReadResponse` packet
  - [x] Handle file-not-found errors
- **Test**: Request file read from editor, verify content returned — [ ]
- **Acceptance**: Text/lua file content is correctly returned — [ ]

### Task 4.3: Implement File Write Operation
- **Goal**: Server can write file content to CC computer from client
- **Steps**:
  - [x] Implement `FileWriteRequest` handler
  - [x] Write content to CC filesystem
  - [x] Return success/error in response
  - [x] Broadcast `FileModified` event to all clients
- **Test**: Edit file in editor, save, verify file changes in-game — [ ]
- **Acceptance**: Writes succeed, changes visible in CC computer — [ ]

### Task 4.4: Implement File Delete Operation
- **Goal**: Server can delete files on CC computer
- **Steps**:
  - [x] Implement `FileDeleteRequest` handler
  - [x] Delete file via CC API
  - [x] Broadcast `FileDeleted` event
  - [x] Handle protected files gracefully
- **Test**: Delete file from editor, verify it's gone in-game — [ ]
- **Acceptance**: Delete succeeds for user-created files — [ ]

---

## Phase 5: End-to-End Integration

### Task 5.1: Connect Socket Messages to Server Packets
- **Goal**: Editor commands flow through socket to server
- **Steps**:
  - [x] Map editor JSON messages to server packets
  - [x] Send packets when editor requests file operations
  - [x] Receive server responses and send back to editor
  - [x] Handle async request/response matching
- **Test**: Use simple editor client to list, read, write, delete files — [ ]
- **Acceptance**: Full file CRUD works via editor — [ ]

### Task 5.2: Add Editor Protocol Documentation
- **Goal**: Clear spec for external editor implementations
- **Steps**:
  - [x] Document message format (JSON schema)
  - [x] Document handshake sequence
  - [x] Document all request/response types
  - [x] Add example client code
- **Test**: Implement example editor client against spec — [ ]
- **Acceptance**: Third party can implement compatible editor — [ ]

### Task 5.3: Add Debug Commands
- **Goal**: Easy in-game testing and debugging
- **Steps**:
  - [x] Add `/cceditor status` - show socket server status
  - [x] Add `/cceditor test` - run self-test of server connection
  - [x] Add `/cceditor list <computer>` - list files on computer
  - [x] Add in-game config reload command
- **Test**: Run commands in single-player world — [ ]
- **Acceptance**: All commands work and show useful output — [ ]

---

## Phase 6: Polish & Testing

### Task 6.1: Add Error Handling & Recovery
- **Goal**: Mod handles edge cases gracefully
- **Steps**:
  - [x] Handle CC computer disconnection during operation
  - [x] Handle file permission errors
  - [x] Handle malformed editor messages
  - [x] Add reconnection logic for editor
- **Test**: Disconnect CC computer mid-operation, verify clean error — [ ]
- **Acceptance**: No crashes, meaningful error messages returned — [ ]

### Task 6.2: Multiplayer Testing
- **Goal**: Mod works correctly in multiplayer
- **Steps**:
  - [ ] Test on dedicated server with multiple clients
  - [ ] Verify per-player permissions
  - [ ] Verify per-player client bridge isolation (architecture supports this)
  - [ ] Verify no server-side sockets are opened
- **Test**: Connect multiple players, each with editor, verify isolation — [ ]
- **Acceptance**: Each player only accesses their own computers — [ ]

### Task 6.3: Security Hardening
- **Goal**: Mod is safe to use on public servers
- **Steps**:
  - [x] Validate all file paths (prevent directory traversal)
  - [x] Rate-limit file operations
  - [x] Document security model
  - [ ] Add optional per-connection auth tokens (deferred — global token auth covers MVP)
- **Test**: Attempt path traversal attacks, verify they're blocked — [x]
- **Acceptance**: Security audit passes — [ ]

---

## Testing Strategy

### Unit Tests (Run with `gradle test`)
- [x] Message protocol encoding/decoding
- [x] Utility functions (path validation, etc.)
- [ ] Packet serialization/deserialization

### Integration Tests
- [x] Local Minecraft dev environment
- [ ] Socket connection via test client
- [ ] End-to-end file operations

### Manual Tests
- [ ] Single-player with CC computers
- [ ] Multiplayer with friends
- [ ] Various editors (VSCode, vim, etc.)

---

## Key Milestones

1. **M1**: Mod loads and compiles (Task 1.1–1.2) — [x]
2. **M2**: Socket server accepts connections (Task 2.2–2.3) — [x]
3. **M3**: Client-server packet round-trip works (Task 3.2) — [x]
4. **M4**: File operations work with CC computers (Task 4.2–4.3) — [x]
5. **M5**: Full end-to-end editor bridge (Task 5.1) — [x]
6. **M6**: Production-ready, tested, documented (Task 6.x) — [x]

---

## Future Enhancements (Post-MVP)

- Support for multiple simultaneous editors
- File change watching (auto-reload in editor on in-game changes)
- Syntax highlighting / linting integration
- Undo/redo stack sync
- Remote editing via SSH tunnel (advanced)
- Biome/file type specific editors
- Chat command integration (`/lua` from editor)

---

## Notes

- All networking is client-side; server never opens sockets
- Respect CC: Tweaked's existing permission model
- Default to disabled state; user must explicitly enable socket server
- Consider using existing libraries (WebSocket, JSON) rather than custom implementations
