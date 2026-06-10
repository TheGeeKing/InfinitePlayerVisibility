package com.infiniteplayervisibility.anchor;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
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

			this.logAnchorForcedChunk(pos, "registered");
			this.updateForcedChunk(pos, true);
		}

		private void unregister(BlockPos pos) {
			long anchorKey = pos.asLong();
			int currentAnchorCount = this.anchorCounts.get(anchorKey);
			if (currentAnchorCount <= 0) {
				return;
			}

			if (currentAnchorCount == 1) {
				this.anchorCounts.remove(anchorKey);
				this.updateForcedChunk(pos, false);
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

		private void updateForcedChunk(BlockPos pos, boolean register) {
			this.updateForcedChunk(
				SectionPos.blockToSectionCoord(pos.getX()),
				SectionPos.blockToSectionCoord(pos.getZ()),
				register
			);
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

		private void logAnchorForcedChunk(BlockPos pos, String action) {
			InfinitePlayerVisibilityMod.LOGGER.warn(
				"Solo visibility anchor {} force-loading chunk in dimension {} at {}.",
				action,
				this.level.dimension(),
				pos
			);
		}
	}
}
