package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
abstract class EntityRenderManagerMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$allowForcedEntities(
		Entity entity,
		Frustum frustum,
		double x,
		double y,
		double z,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (ClientEntityVisibility.shouldOverrideDistanceLimit(entity)) {
			cir.setReturnValue(ClientEntityVisibility.shouldRenderEntity(entity));
		}
	}
}
