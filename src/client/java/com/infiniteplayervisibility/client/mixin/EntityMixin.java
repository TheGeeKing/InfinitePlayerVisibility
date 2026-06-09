package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Inject(method = "shouldRenderAtSqrDistance(D)Z", at = @At("RETURN"), cancellable = true)
	private void infinitePlayerVisibility$allowRemoteEntityRendering(double distance, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}

		Entity entity = (Entity)(Object)this;
		if (ClientEntityVisibility.shouldOverrideVanillaDistanceLimit(entity)) {
			cir.setReturnValue(ClientEntityVisibility.shouldRenderEntity(entity));
		}
	}

	@Inject(method = "isInWater()Z", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeWaterForRemoteAquaticEntities(CallbackInfoReturnable<Boolean> cir) {
		if (ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isUnderWater()Z", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeUnderWaterForRemoteAquaticEntities(CallbackInfoReturnable<Boolean> cir) {
		if (ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$assumeEyeInWaterForRemoteAquaticEntities(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
		if (fluidTag == FluidTags.WATER && ClientEntityVisibility.shouldAssumeWaterState((Entity)(Object)this)) {
			cir.setReturnValue(true);
		}
	}
}
