package com.infiniteplayervisibility.network;

import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class ClientVisibilityDistancePreferences {
	private static final Object2IntOpenHashMap<UUID> VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER = new Object2IntOpenHashMap<>();

	private ClientVisibilityDistancePreferences() {
	}

	public static void set(ServerPlayer player, int visibilityDistanceBlocks) {
		VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.put(player.getUUID(), InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks(visibilityDistanceBlocks));
	}

	public static void remove(ServerPlayer player) {
		VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.removeInt(player.getUUID());
	}

	public static int get(@Nullable ServerPlayer player) {
		int serverMaximumDistanceBlocks = InfinitePlayerVisibilityConfigManager.getConfig().visibilityDistanceBlocks();
		return get(player, serverMaximumDistanceBlocks);
	}

	public static int get(@Nullable ServerPlayer player, int serverMaximumDistanceBlocks) {
		if (player == null) {
			return serverMaximumDistanceBlocks;
		}

		int clientDistanceBlocks = VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.containsKey(player.getUUID())
			? VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.getInt(player.getUUID())
			: serverMaximumDistanceBlocks;
		return Math.min(serverMaximumDistanceBlocks, clientDistanceBlocks);
	}
}
