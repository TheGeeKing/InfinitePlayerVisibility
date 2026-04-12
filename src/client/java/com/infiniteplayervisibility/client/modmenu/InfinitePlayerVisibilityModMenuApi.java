package com.infiniteplayervisibility.client.modmenu;

import com.infiniteplayervisibility.client.gui.InfinitePlayerVisibilityConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class InfinitePlayerVisibilityModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return InfinitePlayerVisibilityConfigScreen::new;
	}
}
