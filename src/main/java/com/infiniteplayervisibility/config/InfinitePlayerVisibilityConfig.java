package com.infiniteplayervisibility.config;

import com.infiniteplayervisibility.EntityVisibilityRules;

public final class InfinitePlayerVisibilityConfig {
	public static final int MIN_VISIBILITY_DISTANCE_BLOCKS = 64;
	public static final int MAX_VISIBILITY_DISTANCE_BLOCKS = EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS;

	private boolean renderRemotePlayers = true;
	private boolean renderRemoteEntities = true;
	private int visibilityDistanceBlocks = MAX_VISIBILITY_DISTANCE_BLOCKS;

	public boolean renderRemotePlayers() {
		return this.renderRemotePlayers;
	}

	public void setRenderRemotePlayers(boolean renderRemotePlayers) {
		this.renderRemotePlayers = renderRemotePlayers;
	}

	public boolean renderRemoteEntities() {
		return this.renderRemoteEntities;
	}

	public void setRenderRemoteEntities(boolean renderRemoteEntities) {
		this.renderRemoteEntities = renderRemoteEntities;
	}

	public int visibilityDistanceBlocks() {
		return clampVisibilityDistanceBlocks(this.visibilityDistanceBlocks);
	}

	public void setVisibilityDistanceBlocks(int visibilityDistanceBlocks) {
		this.visibilityDistanceBlocks = clampVisibilityDistanceBlocks(visibilityDistanceBlocks);
	}

	public boolean usesInfiniteVisibilityDistance() {
		return this.visibilityDistanceBlocks() >= MAX_VISIBILITY_DISTANCE_BLOCKS;
	}

	public InfinitePlayerVisibilityConfig copy() {
		InfinitePlayerVisibilityConfig copy = new InfinitePlayerVisibilityConfig();
		copy.renderRemotePlayers = this.renderRemotePlayers;
		copy.renderRemoteEntities = this.renderRemoteEntities;
		copy.visibilityDistanceBlocks = this.visibilityDistanceBlocks();
		return copy;
	}

	public InfinitePlayerVisibilityConfig sanitizedCopy() {
		InfinitePlayerVisibilityConfig copy = this.copy();
		copy.visibilityDistanceBlocks = copy.visibilityDistanceBlocks();
		return copy;
	}

	public static int clampVisibilityDistanceBlocks(int visibilityDistanceBlocks) {
		if (visibilityDistanceBlocks >= MAX_VISIBILITY_DISTANCE_BLOCKS) {
			return MAX_VISIBILITY_DISTANCE_BLOCKS;
		}

		return Math.max(MIN_VISIBILITY_DISTANCE_BLOCKS, visibilityDistanceBlocks);
	}
}
