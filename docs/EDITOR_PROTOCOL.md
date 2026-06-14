# VoxelLink — Editor Protocol

JSON-over-WebSocket protocol for connecting external editors to in-game scripting targets.

The WebSocket server runs on the **Minecraft client** only, bound to `127.0.0.1`.

## Connection

| Setting | Default | Config key |
|---------|---------|------------|
| Host | `127.0.0.1` | (fixed) |
| Port | `8765` | `socketPort` |
| Enabled | `false` | `enabled` |
| Auth token | (none) | `authToken` |
| Rate limit | 120 ops/min | `maxOperationsPerMinute` (0 = unlimited) |

```
ws://127.0.0.1:8765/
```

## Message format

All messages are JSON objects with a required `type` field.

```json
{
  "type": "message_type",
  "message": "optional string",
  "token": "optional string",
  "targetId": "optional string",
  "path": "optional string",
  "content": "optional string",
  "files": ["optional", "array"],
  "targets": [{"id":"...","backend":"cc","kind":"computer","label":"..."}],
  "backends": ["cc", "sfm"]
}
```

Maximum message size: **65536 bytes**.

## Handshake

**Server → Editor** on connect:

```json
{"type":"hello","message":"VoxelLink voxellink","backends":["cc","sfm"]}
```

When auth is required, `message` is `"Authentication required"` until the editor sends:

```json
{"type":"auth","token":"your-token-here"}
```

**Server → Editor** on success: `{"type":"auth_ok"}`

## Target ids

Targets use a namespace prefix:

| Format | Example |
|--------|---------|
| CC position | `cc:pos:minecraft:overworld:10:64:-5` |
| CC label | `cc:label:my-controller` |
| SFM manager | `sfm:pos:minecraft:overworld:12:64:-3` |

Use `/voxellink id` in-game while looking at a supported block.

### Backend notes

- **CC**: full directory tree; paths like `startup.lua` or `programs/foo.lua`
- **SFM**: single virtual file `program.sfml` (~32KB limit)

## Target discovery

**Editor → Server**

```json
{"type":"target_list"}
```

**Server → Editor**

```json
{
  "type":"target_list_ok",
  "targets":[
    {"id":"cc:pos:minecraft:overworld:10:64:-5","label":"my-controller","backend":"cc","kind":"computer"},
    {"id":"sfm:pos:minecraft:overworld:12:64:-3","label":"","backend":"sfm","kind":"manager"}
  ]
}
```

## File operations

All file requests require `targetId`. Paths use forward slashes.

### List / read / write / delete

```json
{"type":"file_list","targetId":"cc:label:my-controller","path":"/"}
{"type":"file_read","targetId":"cc:pos:minecraft:overworld:10:64:-5","path":"startup.lua"}
{"type":"file_write","targetId":"sfm:pos:minecraft:overworld:12:64:-3","path":"program.sfml","content":"EVERY 20 TICKS DO\nEND\n"}
{"type":"file_delete","targetId":"cc:label:my-controller","path":"test.lua"}
```

Responses: `file_list_ok`, `file_read_ok`, `file_write_ok`, `file_delete_ok`, or `error`.

## Server-initiated events

```json
{"type":"file_modified","targetId":"cc:label:my-controller","path":"startup.lua"}
{"type":"open_file","targetId":"cc:pos:minecraft:overworld:10:64:-5","path":"startup.lua"}
```

`open_file` is sent when a player runs `vledit <file>` on a CC computer.

## Keepalive

```json
{"type":"ping"}
{"type":"pong"}
```

## Debug commands

| Command | Description |
|---------|-------------|
| `/voxellink status` | Show bridge status |
| `/voxellink test` | Self-test WebSocket + packet path |
| `/voxellink reload` | Reload client config |
| `/voxellink id` | Show target ids for looked-at block |
| `/voxellink list [target]` | List files on a target |

## Security

- WebSocket binds to **localhost only**
- Optional token auth
- Per-connection rate limiting
- Backend-specific access checks (CC `isUsable`, SFM proximity/disk checks)
- Path traversal blocked
- Server never opens a socket to editors

## Example

```powershell
websocat ws://127.0.0.1:8765
{"type":"auth","token":"your-token"}
{"type":"target_list"}
{"type":"file_list","targetId":"cc:label:my-controller","path":"/"}
```

See [`examples/editor_client.py`](examples/editor_client.py).
