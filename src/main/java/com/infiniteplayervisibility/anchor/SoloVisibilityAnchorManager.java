package com.infiniteplayervisibility.anchor;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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
		private final LongSet anchors = new LongOpenHashSet();
		private final Long2IntOpenHashMap forcedChunkCounts = new Long2IntOpenHashMap();

		private WorldAnchors(ServerLevel level) {
			this.level = level;
			this.forcedChunkCounts.defaultReturnValue(0);
		}

		private void register(BlockPos pos) {
			if (!this.anchors.add(pos.asLong())) {
				return;
			}

			this.logAnchorForcedChunk(pos, "registered");
			this.updateForcedChunk(pos, true);
		}

		private void unregister(BlockPos pos) {
			if (!this.anchors.remove(pos.asLong())) {
				return;
			}

			this.updateForcedChunk(pos, false);
		}

		private boolean keepsEntitiesTicking(BlockPos pos) {
			long chunkKey = ChunkPos.pack(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
			return this.forcedChunkCounts.containsKey(chunkKey);
		}

		private boolean isEmpty() {
			return this.anchors.isEmpty();
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
