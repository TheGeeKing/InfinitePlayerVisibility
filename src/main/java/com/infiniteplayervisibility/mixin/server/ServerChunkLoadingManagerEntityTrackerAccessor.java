package com.infiniteplayervisibility.mixin.server;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
interface ServerChunkLoadingManagerEntityTrackerAccessor {
	@Accessor("entity")
	Entity infinitePlayerVisibility$getEntity();

	@Invoker("updateTrackedStatus")
	void infinitePlayerVisibility$updateTrackedStatus(ServerPlayerEntity player);
}
