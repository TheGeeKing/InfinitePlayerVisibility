# Infinite Player Visibility

Fabric mod MADE BY AI for Minecraft 1.21.11 that keeps players visible at very long distances without loading their chunks.
- As of 1.2.0 Entities can be rendered too (ticking entities)

*HOW IT WORK :

The server continues to synchronize player entities even outside the normal tracking range.
The client accepts rendering these players even if their chunk is not loaded.
Distant chunks are still not loaded.

*INSTALATION

Install the same JAR on both the server and the clients. The server side handles network tracking, and the client side removes rendering limits.
