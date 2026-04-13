package com.infiniteplayervisibility.client.gui;

import com.infiniteplayervisibility.client.ClientEntityVisibility;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Locale;

public final class InfinitePlayerVisibilityConfigScreen extends Screen {
	private static final Text TITLE = Text.translatable("screen.infinite_player_visibility.title");
	private static final Text SUBTITLE = Text.translatable("screen.infinite_player_visibility.subtitle");
	private static final int OPTIONS_WIDTH = 240;
	private static final int BUTTON_WIDTH = 115;

	private final Screen parent;
	private final InfinitePlayerVisibilityConfig workingCopy;

	public InfinitePlayerVisibilityConfigScreen(Screen parent) {
		super(TITLE);
		this.parent = parent;
		this.workingCopy = InfinitePlayerVisibilityConfigManager.getConfig().copy();
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int leftX = centerX - OPTIONS_WIDTH / 2;
		int y = 68;

		this.addDrawableChild(
			CyclingButtonWidget.onOffBuilder(this.workingCopy.enabled())
				.build(leftX, y, OPTIONS_WIDTH, 20, Text.translatable("option.infinite_player_visibility.enabled"), (button, value) -> {
					this.workingCopy.setEnabled(value);
				})
		);

		y += 24;
		this.addDrawableChild(
			CyclingButtonWidget.onOffBuilder(this.workingCopy.renderRemotePlayers())
				.build(leftX, y, OPTIONS_WIDTH, 20, Text.translatable("option.infinite_player_visibility.remote_players"), (button, value) -> {
					this.workingCopy.setRenderRemotePlayers(value);
				})
		);

		y += 24;
		this.addDrawableChild(
			CyclingButtonWidget.onOffBuilder(this.workingCopy.renderRemoteEntities())
				.build(leftX, y, OPTIONS_WIDTH, 20, Text.translatable("option.infinite_player_visibility.remote_entities"), (button, value) -> {
					this.workingCopy.setRenderRemoteEntities(value);
				})
		);

		y += 28;
		this.addDrawableChild(new VisibilityDistanceSlider(leftX, y, OPTIONS_WIDTH, 20, this.workingCopy.visibilityDistanceBlocks(), this.workingCopy));

		int bottomY = this.height - 28;
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.saveAndClose()).dimensions(centerX - BUTTON_WIDTH - 5, bottomY, BUTTON_WIDTH, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(centerX + 5, bottomY, BUTTON_WIDTH, 20).build());
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 18, 16777215);
		context.drawCenteredTextWithShadow(this.textRenderer, SUBTITLE, this.width / 2, 34, 11184810);
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	private void saveAndClose() {
		InfinitePlayerVisibilityConfigManager.setConfig(this.workingCopy);
		ClientEntityVisibility.invalidateRenderableEntityPositionCache();
		this.close();
	}

	private static double blocksToSliderValue(int blocks) {
		if (blocks >= InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS) {
			return 1.0D;
		}

		double minLog = Math.log(InfinitePlayerVisibilityConfig.MIN_VISIBILITY_DISTANCE_BLOCKS);
		double maxLog = Math.log(InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS);
		double clamped = Math.log(InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks(blocks));
		return (clamped - minLog) / (maxLog - minLog);
	}

	private static int sliderValueToBlocks(double value) {
		if (value >= 0.999D) {
			return InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS;
		}

		double minLog = Math.log(InfinitePlayerVisibilityConfig.MIN_VISIBILITY_DISTANCE_BLOCKS);
		double maxLog = Math.log(InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS);
		double interpolated = Math.exp(minLog + value * (maxLog - minLog));
		return InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks((int)Math.round(interpolated));
	}

	private static Text formatDistanceText(int blocks) {
		if (blocks >= InfinitePlayerVisibilityConfig.MAX_VISIBILITY_DISTANCE_BLOCKS) {
			return Text.translatable("option.infinite_player_visibility.visibility_distance.infinite");
		}

		int chunks = Math.max(1, (int)Math.ceil(blocks / 16.0D));
		return Text.translatable(
			"option.infinite_player_visibility.visibility_distance.value",
			String.format(Locale.ROOT, "%,d", chunks),
			String.format(Locale.ROOT, "%,d", blocks)
		);
	}

	private static final class VisibilityDistanceSlider extends SliderWidget {
		private final InfinitePlayerVisibilityConfig config;
		private int blocks;

		private VisibilityDistanceSlider(int x, int y, int width, int height, int initialBlocks, InfinitePlayerVisibilityConfig config) {
			super(x, y, width, height, ScreenTexts.EMPTY, blocksToSliderValue(initialBlocks));
			this.config = config;
			this.blocks = InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks(initialBlocks);
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(
				Text.translatable("option.infinite_player_visibility.visibility_distance", formatDistanceText(this.blocks))
			);
		}

		@Override
		protected void applyValue() {
			this.blocks = sliderValueToBlocks(this.value);
			this.config.setVisibilityDistanceBlocks(this.blocks);
		}
	}
}
