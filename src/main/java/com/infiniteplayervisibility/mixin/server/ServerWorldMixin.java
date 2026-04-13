package com.infiniteplayervisibility.mixin.server;

import com.infiniteplayervisibility.test.SoloVisibilityAnchorManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {
	@Inject(method = "shouldTickEntityAt", at = @At("RETURN"), cancellable = true)
	private void infinitePlayerVisibility$allowSoloAnchorEntityTicking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && SoloVisibilityAnchorManager.keepsEntitiesTicking((ServerWorld)(Object)this, pos)) {
			cir.setReturnValue(true);
		}
	}
}
