package com.infiniteplayervisibility.client.mixin;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.EntityList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
abstract class ClientWorldMixin {
	@Shadow
	@Final
	private EntityList entityList;

	@Shadow
	@Nullable
	public abstract Entity getEntityById(int id);

	@Inject(method = "addEntity", at = @At("TAIL"))
	private void infinitePlayerVisibility$startForcedEntityTicking(Entity entity, CallbackInfo ci) {
		if (ClientEntityVisibility.shouldForceClientTick(entity) && !this.entityList.has(entity)) {
			this.entityList.add(entity);
		}
	}

	@Inject(method = "removeEntity", at = @At("HEAD"))
	private void infinitePlayerVisibility$stopForcedEntityTicking(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
		Entity entity = this.getEntityById(entityId);
		if (entity != null && ClientEntityVisibility.shouldForceClientTick(entity) && this.entityList.has(entity)) {
			this.entityList.remove(entity);
		}
	}
}
