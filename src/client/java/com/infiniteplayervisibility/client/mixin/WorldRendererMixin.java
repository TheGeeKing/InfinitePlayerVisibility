package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
	@Shadow
	@Final
	private ClientWorld world;

	@Inject(method = "isRenderingReady", at = @At("RETURN"), cancellable = true)
	private void infinitePlayerVisibility$allowRenderingWithoutChunk(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}

		if (ClientEntityVisibility.hasRenderableEntityAt(this.world, pos)) {
			cir.setReturnValue(true);
		}
	}
}
