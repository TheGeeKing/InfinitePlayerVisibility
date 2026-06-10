package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.network.ClientVisibilityDistancePreferences;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

final class ForcedTrackingPlayerDistances {
	private final Reference2IntOpenHashMap<ServerPlayer> distanceBlocksByPlayer;
	private final Reference2LongOpenHashMap<ServerPlayer> distanceBlocksSquaredByPlayer;

	private ForcedTrackingPlayerDistances(int playerCount) {
		this.distanceBlocksByPlayer = new Reference2IntOpenHashMap<>(playerCount);
		this.distanceBlocksSquaredByPlayer = new Reference2LongOpenHashMap<>(playerCount);
	}

	static ForcedTrackingPlayerDistances create(List<ServerPlayer> players, int serverMaximumDistanceBlocks) {
		ForcedTrackingPlayerDistances distances = new ForcedTrackingPlayerDistances(players.size());
		for (ServerPlayer player : players) {
			int distanceBlocks = ClientVisibilityDistancePreferences.get(player, serverMaximumDistanceBlocks);
			distances.distanceBlocksByPlayer.put(player, distanceBlocks);
			distances.distanceBlocksSquaredByPlayer.put(player, squareDistanceBlocks(distanceBlocks));
		}
		return distances;
	}

	int getDistanceBlocks(ServerPlayer player) {
		return this.distanceBlocksByPlayer.getInt(player);
	}

	long getDistanceBlocksSquared(ServerPlayer player) {
		return this.distanceBlocksSquaredByPlayer.getLong(player);
	}

	private static long squareDistanceBlocks(int distanceBlocks) {
		if (distanceBlocks >= EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS) {
			return Long.MAX_VALUE;
		}

		return (long)distanceBlocks * distanceBlocks;
	}
}
