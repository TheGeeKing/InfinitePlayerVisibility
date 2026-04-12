package com.infiniteplayervisibility.client.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
interface EntityRendererAccessor<T extends Entity> {
	@Invoker("getBoundingBox")
	Box infinitePlayerVisibility$invokeGetBoundingBox(T entity);

	@Invoker("canBeCulled")
	boolean infinitePlayerVisibility$invokeCanBeCulled(T entity);
}
