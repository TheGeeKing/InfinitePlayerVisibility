package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.EntityVisibilityRules;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
abstract class ServerChunkLoadingManagerEntityTrackerMixin {
	@Shadow
	@Final
	private Entity entity;

	@Redirect(
		method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
		at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")
	)
	private int infinitePlayerVisibility$removeDistanceCap(int trackedDistance, int watchedDistance) {
		return EntityVisibilityRules.shouldForceServerTracking(this.entity)
			? EntityVisibilityRules.getConfiguredTrackingDistanceBlocks(this.entity)
			: Math.min(trackedDistance, watchedDistance);
	}

	@Redirect(
		method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;isTracked(Lnet/minecraft/server/network/ServerPlayerEntity;II)Z"
		)
	)
	private boolean infinitePlayerVisibility$ignoreChunkVisibility(
		ServerChunkLoadingManager manager,
		net.minecraft.server.network.ServerPlayerEntity player,
		int chunkX,
		int chunkZ
	) {
		return EntityVisibilityRules.shouldForceServerTracking(this.entity) || manager.isTracked(player, chunkX, chunkZ);
	}
}
