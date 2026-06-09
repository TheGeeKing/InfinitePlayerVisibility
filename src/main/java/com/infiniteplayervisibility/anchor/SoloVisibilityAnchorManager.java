package com.infiniteplayervisibility.anchor;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class SoloVisibilityAnchorManager {
	private static final Map<ServerLevel, WorldAnchors> WORLD_ANCHORS = new WeakHashMap<>();

	private SoloVisibilityAnchorManager() {
	}

	public static void register(ServerLevel level, BlockPos pos, int radiusBlocks) {
		WORLD_ANCHORS.computeIfAbsent(level, WorldAnchors::new).register(pos, radiusBlocks);
	}

	public static void refresh(ServerLevel level, BlockPos pos, int radiusBlocks) {
		WORLD_ANCHORS.computeIfAbsent(level, WorldAnchors::new).refresh(pos, radiusBlocks);
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
		private final Long2IntOpenHashMap anchorRadii = new Long2IntOpenHashMap();
		private final Long2IntOpenHashMap forcedChunkCounts = new Long2IntOpenHashMap();

		private WorldAnchors(ServerLevel level) {
			this.level = level;
			this.anchorCounts.defaultReturnValue(0);
			this.anchorRadii.defaultReturnValue(InfinitePlayerVisibilityConfig.DEFAULT_ANCHOR_RADIUS_BLOCKS);
			this.forcedChunkCounts.defaultReturnValue(0);
		}

		private void register(BlockPos pos, int radiusBlocks) {
			int clampedRadiusBlocks = InfinitePlayerVisibilityConfig.clampAnchorRadiusBlocks(radiusBlocks);
			long anchorKey = pos.asLong();
			int newAnchorCount = this.anchorCounts.addTo(anchorKey, 1) + 1;
			if (newAnchorCount > 1) {
				return;
			}

			this.anchorRadii.put(anchorKey, clampedRadiusBlocks);
			this.logAnchorForcedChunks(pos, clampedRadiusBlocks, "registered");
			this.updateForcedChunks(pos, clampedRadiusBlocks, true);
		}

		private void refresh(BlockPos pos, int radiusBlocks) {
			long anchorKey = pos.asLong();
			if (!this.anchorCounts.containsKey(anchorKey)) {
				this.register(pos, radiusBlocks);
				return;
			}

			int clampedRadiusBlocks = InfinitePlayerVisibilityConfig.clampAnchorRadiusBlocks(radiusBlocks);
			int currentRadiusBlocks = this.anchorRadii.get(anchorKey);
			if (currentRadiusBlocks == clampedRadiusBlocks) {
				return;
			}

			this.updateForcedChunks(pos, currentRadiusBlocks, false);
			this.anchorRadii.put(anchorKey, clampedRadiusBlocks);
			this.logAnchorForcedChunks(pos, clampedRadiusBlocks, "updated");
			this.updateForcedChunks(pos, clampedRadiusBlocks, true);
		}

		private void unregister(BlockPos pos) {
			long anchorKey = pos.asLong();
			int currentAnchorCount = this.anchorCounts.get(anchorKey);
			if (currentAnchorCount <= 0) {
				return;
			}

			if (currentAnchorCount == 1) {
				this.anchorCounts.remove(anchorKey);
				int radiusBlocks = this.anchorRadii.remove(anchorKey);
				this.updateForcedChunks(pos, radiusBlocks, false);
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

		private void updateForcedChunks(BlockPos pos, int radiusBlocks, boolean register) {
			int centerChunkX = SectionPos.blockToSectionCoord(pos.getX());
			int centerChunkZ = SectionPos.blockToSectionCoord(pos.getZ());
			int chunkRadius = getChunkRadius(radiusBlocks);

			for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
				for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
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

		private void logAnchorForcedChunks(BlockPos pos, int radiusBlocks, String action) {
			int chunkRadius = getChunkRadius(radiusBlocks);
			int chunkDiameter = chunkRadius * 2 + 1;
			InfinitePlayerVisibilityMod.LOGGER.warn(
				"Solo visibility anchor {} force-loading chunks in dimension {} at {} with radius {} blocks ({} chunks).",
				action,
				this.level.dimension(),
				pos,
				radiusBlocks,
				chunkDiameter * chunkDiameter
			);
		}

		private static int getChunkRadius(int radiusBlocks) {
			return (InfinitePlayerVisibilityConfig.clampAnchorRadiusBlocks(radiusBlocks) + 15) / 16;
		}
	}
}
