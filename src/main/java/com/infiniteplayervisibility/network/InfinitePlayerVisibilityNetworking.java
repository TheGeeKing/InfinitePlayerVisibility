package com.infiniteplayervisibility.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class InfinitePlayerVisibilityNetworking {
	private InfinitePlayerVisibilityNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(ClientVisibilityDistancePayload.TYPE, ClientVisibilityDistancePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
			ClientVisibilityDistancePayload.TYPE,
			(payload, context) -> ClientVisibilityDistancePreferences.set(context.player(), payload.visibilityDistanceBlocks())
		);
		ServerPlayConnectionEvents.DISCONNECT.register(
			(handler, server) -> ClientVisibilityDistancePreferences.remove(handler.player)
		);
	}
}
