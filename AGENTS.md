# VoxelLink - Modding Agent Documentation

## Project Name
**VoxelLink** - A Minecraft 1.21.1 NeoForge mod connecting external editors to in-game scripting targets via a client-side WebSocket bridge.

## Project Context

Architecture:

```
editor <-> client <-> server <-> script backends (CC, SFM, ...)
```

The client runs on the player's machine and exposes a local WebSocket that editors connect to. Communication flows editor → client → server. No server-side sockets are exposed.

## Key Technologies

- **Minecraft Version**: 1.21.1
- **Mod Framework**: NeoForge
- **Integrations**: CC: Tweaked (optional), Super Factory Manager (optional, reflection)
- **Communication**: WebSockets on client side (localhost only)
- **Language**: Java

## Architecture

- **Core**: WebSocket bridge, protocol, packets, config, commands
- **target/**: `TargetProvider` registry, namespaced target ids
- **cc/**: CC: Tweaked provider (filesystem, `vledit`, Lua API)
- **sfm/**: Super Factory Manager provider (virtual `program.sfml` file)

## Entry Points

### Client Side
- `VoxelLinkClient` - WebSocket server startup
- `EditorBridgeService` - connection management
- `EditorFileOperations` - protocol → packet mapping

### Server Side
- `ServerPacketHandler` - dispatches to `TargetProviderRegistry`
- `TargetProvider` implementations per backend

## Code Conventions
- Package: `com.berotech.voxellink.*`
- Mod id: `voxellink`
- Use optional mod dependencies; providers register only when backend mods are present
- Prefer events over mixins

## Important Constraints
- **NEVER** introduce server-side sockets
- Respect backend permission models
- Default bridge disabled; localhost + optional token auth
- SFM integration uses reflection (no published API)

## References
- CC: Tweaked API: https://javadoc.cc-tweaked.dev/1.21.1/
- Super Factory Manager: https://github.com/TeamDman/SuperFactoryManager
- NeoForge Docs: https://docs.neoforge.net/
