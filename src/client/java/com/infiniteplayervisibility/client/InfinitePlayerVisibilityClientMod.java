package com.infiniteplayervisibility.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class InfinitePlayerVisibilityClientMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientVisibilityDistanceReporter.sendCurrentDistance());
		ClientTickEvents.END_CLIENT_TICK.register(client -> ClientVisibilityDistanceReporter.sendIfChanged());
	}
}
