package com.infiniteplayervisibility.config;

public final class InfinitePlayerVisibilityConfig {
	public static final int MIN_VISIBILITY_DISTANCE_CHUNKS = 20;
	public static final int VISIBILITY_DISTANCE_STEP_CHANGE_CHUNKS = 100;
	public static final int LOW_VISIBILITY_DISTANCE_STEP_CHUNKS = 6;
	public static final int HIGH_VISIBILITY_DISTANCE_STEP_CHUNKS = 8;
	public static final int MAX_VISIBILITY_DISTANCE_CHUNKS = 2048;
	public static final int MIN_VISIBILITY_DISTANCE_BLOCKS = chunksToBlocks(MIN_VISIBILITY_DISTANCE_CHUNKS);
	public static final int MAX_VISIBILITY_DISTANCE_BLOCKS = chunksToBlocks(MAX_VISIBILITY_DISTANCE_CHUNKS);
	public static final int MIN_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 1;
	public static final int MAX_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 200;
	public static final int DEFAULT_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS = 4;
	public static final int UNLIMITED_TRACKED_ENTITIES_PER_REFRESH = 0;
	public static final int MAX_TRACKED_ENTITIES_PER_REFRESH = 1000000;
	public static final int DEFAULT_MAX_TRACKED_REMOTE_ENTITIES_PER_REFRESH = 128;
	public static final int DEFAULT_REMOTE_ENTITIES_PER_PLAYER_REFRESH_RATIO = 16;

	public enum RemoteRenderDistanceMode {
		MOD_DISTANCE,
		TERRAIN_CONTEXT
	}

	private boolean enabled = true;
	private boolean renderRemotePlayers = true;
	private boolean renderRemoteEntities = true;
	private int visibilityDistanceBlocks = MAX_VISIBILITY_DISTANCE_BLOCKS;
	private RemoteRenderDistanceMode remoteRenderDistanceMode = RemoteRenderDistanceMode.TERRAIN_CONTEXT;
	private int remoteEntityTrackingIntervalTicks = DEFAULT_REMOTE_ENTITY_TRACKING_INTERVAL_TICKS;
	private int maxTrackedRemotePlayersPerRefresh = UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;
	private int maxTrackedRemoteEntitiesPerRefresh = DEFAULT_MAX_TRACKED_REMOTE_ENTITIES_PER_REFRESH;
	private int remoteEntitiesPerPlayerRefreshRatio = DEFAULT_REMOTE_ENTITIES_PER_PLAYER_REFRESH_RATIO;

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

	public RemoteRenderDistanceMode remoteRenderDistanceMode() {
		return this.remoteRenderDistanceMode == null ? RemoteRenderDistanceMode.TERRAIN_CONTEXT : this.remoteRenderDistanceMode;
	}

	public void setRemoteRenderDistanceMode(RemoteRenderDistanceMode remoteRenderDistanceMode) {
		this.remoteRenderDistanceMode = remoteRenderDistanceMode == null ? RemoteRenderDistanceMode.TERRAIN_CONTEXT : remoteRenderDistanceMode;
	}

	public int remoteEntityTrackingIntervalTicks() {
		return clampRemoteEntityTrackingIntervalTicks(this.remoteEntityTrackingIntervalTicks);
	}

	public void setRemoteEntityTrackingIntervalTicks(int remoteEntityTrackingIntervalTicks) {
		this.remoteEntityTrackingIntervalTicks = clampRemoteEntityTrackingIntervalTicks(remoteEntityTrackingIntervalTicks);
	}

	public int maxTrackedRemotePlayersPerRefresh() {
		return clampMaxTrackedEntitiesPerRefresh(this.maxTrackedRemotePlayersPerRefresh);
	}

	public void setMaxTrackedRemotePlayersPerRefresh(int maxTrackedRemotePlayersPerRefresh) {
		this.maxTrackedRemotePlayersPerRefresh = clampMaxTrackedEntitiesPerRefresh(maxTrackedRemotePlayersPerRefresh);
	}

	public int maxTrackedRemoteEntitiesPerRefresh() {
		return clampMaxTrackedEntitiesPerRefresh(this.maxTrackedRemoteEntitiesPerRefresh);
	}

