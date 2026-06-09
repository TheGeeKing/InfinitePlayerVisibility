package com.infiniteplayervisibility.client;

import com.infiniteplayervisibility.client.compat.VoxyCompat;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import com.infiniteplayervisibility.network.ClientVisibilityDistancePayload;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientVisibilityDistanceReporter {
	private static int lastReportedDistanceBlocks = Integer.MIN_VALUE;

	private ClientVisibilityDistanceReporter() {
	}

	public static void sendCurrentDistance() {
		lastReportedDistanceBlocks = Integer.MIN_VALUE;
		sendIfChanged();
	}

	public static void sendIfChanged() {
		if (!ClientPlayNetworking.canSend(ClientVisibilityDistancePayload.TYPE)) {
			return;
		}

		int effectiveVisibilityDistanceBlocks = getEffectiveVisibilityDistanceBlocks();
		if (effectiveVisibilityDistanceBlocks == lastReportedDistanceBlocks) {
			return;
		}

		lastReportedDistanceBlocks = effectiveVisibilityDistanceBlocks;
		ClientPlayNetworking.send(new ClientVisibilityDistancePayload(effectiveVisibilityDistanceBlocks));
	}

	public static int getEffectiveVisibilityDistanceBlocks() {
		InfinitePlayerVisibilityConfig config = InfinitePlayerVisibilityConfigManager.getConfig();
		int distanceBlocks = config.visibilityDistanceBlocks();
		if (config.remoteRenderDistanceMode() != InfinitePlayerVisibilityConfig.RemoteRenderDistanceMode.TERRAIN_CONTEXT) {
			return distanceBlocks;
		}

		OptionalInt voxyDistanceBlocks = VoxyCompat.getConfiguredRenderDistanceBlocks();
		return Math.min(distanceBlocks, voxyDistanceBlocks.orElse(getVanillaRenderDistanceBlocks()));
	}

	private static int getVanillaRenderDistanceBlocks() {
		Minecraft client = Minecraft.getInstance();
		if (client.options == null) {
			return InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS;
		}

		return client.options.renderDistance().get() * 16;
	}
}
