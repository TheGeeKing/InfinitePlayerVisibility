package com.infiniteplayervisibility.config;

public final class InfinitePlayerVisibilityConfig {
	public static final int MIN_VISIBILITY_DISTANCE_CHUNKS = 20;
	public static final int VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS = 100;
	public static final int LOW_VISIBILITY_DISTANCE_STEP_CHUNKS = 6;
	public static final int HIGH_VISIBILITY_DISTANCE_STEP_CHUNKS = 8;
	public static final int MAX_VISIBILITY_DISTANCE_CHUNKS = 2048;
	public static final int MIN_VISIBILITY_DISTANCE_BLOCKS = chunksToBlocks(MIN_VISIBILITY_DISTANCE_CHUNKS);
	public static final int MAX_VISIBILITY_DISTANCE_BLOCKS = chunksToBlocks(MAX_VISIBILITY_DISTANCE_CHUNKS);
	public static final int MIN_ANCHOR_RADIUS_BLOCKS = 0;
	public static final int MAX_ANCHOR_RADIUS_BLOCKS = 128;
	public static final int DEFAULT_ANCHOR_RADIUS_BLOCKS = 128;
	public static final int MIN_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 1;
	public static final int MAX_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 200;
	public static final int DEFAULT_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 4;
	public static final int UNLIMITED_TRACKED_ENTITIES_PER_REFRESH = 0;
	public static final int MAX_TRACKED_ENTITIES_PER_REFRESH = 1000000;

	public enum RemoteRenderDistanceMode {
		MOD_DISTANCE,
		TERRAIN_CONTEXT
	}

	private boolean enabled = true;
	private boolean renderRemotePlayers = true;
	private boolean renderRemoteEntities = true;
	private int visibilityDistanceBlocks = MAX_VISIBILITY_DISTANCE_BLOCKS;
	private RemoteRenderDistanceMode remoteRenderDistanceMode = RemoteRenderDistanceMode.TERRAIN_CONTEXT;
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
		return false;
	}

	public RemoteRenderDistanceMode remoteRenderDistanceMode() {
		return this.remoteRenderDistanceMode == null ? RemoteRenderDistanceMode.TERRAIN_CONTEXT : this.remoteRenderDistanceMode;
	}

	public void setRemoteRenderDistanceMode(RemoteRenderDistanceMode remoteRenderDistanceMode) {
		this.remoteRenderDistanceMode = remoteRenderDistanceMode == null ? RemoteRenderDistanceMode.TERRAIN_CONTEXT : remoteRenderDistanceMode;
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
		copy.remoteRenderDistanceMode = this.remoteRenderDistanceMode();
		copy.enableSoloVisibilityAnchor = this.enableSoloVisibilityAnchor;
		copy.anchorRadiusBlocks = this.anchorRadiusBlocks();
		copy.remoteEntityTrackingIntervalTicks = this.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedEntitiesPerRefresh = this.maxTrackedEntitiesPerRefresh();
		return copy;
	}

	public InfinitePlayerVisibilityConfig sanitizedCopy() {
		InfinitePlayerVisibilityConfig copy = this.copy();
		copy.visibilityDistanceBlocks = copy.visibilityDistanceBlocks();
		copy.remoteRenderDistanceMode = copy.remoteRenderDistanceMode();
		copy.anchorRadiusBlocks = copy.anchorRadiusBlocks();
		copy.remoteEntityTrackingIntervalTicks = copy.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedEntitiesPerRefresh = copy.maxTrackedEntitiesPerRefresh();
		return copy;
	}

	public static int clampVisibilityDistanceBlocks(int visibilityDistanceBlocks) {
		int chunks = (int)Math.ceil(visibilityDistanceBlocks / 16.0D);
		if (chunks <= MIN_VISIBILITY_DISTANCE_CHUNKS) {
			return MIN_VISIBILITY_DISTANCE_BLOCKS;
		}

		if (chunks >= MAX_VISIBILITY_DISTANCE_CHUNKS) {
			return MAX_VISIBILITY_DISTANCE_BLOCKS;
		}

		return visibilityDistanceBlocksAtStep(nearestVisibilityDistanceStepIndex(visibilityDistanceBlocks));
	}

	public static int visibilityDistanceBlockStepCount() {
		return getLowStepCount() + 1 + getHighStepCount();
	}

	public static int visibilityDistanceBlocksAtStep(int stepIndex) {
		int clampedStepIndex = Math.max(0, Math.min(visibilityDistanceBlockStepCount() - 1, stepIndex));
		int lowStepCount = getLowStepCount();
		if (clampedStepIndex < lowStepCount) {
			return chunksToBlocks(MIN_VISIBILITY_DISTANCE_CHUNKS + clampedStepIndex * LOW_VISIBILITY_DISTANCE_STEP_CHUNKS);
		}

		if (clampedStepIndex == lowStepCount) {
			return chunksToBlocks(VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS);
		}

		int highStepIndex = clampedStepIndex - lowStepCount - 1;
		int chunks = VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS + (highStepIndex + 1) * HIGH_VISIBILITY_DISTANCE_STEP_CHUNKS;
		return chunksToBlocks(Math.min(MAX_VISIBILITY_DISTANCE_CHUNKS, chunks));
	}

	public static int visibilityDistanceStepIndex(int visibilityDistanceBlocks) {
		int snappedBlocks = clampVisibilityDistanceBlocks(visibilityDistanceBlocks);
		return nearestVisibilityDistanceStepIndex(snappedBlocks);
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

	private static int nearestVisibilityDistanceStepIndex(int visibilityDistanceBlocks) {
		int bestIndex = 0;
		int bestDistance = Integer.MAX_VALUE;
		for (int index = 0; index < visibilityDistanceBlockStepCount(); index++) {
			int distance = Math.abs(visibilityDistanceBlocksAtStep(index) - visibilityDistanceBlocks);
			if (distance < bestDistance) {
				bestIndex = index;
				bestDistance = distance;
			}
		}

		return bestIndex;
	}

	private static int getLowStepCount() {
		return Math.floorDiv(
			VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS - MIN_VISIBILITY_DISTANCE_CHUNKS - HIGH_VISIBILITY_DISTANCE_STEP_CHUNKS,
			LOW_VISIBILITY_DISTANCE_STEP_CHUNKS
		) + 1;
	}

	private static int getHighStepCount() {
		int distanceAfterStepChange = MAX_VISIBILITY_DISTANCE_CHUNKS - VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS;
		return (int)Math.ceil(distanceAfterStepChange / (double)HIGH_VISIBILITY_DISTANCE_STEP_CHUNKS);
	}

	private static int chunksToBlocks(int chunks) {
		return chunks * 16;
	}
}
