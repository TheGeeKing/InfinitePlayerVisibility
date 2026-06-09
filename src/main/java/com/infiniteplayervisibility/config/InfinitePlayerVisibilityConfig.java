package com.infiniteplayervisibility.config;

import com.infiniteplayervisibility.EntityVisibilityRules;

public final class InfinitePlayerVisibilityConfig {
	public static final int MIN_VISIBILITY_DISTANCE_BLOCKS = 64;
	public static final int MAX_VISIBILITY_DISTANCE_BLOCKS = EntityVisibilityRules.INFINITE_TRACKING_DISTANCE_BLOCKS;
	public static final int MIN_ANCHOR_RADIUS_BLOCKS = 0;
	public static final int MAX_ANCHOR_RADIUS_BLOCKS = 128;
	public static final int DEFAULT_ANCHOR_RADIUS_BLOCKS = 128;
	public static final int MIN_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 1;
	public static final int MAX_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 200;
	public static final int DEFAULT_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 4;
	public static final int UNLIMITED_TRACKED_ENTITIES_PER_REFRESH = 0;
	public static final int MAX_TRACKED_ENTITIES_PER_REFRESH = 1000000;

	private boolean enabled = true;
	private boolean renderRemotePlayers = true;
	private boolean renderRemoteEntities = true;
	private int visibilityDistanceBlocks = MAX_VISIBILITY_DISTANCE_BLOCKS;
	private boolean enableSoloVisibilityAnchor = true;
	private int anchorRadiusBlocks = DEFAULT_ANCHOR_RADIUS_BLOCKS;
	private int remoteEntityTrackingIntervalTicks = DEFAULT_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS;
	private int maxTrackedEntitiesPerRefresh = UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;

	public boolean enabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

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

	public boolean enableSoloVisibilityAnchor() {
		return this.enableSoloVisibilityAnchor;
	}

	public void setEnableSoloVisibilityAnchor(boolean enableSoloVisibilityAnchor) {
		this.enableSoloVisibilityAnchor = enableSoloVisibilityAnchor;
	}

	public int anchorRadiusBlocks() {
		return clampAnchorRadiusBlocks(this.anchorRadiusBlocks);
	}

	public void setAnchorRadiusBlocks(int anchorRadiusBlocks) {
		this.anchorRadiusBlocks = clampAnchorRadiusBlocks(anchorRadiusBlocks);
	}

	public int remoteEntityTrackingIntervalTicks() {
		return clampRemoteEntityTrackingIntervalTicks(this.remoteEntityTrackingIntervalTicks);
	}

	public void setRemoteEntityTrackingIntervalTicks(int remoteEntityTrackingIntervalTicks) {
		this.remoteEntityTrackingIntervalTicks = clampRemoteEntityTrackingIntervalTicks(remoteEntityTrackingIntervalTicks);
	}

	public int maxTrackedEntitiesPerRefresh() {
		return clampMaxTrackedEntitiesPerRefresh(this.maxTrackedEntitiesPerRefresh);
	}

	public void setMaxTrackedEntitiesPerRefresh(int maxTrackedEntitiesPerRefresh) {
		this.maxTrackedEntitiesPerRefresh = clampMaxTrackedEntitiesPerRefresh(maxTrackedEntitiesPerRefresh);
	}

	public InfinitePlayerVisibilityConfig copy() {
		InfinitePlayerVisibilityConfig copy = new InfinitePlayerVisibilityConfig();
		copy.enabled = this.enabled;
		copy.renderRemotePlayers = this.renderRemotePlayers;
		copy.renderRemoteEntities = this.renderRemoteEntities;
		copy.visibilityDistanceBlocks = this.visibilityDistanceBlocks();
		copy.enableSoloVisibilityAnchor = this.enableSoloVisibilityAnchor;
		copy.anchorRadiusBlocks = this.anchorRadiusBlocks();
		copy.remoteEntityTrackingIntervalTicks = this.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedEntitiesPerRefresh = this.maxTrackedEntitiesPerRefresh();
		return copy;
	}

	public InfinitePlayerVisibilityConfig sanitizedCopy() {
		InfinitePlayerVisibilityConfig copy = this.copy();
		copy.visibilityDistanceBlocks = copy.visibilityDistanceBlocks();
		copy.anchorRadiusBlocks = copy.anchorRadiusBlocks();
		copy.remoteEntityTrackingIntervalTicks = copy.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedEntitiesPerRefresh = copy.maxTrackedEntitiesPerRefresh();
		return copy;
	}

	public static int clampVisibilityDistanceBlocks(int visibilityDistanceBlocks) {
		if (visibilityDistanceBlocks >= MAX_VISIBILITY_DISTANCE_BLOCKS) {
			return MAX_VISIBILITY_DISTANCE_BLOCKS;
		}

		return Math.max(MIN_VISIBILITY_DISTANCE_BLOCKS, visibilityDistanceBlocks);
	}

	public static int clampAnchorRadiusBlocks(int anchorRadiusBlocks) {
		return Math.max(MIN_ANCHOR_RADIUS_BLOCKS, Math.min(MAX_ANCHOR_RADIUS_BLOCKS, anchorRadiusBlocks));
	}

	public static int clampRemoteEntityTrackingIntervalTicks(int remoteEntityTrackingIntervalTicks) {
		return Math.max(
			MIN_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS,
			Math.min(MAX_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS, remoteEntityTrackingIntervalTicks)
		);
	}

	public static int clampMaxTrackedEntitiesPerRefresh(int maxTrackedEntitiesPerRefresh) {
		if (maxTrackedEntitiesPerRefresh <= UNLIMITED_TRACKED_ENTITIES_PER_REFRESH) {
			return UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;
		}

		return Math.min(MAX_TRACKED_ENTITIES_PER_REFRESH, maxTrackedEntitiesPerRefresh);
	}
}
