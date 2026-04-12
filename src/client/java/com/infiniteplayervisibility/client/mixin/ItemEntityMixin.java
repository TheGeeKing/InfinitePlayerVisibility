package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {
	@Shadow
	private int itemAge;

	@Shadow
	private int pickupDelay;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$skipRemoteItemPhysics(CallbackInfo ci) {
		ItemEntity entity = (ItemEntity)(Object)this;
		if (!entity.getEntityWorld().isClient() || !ClientEntityVisibility.shouldForceClientTick(entity)) {
			return;
		}

		if (entity.getStack().isEmpty()) {
			entity.discard();
			ci.cancel();
			return;
		}

		entity.baseTick();
		if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
			this.pickupDelay--;
		}

		if (this.itemAge != -32768) {
			this.itemAge++;
		}

		ci.cancel();
	}
}
