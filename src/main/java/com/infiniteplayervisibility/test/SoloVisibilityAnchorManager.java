package com.infiniteplayervisibility.test;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class SoloVisibilityAnchorManager {
	public static final int ANCHOR_RADIUS_BLOCKS = 128;
	private static final int ANCHOR_CHUNK_RADIUS = (ANCHOR_RADIUS_BLOCKS + 15) / 16;
	private static final Map<ServerLevel, WorldAnchors> WORLD_ANCHORS = new WeakHashMap<>();

	private SoloVisibilityAnchorManager() {
	}

	public static void register(ServerLevel level, BlockPos pos) {
		WORLD_ANCHORS.computeIfAbsent(level, WorldAnchors::new).register(pos);
	}

	public static void unregister(ServerLevel level, BlockPos pos) {
		WorldAnchors anchors = WORLD_ANCHORS.get(level);
		if (anchors == null) {
			return;
		}

		anchors.unregister(pos);
		if (anchors.isEmpty()) {
			WORLD_ANCHORS.remove(level);
		}
	}

	public static boolean keepsEntitiesTicking(ServerLevel level, BlockPos pos) {
		WorldAnchors anchors = WORLD_ANCHORS.get(level);
		return anchors != null && anchors.keepsEntitiesTicking(pos);
	}

	private static final class WorldAnchors {
		private final ServerLevel level;
		private final Long2IntOpenHashMap anchorCounts = new Long2IntOpenHashMap();
		private final Long2IntOpenHashMap forcedChunkCounts = new Long2IntOpenHashMap();

		private WorldAnchors(ServerLevel level) {
			this.level = level;
			this.anchorCounts.defaultReturnValue(0);
			this.forcedChunkCounts.defaultReturnValue(0);
		}

		private void register(BlockPos pos) {
			long anchorKey = pos.asLong();
			int newAnchorCount = this.anchorCounts.addTo(anchorKey, 1) + 1;
			if (newAnchorCount > 1) {
				return;
			}

			this.updateForcedChunks(pos, true);
		}

		private void unregister(BlockPos pos) {
			long anchorKey = pos.asLong();
			int currentAnchorCount = this.anchorCounts.get(anchorKey);
			if (currentAnchorCount <= 0) {
				return;
			}

			if (currentAnchorCount == 1) {
				this.anchorCounts.remove(anchorKey);
				this.updateForcedChunks(pos, false);
				return;
			}

			this.anchorCounts.put(anchorKey, currentAnchorCount - 1);
		}

		private boolean keepsEntitiesTicking(BlockPos pos) {
			long chunkKey = ChunkPos.pack(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
			return this.forcedChunkCounts.containsKey(chunkKey);
		}

		private boolean isEmpty() {
			return this.anchorCounts.isEmpty();
		}

		private void updateForcedChunks(BlockPos pos, boolean register) {
			int centerChunkX = SectionPos.blockToSectionCoord(pos.getX());
			int centerChunkZ = SectionPos.blockToSectionCoord(pos.getZ());

			for (int chunkX = centerChunkX - ANCHOR_CHUNK_RADIUS; chunkX <= centerChunkX + ANCHOR_CHUNK_RADIUS; chunkX++) {
				for (int chunkZ = centerChunkZ - ANCHOR_CHUNK_RADIUS; chunkZ <= centerChunkZ + ANCHOR_CHUNK_RADIUS; chunkZ++) {
					this.updateForcedChunk(chunkX, chunkZ, register);
				}
			}
		}

		private void updateForcedChunk(int chunkX, int chunkZ, boolean register) {
			long chunkKey = ChunkPos.pack(chunkX, chunkZ);
			int currentCount = this.forcedChunkCounts.get(chunkKey);

			if (register) {
				if (currentCount == 0) {
					this.level.setChunkForced(chunkX, chunkZ, true);
				}
				this.forcedChunkCounts.put(chunkKey, currentCount + 1);
				return;
			}

			if (currentCount <= 1) {
				this.forcedChunkCounts.remove(chunkKey);
				this.level.setChunkForced(chunkX, chunkZ, false);
				return;
			}

			this.forcedChunkCounts.put(chunkKey, currentCount - 1);
		}
	}
}
