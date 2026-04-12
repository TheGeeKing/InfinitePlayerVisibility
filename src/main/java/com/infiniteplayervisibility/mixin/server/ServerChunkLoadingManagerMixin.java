package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.EntityVisibilityRules;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
abstract class ServerChunkLoadingManagerMixin {
	@Shadow
	@Final
	private Int2ObjectMap<?> entityTrackers;

	@Shadow
	@Final
	ServerWorld world;

	@Inject(method = "tickEntityMovement", at = @At("TAIL"))
	private void infinitePlayerVisibility$refreshForcedTrackers(CallbackInfo ci) {
		if ((this.world.getTime() & 3L) != 0L) {
			return;
		}

		if (this.world.getPlayers().isEmpty()) {
			return;
		}

		for (Object trackerObject : this.entityTrackers.values()) {
			ServerChunkLoadingManagerEntityTrackerAccessor tracker = (ServerChunkLoadingManagerEntityTrackerAccessor)trackerObject;
			Entity entity = tracker.infinitePlayerVisibility$getEntity();
			if (!EntityVisibilityRules.shouldForceServerTracking(entity)) {
				continue;
			}

			for (ServerPlayerEntity player : this.world.getPlayers()) {
				tracker.infinitePlayerVisibility$updateTrackedStatus(player);
			}
		}
	}
}
