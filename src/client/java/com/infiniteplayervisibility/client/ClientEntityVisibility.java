package com.infiniteplayervisibility.client;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.client.compat.VoxyCompat;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.WaterAnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ClientEntityVisibility {
	private static final LongSet RENDERABLE_ENTITY_POSITIONS = new LongOpenHashSet();
	private static ClientWorld cachedRenderableEntityWorld;
	private static long cachedRenderableEntityWorldTime = Long.MIN_VALUE;
	private static long cachedRenderableEntityCameraPos = Long.MIN_VALUE;

	private ClientEntityVisibility() {
	}

	public static boolean shouldOverrideDistanceLimit(Entity entity) {
		return EntityVisibilityRules.shouldForceRemoteTracking(entity);
	}

	public static boolean shouldForceClientTick(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && !entity.isPlayer() && isWithinConfiguredVisibility(entity) && requiresForcedClientTick(entity);
	}

	public static boolean shouldKeepClientTicking(Entity entity) {
		return shouldForceClientTick(entity) && !entity.isRemoved();
	}

	public static boolean shouldRenderEntity(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && isWithinConfiguredVisibility(entity) && VoxyCompat.shouldRenderEntity(entity);
	}

	public static boolean shouldAssumeWaterState(Entity entity) {
		return shouldForceClientTick(entity)
			&& isAquaticEntity(entity)
			&& !entity.isRemoved();
	}

	public static boolean hasRenderableEntityAt(ClientWorld world, BlockPos pos) {
		refreshRenderableEntityPositionCache(world);
		return RENDERABLE_ENTITY_POSITIONS.contains(pos.asLong());
	}

	public static void invalidateRenderableEntityPositionCache() {
		cachedRenderableEntityWorld = null;
		cachedRenderableEntityWorldTime = Long.MIN_VALUE;
		cachedRenderableEntityCameraPos = Long.MIN_VALUE;
		RENDERABLE_ENTITY_POSITIONS.clear();
	}

	private static boolean isWithinConfiguredVisibility(Entity entity) {
		int visibilityDistance = EntityVisibilityRules.getConfiguredTrackingDistanceBlocks(entity);
		if (visibilityDistance >= EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS) {
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
			return true;
		}

		Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
		double maxDistance = visibilityDistance;
		return entity.squaredDistanceTo(cameraPos.x, cameraPos.y, cameraPos.z) <= maxDistance * maxDistance;
	}

	private static void refreshRenderableEntityPositionCache(ClientWorld world) {
		long worldTime = world.getTime();
		long cameraPos = getCameraBlockPos();
		if (world == cachedRenderableEntityWorld && worldTime == cachedRenderableEntityWorldTime && cameraPos == cachedRenderableEntityCameraPos) {
			return;
		}

		RENDERABLE_ENTITY_POSITIONS.clear();
		for (Entity entity : world.getEntities()) {
			if (shouldIndexRenderableEntity(entity)) {
				RENDERABLE_ENTITY_POSITIONS.add(entity.getBlockPos().asLong());
			}
		}

		cachedRenderableEntityWorld = world;
		cachedRenderableEntityWorldTime = worldTime;
		cachedRenderableEntityCameraPos = cameraPos;
	}

	private static long getCameraBlockPos() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
			return Long.MIN_VALUE;
		}

		return BlockPos.ofFloored(client.gameRenderer.getCamera().getCameraPos()).asLong();
	}

	private static boolean shouldIndexRenderableEntity(Entity entity) {
		return !entity.isRemoved() && shouldRenderEntity(entity);
	}

	private static boolean requiresForcedClientTick(Entity entity) {
		return entity.getEntityWorld().isClient() && !entity.getEntityWorld().isPosLoaded(entity.getBlockPos());
	}

	private static boolean isAquaticEntity(Entity entity) {
		return entity instanceof FishEntity || entity instanceof WaterAnimalEntity || entity instanceof AxolotlEntity;
	}
}
