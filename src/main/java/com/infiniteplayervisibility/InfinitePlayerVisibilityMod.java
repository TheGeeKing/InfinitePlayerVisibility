package com.infiniteplayervisibility;

import com.infiniteplayervisibility.anchor.SoloVisibilityAnchorContent;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InfinitePlayerVisibilityMod implements ModInitializer {
	public static final String MOD_ID = "infinite_player_visibility";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		InfinitePlayerVisibilityConfigManager.load();
		SoloVisibilityAnchorContent.initialize();
		LOGGER.info("Infinite player visibility is active.");
	}
}
