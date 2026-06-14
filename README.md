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

See [Getting Started](docs/MOD.md#getting-started) for install steps, enabling the bridge, and connecting your editor.

## Target ids

| Backend | Example id | Notes |
|---------|------------|-------|
| CC (position) | `cc:pos:minecraft:overworld:10:64:-5` | Multi-file filesystem |
| CC (label) | `cc:label:my-controller` | Requires `os.setComputerLabel` |
| SFM (manager) | `sfm:pos:minecraft:overworld:12:64:-3` | Single virtual file `program.sfml` |

## Documentation

- [Mod page / getting started](docs/MOD.md#getting-started)
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
