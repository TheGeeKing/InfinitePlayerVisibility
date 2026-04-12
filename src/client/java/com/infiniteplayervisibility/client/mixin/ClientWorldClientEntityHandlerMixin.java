package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.world.ClientWorld$ClientEntityHandler")
abstract class ClientWorldClientEntityHandlerMixin {
	@Inject(method = "stopTicking", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$keepForcedEntitiesTicking(Entity entity, CallbackInfo ci) {
		if (ClientEntityVisibility.shouldKeepClientTicking(entity)) {
			ci.cancel();
		}
	}
}