	public void setMaxTrackedRemoteEntitiesPerRefresh(int maxTrackedRemoteEntitiesPerRefresh) {
		this.maxTrackedRemoteEntitiesPerRefresh = clampMaxTrackedEntitiesPerRefresh(maxTrackedRemoteEntitiesPerRefresh);
	}

	public int remoteEntitiesPerPlayerRefreshRatio() {
		return clampMaxTrackedEntitiesPerRefresh(this.remoteEntitiesPerPlayerRefreshRatio);
	}

	public void setRemoteEntitiesPerPlayerRefreshRatio(int remoteEntitiesPerPlayerRefreshRatio) {
		this.remoteEntitiesPerPlayerRefreshRatio = clampMaxTrackedEntitiesPerRefresh(remoteEntitiesPerPlayerRefreshRatio);
	}

	public InfinitePlayerVisibilityConfig copy() {
		InfinitePlayerVisibilityConfig copy = new InfinitePlayerVisibilityConfig();
		copy.enabled = this.enabled;
		copy.renderRemotePlayers = this.renderRemotePlayers;
		copy.renderRemoteEntities = this.renderRemoteEntities;
		copy.visibilityDistanceBlocks = this.visibilityDistanceBlocks();
		copy.remoteRenderDistanceMode = this.remoteRenderDistanceMode();
		copy.remoteEntityTrackingIntervalTicks = this.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedRemotePlayersPerRefresh = this.maxTrackedRemotePlayersPerRefresh();
		copy.maxTrackedRemoteEntitiesPerRefresh = this.maxTrackedRemoteEntitiesPerRefresh();
		copy.remoteEntitiesPerPlayerRefreshRatio = this.remoteEntitiesPerPlayerRefreshRatio();
		return copy;
	}

	public InfinitePlayerVisibilityConfig sanitizedCopy() {
		InfinitePlayerVisibilityConfig copy = this.copy();
		copy.visibilityDistanceBlocks = copy.visibilityDistanceBlocks();
		copy.remoteRenderDistanceMode = copy.remoteRenderDistanceMode();
		copy.remoteEntityTrackingIntervalTicks = copy.remoteEntityTrackingIntervalTicks();
		copy.maxTrackedRemotePlayersPerRefresh = copy.maxTrackedRemotePlayersPerRefresh();
		copy.maxTrackedRemoteEntitiesPerRefresh = copy.maxTrackedRemoteEntitiesPerRefresh();
		copy.remoteEntitiesPerPlayerRefreshRatio = copy.remoteEntitiesPerPlayerRefreshRatio();
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

	public static int resolveRemoteEntityRefreshCap(int onlinePlayers, int maxTrackedRemoteEntitiesPerRefresh, int remoteEntitiesPerPlayerRefreshRatio) {
		int absoluteCap = clampMaxTrackedEntitiesPerRefresh(maxTrackedRemoteEntitiesPerRefresh);
		int ratioCap = clampScaledRefreshCap(onlinePlayers, remoteEntitiesPerPlayerRefreshRatio);
		if (absoluteCap == UNLIMITED_TRACKED_ENTITIES_PER_REFRESH) {
			return ratioCap;
		}

		if (ratioCap == UNLIMITED_TRACKED_ENTITIES_PER_REFRESH) {
			return absoluteCap;
		}

		return Math.min(absoluteCap, ratioCap);
	}

	private static int clampScaledRefreshCap(int onlinePlayers, int remoteEntitiesPerPlayerRefreshRatio) {
		int ratio = clampMaxTrackedEntitiesPerRefresh(remoteEntitiesPerPlayerRefreshRatio);
		if (ratio == UNLIMITED_TRACKED_ENTITIES_PER_REFRESH) {
			return UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;
		}

		if (onlinePlayers <= 0) {
			return UNLIMITED_TRACKED_ENTITIES_PER_REFRESH;
		}

		long scaledCap = (long)onlinePlayers * ratio;
		return (int)Math.min(MAX_TRACKED_ENTITIES_PER_REFRESH, scaledCap);
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
