package com.infiniteplayervisibility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InfinitePlayerVisibilityConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("infinite-player-visibility.json");
	private static InfinitePlayerVisibilityConfig config = new InfinitePlayerVisibilityConfig();

	private InfinitePlayerVisibilityConfigManager() {
	}

	public static void load() {
		config = readConfig();
		save();
	}

	public static InfinitePlayerVisibilityConfig getConfig() {
		return config;
	}

	public static void setConfig(InfinitePlayerVisibilityConfig newConfig) {
		config = sanitize(newConfig);
		save();
	}

	public static Path getConfigPath() {
		return CONFIG_PATH;
	}

	private static InfinitePlayerVisibilityConfig readConfig() {
		if (!Files.exists(CONFIG_PATH)) {
			return new InfinitePlayerVisibilityConfig();
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			return sanitize(GSON.fromJson(reader, InfinitePlayerVisibilityConfig.class));
		} catch (IOException | RuntimeException exception) {
			InfinitePlayerVisibilityMod.LOGGER.error("Failed to read config file {}.", CONFIG_PATH, exception);
			return new InfinitePlayerVisibilityConfig();
		}
	}

	private static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			InfinitePlayerVisibilityMod.LOGGER.error("Failed to write config file {}.", CONFIG_PATH, exception);
		}
	}

	private static InfinitePlayerVisibilityConfig sanitize(InfinitePlayerVisibilityConfig loadedConfig) {
		return loadedConfig == null ? new InfinitePlayerVisibilityConfig() : loadedConfig.sanitizedCopy();
	}
}
