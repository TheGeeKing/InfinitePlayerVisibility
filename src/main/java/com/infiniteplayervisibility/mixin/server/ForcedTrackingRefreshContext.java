package com.infiniteplayervisibility.mixin.server;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

final class ForcedTrackingRefreshContext {
	private static final ThreadLocal<ForcedTrackingRefreshContext> CURRENT = new ThreadLocal<>();

	private final Entity entity;
	private final int configuredTrackingDistanceBlocks;
	private final Reference2IntMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer;

	private ForcedTrackingRefreshContext(
		Entity entity,
		int configuredTrackingDistanceBlocks,
		Reference2IntMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer
	) {
		this.entity = entity;
		this.configuredTrackingDistanceBlocks = configuredTrackingDistanceBlocks;
		this.clientTrackingDistanceBlocksByPlayer = clientTrackingDistanceBlocksByPlayer;
	}

	static void set(
		Entity entity,
		int configuredTrackingDistanceBlocks,
		Reference2IntMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer
	) {
		CURRENT.set(new ForcedTrackingRefreshContext(entity, configuredTrackingDistanceBlocks, clientTrackingDistanceBlocksByPlayer));
	}

	static void clear() {
		CURRENT.remove();
	}

	static boolean isRefreshing(Entity entity) {
		ForcedTrackingRefreshContext context = CURRENT.get();
		return context != null && context.entity == entity;
	}

	static int getTrackingDistanceBlocks(Entity entity, ServerPlayer player) {
		ForcedTrackingRefreshContext context = CURRENT.get();
		if (context == null || context.entity != entity) {
			return -1;
		}

		return Math.min(
			context.configuredTrackingDistanceBlocks,
			context.clientTrackingDistanceBlocksByPlayer.getInt(player)
		);
	}
}
