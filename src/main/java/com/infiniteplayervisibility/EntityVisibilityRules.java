package com.infiniteplayervisibility;

import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class EntityVisibilityRules {
	public static final int INFINITE_TRACKING_DISTANCE_BLOCKS = 30000000;

	private EntityVisibilityRules() {
	}

	public static boolean shouldForceRemoteTracking(Entity entity) {
		return shouldForceRemoteTracking(entity, InfinitePlayerVisibilityConfigManager.getConfig());
	}

	public static int getConfiguredTrackingDistanceBlocks(Entity entity) {
		InfinitePlayerVisibilityConfig config = InfinitePlayerVisibilityConfigManager.getConfig();
		return shouldForceRemoteTracking(entity, config) ? config.visibilityDistanceBlocks() : 0;
	}

	public static boolean shouldForceServerTracking(Entity entity) {
		if (!shouldForceRemoteTracking(entity)) {
			return false;
		}

		if (usesPlayerVisibilityRule(entity)) {
			return true;
		}

		return entity.level() instanceof ServerLevel serverWorld && serverWorld.isPositionEntityTicking(entity.blockPosition());
	}

	private static boolean shouldForceRemoteTracking(Entity entity, InfinitePlayerVisibilityConfig config) {
		if (entity.isRemoved()) {
			return false;
		}

		if (!config.enabled()) {
			return false;
		}

		return usesPlayerVisibilityRule(entity) ? config.renderRemotePlayers() : config.renderRemoteEntities();
	}

	private static boolean usesPlayerVisibilityRule(Entity entity) {
		return entity instanceof Player;
	}
}
