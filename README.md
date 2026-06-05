# CC Editor Bridge

A Minecraft 1.21.1 NeoForge mod that bridges external code editors to CC: Tweaked computers via a client-side WebSocket server.

```
editor <-> client (WebSocket) <-> server (packets) <-> CC computer filesystem
```

## Features

- Localhost WebSocket server for editor connections
- Token authentication
- File list / read / write / delete against CC computer HDD storage
- Real-time file change events pushed to editors
- Computer identification by position (`pos:`) or CC label (`label:`)
- In-game `ccedit` shell command to open files in a connected editor

## Quick start

1. Install the mod with [CC: Tweaked](https://tweaked.cc/) and NeoForge 1.21.1
2. Enable the bridge in `config/cceditorbridge-client.toml`:

```toml
enabled = true
socketPort = 8765
authToken = "your-secret-token"
preferLabelIds = true
maxOperationsPerMinute = 120
```

3. In-game, look at a CC computer and run `/cceditor id` to get its computer id
4. Connect your editor to `ws://127.0.0.1:8765/`

## Documentation

- [Editor protocol spec](docs/EDITOR_PROTOCOL.md)
- [Example Python client](examples/editor_client.py)
- [Development plan](PLAN.md)

## Debug commands

| Command | Description |
|---------|-------------|
| `/cceditor status` | Bridge status and connection info |
| `/cceditor test` | Self-test WebSocket and packet path |
| `/cceditor reload` | Reload client config from disk |
| `/cceditor id` | Show ids for the computer you're looking at |
| `/cceditor list [computer]` | List files on a computer |

## Development

```powershell
./gradlew runClient
```

Requires Java 21.

### Multiplayer / dedicated server testing

NeoForge includes a dedicated server run config. Use two terminals:

**Terminal 1 — dedicated server**
```powershell
./gradlew runServer
```

**Terminal 2 — client**
```powershell
./gradlew runClient
```

Connect the client to `localhost` (default port `25565`). The server uses `run-server/` so it can run at the same time as the client (`run/`).

CC: Tweaked is included automatically in both runs via `localRuntime`. Each connected player gets their own localhost WebSocket bridge on their client.
