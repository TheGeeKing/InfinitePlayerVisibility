package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
abstract class ServerChunkLoadingManagerMixin {
	@Shadow
	@Final
	private Int2ObjectMap<?> entityMap;

	@Shadow
	@Final
	ServerLevel level;

	@Inject(method = "tick()V", at = @At("TAIL"))
	private void infinitePlayerVisibility$refreshForcedTrackers(CallbackInfo ci) {
		InfinitePlayerVisibilityConfig config = InfinitePlayerVisibilityConfigManager.getConfig();
		if (!shouldRefreshForcedTrackers(config)) {
			return;
		}

		if (this.level.getGameTime() % config.remoteEntityTrackingIntervalTicks() != 0L) {
			return;
		}

		List<ServerPlayer> players = this.level.players();
		int configuredTrackingDistanceBlocks = config.visibilityDistanceBlocks();
		ForcedTrackingPlayerDistances playerDistances = ForcedTrackingPlayerDistances.create(players, configuredTrackingDistanceBlocks);
		int refreshedPlayers = 0;
		int refreshedEntities = 0;
		int maxRefreshedPlayers = config.maxTrackedRemotePlayersPerRefresh();
		int maxRefreshedEntities = InfinitePlayerVisibilityConfig.resolveRemoteEntityRefreshCap(
			players.size(),
			config.maxTrackedRemoteEntitiesPerRefresh(),
			config.remoteEntitiesPerPlayerRefreshRatio()
		);
		boolean loggedPlayerCap = false;
		boolean loggedEntityCap = false;
		for (Object trackerObject : this.entityMap.values()) {
			ServerChunkLoadingManagerEntityTrackerAccessor tracker = (ServerChunkLoadingManagerEntityTrackerAccessor)trackerObject;
			Entity entity = tracker.infinitePlayerVisibility$getEntity();
			if (!shouldForceServerTracking(entity, config)) {
				continue;
			}

			boolean playerEntity = entity instanceof Player;
			if (reachedRefreshCap(playerEntity, refreshedPlayers, refreshedEntities, maxRefreshedPlayers, maxRefreshedEntities)) {
				if (playerEntity && !loggedPlayerCap) {
					logRefreshCap(true, maxRefreshedPlayers);
					loggedPlayerCap = true;
				} else if (!playerEntity && !loggedEntityCap) {
					logRefreshCap(false, maxRefreshedEntities);
					loggedEntityCap = true;
				}
				continue;
			}

			try {
				ForcedTrackingRefreshContext.set(entity, playerDistances);
				for (ServerPlayer player : players) {
					if (isWithinClientTrackingDistance(entity, player, playerDistances)) {
						tracker.infinitePlayerVisibility$updateTrackedStatus(player);
					}
				}
			} finally {
				ForcedTrackingRefreshContext.clear();
			}
			if (playerEntity) {
				refreshedPlayers++;
			} else {
				refreshedEntities++;
			}
		}
	}

	private boolean shouldRefreshForcedTrackers(InfinitePlayerVisibilityConfig config) {
		return config.enabled()
			&& (config.renderRemotePlayers() || config.renderRemoteEntities())
			&& !this.level.players().isEmpty();
	}

	private boolean shouldForceServerTracking(Entity entity, InfinitePlayerVisibilityConfig config) {
		if (entity.isRemoved() || !config.enabled()) {
			return false;
		}

		if (entity instanceof Player) {
			return config.renderRemotePlayers();
		}

		return config.renderRemoteEntities() && this.level.isPositionEntityTicking(entity.blockPosition());
	}

	private static boolean isWithinClientTrackingDistance(
		Entity entity,
		ServerPlayer player,
		ForcedTrackingPlayerDistances playerDistances
	) {
		int distanceBlocks = playerDistances.getDistanceBlocks(player);
		if (distanceBlocks >= EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS) {
			return true;
		}

		return entity.distanceToSqr(player) <= playerDistances.getDistanceBlocksSquared(player);
	}

	private static boolean reachedRefreshCap(
		boolean playerEntity,
		int refreshedPlayers,
		int refreshedEntities,
		int maxRefreshedPlayers,
		int maxRefreshedEntities
	) {
		if (playerEntity) {
			return isLimited(maxRefreshedPlayers) && refreshedPlayers >= maxRefreshedPlayers;
		}

		return isLimited(maxRefreshedEntities) && refreshedEntities >= maxRefreshedEntities;
	}

	private void logRefreshCap(boolean playerEntity, int cap) {
		InfinitePlayerVisibilityMod.LOGGER.debug(
			"Skipped forced tracker refreshes in dimension {} after reaching configured cap of {} {}.",
			this.level.dimension(),
			cap,
			playerEntity ? "players" : "entities"
		);
	}

	private static boolean isLimited(int cap) {
		return cap > InfinitePlayerVisibilityConfig.UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;
	}
}
