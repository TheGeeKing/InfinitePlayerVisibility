package com.infiniteplayervisibility.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class InfinitePlayerVisibilityConfigTest {
	@Test
	void clampsVisibilityDistanceToMinimumVoxyStep() {
		assertEquals(20, chunks(64));
		assertEquals(20, chunks(blocks(20)));
	}

	@Test
	void snapsVisibilityDistanceToNearestVoxyStep() {
		assertEquals(20, chunks(blocks(23)));
		assertEquals(26, chunks(blocks(24)));
		assertEquals(92, chunks(blocks(95)));
		assertEquals(100, chunks(blocks(98)));
		assertEquals(100, chunks(blocks(100)));
		assertEquals(100, chunks(blocks(104)));
		assertEquals(108, chunks(blocks(105)));
	}

	@Test
	void clampsVisibilityDistanceToMaximumVoxyStep() {
		assertEquals(2048, chunks(blocks(2048)));
		assertEquals(2048, chunks(30000000));
	}

	@Test
	void exposesStableSliderStepBoundaries() {
		assertEquals(blocks(20), InfinitePlayerVisibilityConfig.visibilityDistanceBlocksAtStep(0));
		assertEquals(blocks(92), InfinitePlayerVisibilityConfig.visibilityDistanceBlocksAtStep(12));
		assertEquals(blocks(100), InfinitePlayerVisibilityConfig.visibilityDistanceBlocksAtStep(13));
		assertEquals(blocks(2048), InfinitePlayerVisibilityConfig.visibilityDistanceBlocksAtStep(Integer.MAX_VALUE));
		assertEquals(258, InfinitePlayerVisibilityConfig.visibilityDistanceBlockStepCount());
	}

	@Test
	void defaultsRemoteRenderDistanceToTerrainContext() {
		InfinitePlayerVisibilityConfig config = new InfinitePlayerVisibilityConfig();

		assertEquals(InfinitePlayerVisibilityConfig.RemoteRenderDistanceMode.TERRAIN_CONTEXT, config.remoteRenderDistanceMode());
	}

	@Test
	void sanitizesNullRemoteRenderDistanceMode() {
		InfinitePlayerVisibilityConfig config = new InfinitePlayerVisibilityConfig();

		config.setRemoteRenderDistanceMode(null);

		assertEquals(InfinitePlayerVisibilityConfig.RemoteRenderDistanceMode.TERRAIN_CONTEXT, config.remoteRenderDistanceMode());
	}

	@Test
	void sanitizesOldInfiniteVisibilityConfigValues() {
		InfinitePlayerVisibilityConfig config = new InfinitePlayerVisibilityConfig();

		config.setVisibilityDistanceBlocks(30000000);

		assertEquals(blocks(2048), config.visibilityDistanceBlocks());
	}

	private static int chunks(int blocks) {
		return InfinitePlayerVisibilityConfig.clampVisibilityDistanceBlocks(blocks) / 16;
	}

	private static int blocks(int chunks) {
		return chunks * 16;
	}
}
