package com.infiniteplayervisibility.tracking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class ForcedTrackingRefreshContext {
	private static final ThreadLocal<ForcedTrackingRefreshContext> CURRENT = new ThreadLocal<>();

	private final Entity entity;
	private final ForcedTrackingPlayerDistances playerDistances;

	private ForcedTrackingRefreshContext(
		Entity entity,
		ForcedTrackingPlayerDistances playerDistances
	) {
		this.entity = entity;
		this.playerDistances = playerDistances;
	}

	public static void set(
		Entity entity,
		ForcedTrackingPlayerDistances playerDistances
	) {
		CURRENT.set(new ForcedTrackingRefreshContext(entity, playerDistances));
	}

	public static void clear() {
		CURRENT.remove();
	}

	public static boolean isRefreshing(Entity entity) {
		ForcedTrackingRefreshContext context = CURRENT.get();
		return context != null && context.entity == entity;
	}

	public static int getTrackingDistanceBlocks(Entity entity, ServerPlayer player) {
		ForcedTrackingRefreshContext context = CURRENT.get();
		if (context == null || context.entity != entity) {
			return -1;
		}

		return context.playerDistances.getDistanceBlocks(player);
	}
}
