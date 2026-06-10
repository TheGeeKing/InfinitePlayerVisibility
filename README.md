# Infinite Player Visibility

Fabric mod for Minecraft 26.1.x that keeps players visible at very long distances without loading their chunks.

As of 1.2.0, ticking entities can be rendered too.

## Behavior

The server continues to synchronize player entities even outside the normal tracking range.
The client accepts rendering these players even if their chunk is not loaded.
Distant chunks are still not loaded.

Players are handled separately from other entities. Distant mobs and items can only stay visible while their server-side position is still ticking.

## Server behavior

The mod can refresh remote entity tracking every few ticks so clients keep receiving distant player/entity updates. Server admins can tune this in `config/infinite-player-visibility.json`:

- `renderRemotePlayers`: renders distant players.
- `renderRemoteEntities`: renders distant ticking entities.
- `visibilityDistanceBlocks`: maximum render/tracking distance. Values are clamped to Voxy-style distances from 20 to 2,048 chunks: 6-chunk steps below 100 chunks, then 8-chunk steps up to 2,048 chunks.
- `remoteRenderDistanceMode`: controls how the client caps remote rendering. `TERRAIN_CONTEXT` keeps remote entities within the client's terrain context; `MOD_DISTANCE` uses only the mod distance.
- `remoteEntityTrackingIntervalTicks`: how often the server refreshes forced trackers. The default is `4`, matching the original behavior.
- `maxTrackedEntitiesPerRefresh`: optional cap for refreshed forced entities per refresh. Use `0` for no cap.

Clients report their effective render cap to the server. The server then uses the lower of its own config and each client's cap, so a client rendering 32 chunks is not force-sent entities out to 64 chunks.

## Solo Visibility Anchor

The `infinite_player_visibility:solo_visibility_anchor` block is an admin/debug-style helper for keeping one chunk ticking so remote entities there can remain visible.

```mcfunction
/give @p infinite_player_visibility:solo_visibility_anchor
```

The anchor force-loads only the chunk containing the anchor. Remove the block to release the chunk.

When an anchor starts forced chunk loading, the server log includes the dimension and position.

## Installation

Install the same JAR on both the server and the clients. The server side handles network tracking, and the client side removes rendering limits.

The published mod metadata accepts Minecraft `>=26.1 <26.2`, Fabric Loader `>=0.19.1`, Fabric API `>=0.145.1`, and Mod Menu `>=18.0.0-alpha.8` when present.

## Build

This project targets Java 25.

```powershell
.\gradlew.bat build
```

The remapped JAR will be located in build/libs/.

## Dependency audit

Run an OWASP Dependency-Check scan with:

```powershell
.\gradlew.bat dependencyCheckAnalyze
```

If you have an NVD API key, provide it with either a Gradle property:

```powershell
.\gradlew.bat dependencyCheckAnalyze -PnvdApiKey=YOUR_KEY
```

or an environment variable:

```powershell
$env:NVD_API_KEY="YOUR_KEY"
.\gradlew.bat dependencyCheckAnalyze
```

The HTML report is written to `build/reports/dependency-check-report.html`.
