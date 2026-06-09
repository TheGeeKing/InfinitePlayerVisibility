package com.infiniteplayervisibility.network;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class ClientVisibilityDistancePreferences {
	private static final Object2IntOpenHashMap<UUID> VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER = new Object2IntOpenHashMap<>();

	static {
		VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.defaultReturnValue(EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS);
	}

	private ClientVisibilityDistancePreferences() {
	}

	public static void set(ServerPlayer player, int visibilityDistanceBlocks) {
		VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.put(player.getUUID(), InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks(visibilityDistanceBlocks));
	}

	public static void remove(ServerPlayer player) {
		VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.removeInt(player.getUUID());
	}

	public static int get(@Nullable ServerPlayer player) {
		if (player == null) {
			return EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS;
		}

		return VISIBILITY_DISTANCE_BLOCKS_BY_PLAYER.getInt(player.getUUID());
	}
}
