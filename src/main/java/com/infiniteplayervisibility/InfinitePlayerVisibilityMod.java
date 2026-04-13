package com.infiniteplayervisibility;

import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import com.infiniteplayervisibility.test.SoloVisibilityTestContent;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InfinitePlayerVisibilityMod implements ModInitializer {
	public static final String MOD_ID = "infinite_player_visibility";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		InfinitePlayerVisibilityConfigManager.load();
		SoloVisibilityTestContent.initialize();
		LOGGER.info("Infinite player visibility is active.");
	}
}
