package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
abstract class EntityRenderManagerMixin {
	@Shadow
	public abstract <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity);

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void infinitePlayerVisibility$allowForcedEntitiesWithoutBypassingFrustum(
		Entity entity,
		Frustum frustum,
		double x,
		double y,
		double z,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!ClientEntityVisibility.shouldOverrideDistanceLimit(entity)) {
			return;
		}

		if (!ClientEntityVisibility.shouldRenderEntity(entity)) {
			cir.setReturnValue(false);
			return;
		}

		EntityRendererAccessor<Entity> renderer = (EntityRendererAccessor<Entity>)this.getRenderer(entity);
		if (!renderer.infinitePlayerVisibility$invokeCanBeCulled(entity)) {
			cir.setReturnValue(true);
			return;
		}

		Box box = renderer.infinitePlayerVisibility$invokeGetBoundingBox(entity).expand(0.5D);
		if (box.getAverageSideLength() == 0.0D) {
			box = new Box(
				entity.getX() - 2.0D,
				entity.getY() - 2.0D,
				entity.getZ() - 2.0D,
				entity.getX() + 2.0D,
				entity.getY() + 2.0D,
				entity.getZ() + 2.0D
			);
		}

		cir.setReturnValue(frustum.isVisible(box));
	}
}
