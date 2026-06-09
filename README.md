# Infinite Player Visibility

Fabric mod for Minecraft 1.21.11 that keeps players visible at very long distances without loading their chunks.

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
- `visibilityDistanceBlocks`: maximum render/tracking distance.
- `remoteEntityTrackingIntervalTicks`: how often the server refreshes forced trackers. The default is `4`, matching the original behavior.
- `maxTrackedEntitiesPerRefresh`: optional cap for refreshed forced entities per refresh. Use `0` for no cap.

## Solo Visibility Anchor

The `infinite_player_visibility:solo_visibility_anchor` block is an admin/debug-style helper for keeping an area ticking so remote entities there can remain visible.

```mcfunction
/give @p infinite_player_visibility:solo_visibility_anchor
```

By default, the anchor is enabled and force-loads chunks within a `128` block radius, matching the original behavior. Server admins can control it with:

- `enableSoloVisibilityAnchor`: enables or disables anchor force-loading.
- `anchorRadiusBlocks`: force-loaded anchor radius, clamped from `0` to `128` blocks.

When an anchor starts or updates forced chunk loading, the server log includes the dimension, position, radius, and chunk count.

## Installation

Install the same JAR on both the server and the clients. The server side handles network tracking, and the client side removes rendering limits.

## Build

This project targets Java 25.

```powershell
.\gradlew.bat build
```

The remapped JAR will be located in build/libs/.
