package com.infiniteplayervisibility.client;

import com.infiniteplayervisibility.EntityVisibilityRules;
import com.infiniteplayervisibility.client.compat.VoxyCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ClientEntityVisibility {
	private ClientEntityVisibility() {
	}

	public static boolean shouldOverrideDistanceLimit(Entity entity) {
		return EntityVisibilityRules.shouldForceRemoteTracking(entity);
	}

	public static boolean shouldForceClientTick(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && !entity.isPlayer() && isWithinConfiguredVisibility(entity);
	}

	public static boolean shouldKeepClientTicking(Entity entity) {
		return shouldForceClientTick(entity) && !entity.isRemoved();
	}

	public static boolean shouldRenderEntity(Entity entity) {
		return shouldOverrideDistanceLimit(entity) && isWithinConfiguredVisibility(entity) && VoxyCompat.shouldRenderEntity(entity);
	}

	public static boolean hasRenderableEntityAt(ClientWorld world, BlockPos pos) {
		for (Entity entity : world.getEntities()) {
			if (!entity.isRemoved() && entity.getBlockPos().equals(pos) && shouldRenderEntity(entity)) {
				return true;
			}
		}

		return false;
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
}
