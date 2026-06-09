package com.infiniteplayervisibility.client;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.client.compat.VoxyCompat;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.phys.Vec3;

public final class ClientEntityVisibility {
	private static final LongSet RENDERABLE_ENTITY_BLOCK_POSITIONS = new LongOpenHashSet();
	private static ClientLevel cachedRenderableEntityLevel;
	private static long cachedRenderableEntityGameTime = Long.MIN_VALUE;
	private static long cachedCameraBlockPos = Long.MIN_VALUE;

	private ClientEntityVisibility() {
	}

	public static boolean shouldOverrideDistanceLimit(Entity entity) {
		return EntityVisibilityRules.shouldForceRemoteTracking(entity);
	}

	public static boolean shouldForceClientTick(Entity entity) {
		return shouldOverrideDistanceLimit(entity)
			&& canBeForceTickedOnClient(entity)
			&& isWithinConfiguredVisibility(entity)
			&& isOutsideLoadedClientChunks(entity);
	}

	public static boolean shouldKeepClientTicking(Entity entity) {
		return shouldForceClientTick(entity) && !entity.isRemoved();
	}

	public static boolean shouldOverrideVanillaRendering(Entity entity) {
		return shouldOverrideDistanceLimit(entity)
			&& isWithinConfiguredVisibility(entity)
			&& isOutsideLoadedClientChunks(entity);
	}

	public static boolean shouldOverrideVanillaDistanceLimit(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && isWithinConfiguredVisibility(entity);
	}

	public static boolean shouldRenderEntity(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && isWithinConfiguredVisibility(entity) && VoxyCompat.shouldRenderEntity(entity);
	}

	public static boolean shouldAssumeWaterState(Entity entity) {
		return shouldForceClientTick(entity)
			&& isAquaticEntity(entity)
			&& !entity.isRemoved();
	}

	public static boolean hasRenderableEntityAt(ClientLevel world, BlockPos pos) {
		refreshRenderableEntityPositionCache(world);
		return RENDERABLE_ENTITY_BLOCK_POSITIONS.contains(pos.asLong());
	}

	public static void invalidateRenderableEntityPositionCache() {
		cachedRenderableEntityLevel = null;
		cachedRenderableEntityGameTime = Long.MIN_VALUE;
		cachedCameraBlockPos = Long.MIN_VALUE;
		RENDERABLE_ENTITY_BLOCK_POSITIONS.clear();
	}

	private static void refreshRenderableEntityPositionCache(ClientLevel world) {
		long worldTime = world.getGameTime();
		long cameraPos = getCameraBlockPos();
		if (isRenderableEntityCacheFresh(world, worldTime, cameraPos)) {
			return;
		}

		RENDERABLE_ENTITY_BLOCK_POSITIONS.clear();
		for (Entity entity : world.entitiesForRendering()) {
			if (shouldIndexRenderableEntity(entity)) {
				RENDERABLE_ENTITY_BLOCK_POSITIONS.add(entity.blockPosition().asLong());
			}
		}

		cachedRenderableEntityLevel = world;
		cachedRenderableEntityGameTime = worldTime;
		cachedCameraBlockPos = cameraPos;
	}

	private static boolean isRenderableEntityCacheFresh(ClientLevel world, long worldTime, long cameraPos) {
		return world == cachedRenderableEntityLevel
			&& worldTime == cachedRenderableEntityGameTime
			&& cameraPos == cachedCameraBlockPos;
	}

	private static long getCameraBlockPos() {
		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer == null || client.gameRenderer.getMainCamera() == null) {
			return Long.MIN_VALUE;
		}

		return BlockPos.containing(client.gameRenderer.getMainCamera().position()).asLong();
	}

	private static boolean shouldIndexRenderableEntity(Entity entity) {
		return !entity.isRemoved() && shouldRenderEntity(entity);
	}

	private static boolean isWithinConfiguredVisibility(Entity entity) {
		int visibilityDistance = EntityVisibilityRules.getConfiguredTrackingDistanceBlocks(entity);
		if (visibilityDistance >= EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS) {
			return true;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer == null || client.gameRenderer.getMainCamera() == null) {
			return true;
		}

		Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
		double maxDistance = visibilityDistance;
		return entity.distanceToSqr(cameraPos.x, cameraPos.y, cameraPos.z) <= maxDistance * maxDistance;
	}

	private static boolean canBeForceTickedOnClient(Entity entity) {
		return !entity.isAlwaysTicking();
	}

	private static boolean isOutsideLoadedClientChunks(Entity entity) {
		return entity.level().isClientSide() && !entity.level().isLoaded(entity.blockPosition());
	}

	private static boolean isAquaticEntity(Entity entity) {
		return entity instanceof AbstractFish || entity instanceof WaterAnimal || entity instanceof Axolotl;
	}
}
