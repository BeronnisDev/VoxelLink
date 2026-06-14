# VoxelLink

A Minecraft 1.21.1 NeoForge mod that connects external code editors to in-game scripting targets via a client-side WebSocket bridge.

```
editor <-> client (WebSocket) <-> server (packets) <-> script backends (CC, SFM, ...)
```

## Features

- Localhost WebSocket server for editor connections
- Token authentication
- Pluggable backends for **CC: Tweaked** (multi-file computers) and **Super Factory Manager** (manager disk programs)
- File list / read / write / delete (backend-specific capabilities apply)
- Real-time file change events pushed to editors
- Target discovery with namespaced ids (`cc:`, `sfm:`)
- In-game `vledit` shell command on CC computers to open files in a connected editor

## Quick start

1. Install VoxelLink with NeoForge 1.21.1
2. Optionally install [CC: Tweaked](https://tweaked.cc/) and/or [Super Factory Manager](https://github.com/TeamDman/SuperFactoryManager)
3. Enable the bridge in `config/voxellink-client.toml`:

```toml
enabled = true
socketPort = 8765
authToken = "your-secret-token"
preferLabelIds = true
maxOperationsPerMinute = 120
```

4. In-game, look at a CC computer or SFM manager with a disk inserted and run `/voxellink id`
5. Connect your editor to `ws://127.0.0.1:8765/`

## Target ids

| Backend | Example id | Notes |
|---------|------------|-------|
| CC (position) | `cc:pos:minecraft:overworld:10:64:-5` | Multi-file filesystem |
| CC (label) | `cc:label:my-controller` | Requires `os.setComputerLabel` |
| SFM (manager) | `sfm:pos:minecraft:overworld:12:64:-3` | Single virtual file `program.sfml` |

## Documentation

- [Editor protocol spec](docs/EDITOR_PROTOCOL.md)
- [Example Python client](examples/editor_client.py)

## Debug commands

| Command | Description |
|---------|-------------|
| `/voxellink status` | Bridge status and connection info |
| `/voxellink test` | Self-test WebSocket and packet path |
| `/voxellink reload` | Reload client config from disk |
| `/voxellink id` | Show ids for the block you're looking at |
| `/voxellink list [target]` | List files on a target |

## CC shell

On CC computers with VoxelLink installed:

```
vledit startup.lua
vledit programs/miner.lua
```

## Development

```powershell
./gradlew runClient
```

Requires Java 21. CC: Tweaked and Super Factory Manager are included in dev runs via `localRuntime`.

### Multiplayer / dedicated server testing

```powershell
./gradlew runServer   # terminal 1
./gradlew runClient   # terminal 2
```

The dedicated server uses `run-server/` as its game directory.

## License

MIT — see [LICENSE.md](LICENSE.md).
