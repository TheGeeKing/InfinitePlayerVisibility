package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import com.infiniteplayervisibility.network.ClientVisibilityDistancePreferences;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
		Reference2IntOpenHashMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer = getClientTrackingDistanceBlocksByPlayer(players);
		int configuredTrackingDistanceBlocks = config.visibilityDistanceBlocks();
		int refreshedEntities = 0;
		int maxRefreshedEntities = config.maxTrackedEntitiesPerRefresh();
		for (Object trackerObject : this.entityMap.values()) {
			if (maxRefreshedEntities > InfinitePlayerVisibilityConfig.UNLIMITED_TRACKED_ENTITIES_PER_REFRESH
				&& refreshedEntities >= maxRefreshedEntities) {
				InfinitePlayerVisibilityMod.LOGGER.debug(
					"Skipped forced tracker refreshes in dimension {} after reaching configured cap of {} entities.",
					this.level.dimension(),
					maxRefreshedEntities
				);
				return;
			}

			ServerChunkLoadingManagerEntityTrackerAccessor tracker = (ServerChunkLoadingManagerEntityTrackerAccessor)trackerObject;
			Entity entity = tracker.infinitePlayerVisibility$getEntity();
			if (!EntityVisibilityRules.shouldForceServerTracking(entity)) {
				continue;
			}

			try {
				ForcedTrackingRefreshContext.set(entity, configuredTrackingDistanceBlocks, clientTrackingDistanceBlocksByPlayer);
				for (ServerPlayer player : players) {
					if (isWithinClientTrackingDistance(entity, player, configuredTrackingDistanceBlocks, clientTrackingDistanceBlocksByPlayer)) {
						tracker.infinitePlayerVisibility$updateTrackedStatus(player);
					}
				}
			} finally {
				ForcedTrackingRefreshContext.clear();
			}
			refreshedEntities++;
		}
	}

	private boolean shouldRefreshForcedTrackers(InfinitePlayerVisibilityConfig config) {
		return config.enabled()
			&& (config.renderRemotePlayers() || config.renderRemoteEntities())
			&& !this.level.players().isEmpty();
	}

	private static Reference2IntOpenHashMap<ServerPlayer> getClientTrackingDistanceBlocksByPlayer(List<ServerPlayer> players) {
		Reference2IntOpenHashMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer = new Reference2IntOpenHashMap<>(players.size());
		for (ServerPlayer player : players) {
			clientTrackingDistanceBlocksByPlayer.put(player, ClientVisibilityDistancePreferences.get(player));
		}
		return clientTrackingDistanceBlocksByPlayer;
	}

	private static boolean isWithinClientTrackingDistance(
		Entity entity,
		ServerPlayer player,
		int configuredTrackingDistanceBlocks,
		Reference2IntOpenHashMap<ServerPlayer> clientTrackingDistanceBlocksByPlayer
	) {
		int distanceBlocks = Math.min(
			configuredTrackingDistanceBlocks,
			clientTrackingDistanceBlocksByPlayer.getInt(player)
		);
		if (distanceBlocks >= EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS) {
			return true;
		}

		double maxDistance = distanceBlocks;
		return entity.distanceToSqr(player) <= maxDistance * maxDistance;
	}
}
