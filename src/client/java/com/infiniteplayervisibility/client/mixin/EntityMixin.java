package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Inject(method = "shouldRender(D)Z", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$allowRemoteEntityRendering(double distance, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = (Entity)(Object)this;
		if (ClientEntityVisibility.shouldOverrideDistanceLimit(entity)) {
			cir.setReturnValue(ClientEntityVisibility.shouldRenderEntity(entity));
		}
	}

	@Inject(method = "isTouchingWater()Z", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeWaterForRemoteAquaticEntities(CallbackInfoReturnable<Boolean> cir) {
		if (ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isSubmergedInWater()Z", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeSubmergedWaterForRemoteAquaticEntities(CallbackInfoReturnable<Boolean> cir) {
		if (ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isSubmergedIn", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeSubmergedWaterTagForRemoteAquaticEntities(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
		if (fluidTag == FluidTags.WATER && ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}
}
