package com.infiniteplayervisibility.anchor;

import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfig;
import com.infiniteplayervisibility.config.InfinitePlayerVisibilityConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SoloVisibilityAnchorBlockEntity extends BlockEntity {
	private boolean registered;

	public SoloVisibilityAnchorBlockEntity(BlockPos pos, BlockState state) {
		super(SoloVisibilityAnchorContent.SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SoloVisibilityAnchorBlockEntity blockEntity) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		InfinitePlayerVisibilityConfig config = InfinitePlayerVisibilityConfigManager.getConfig();
		if (!config.enableSoloVisibilityAnchor()) {
			if (blockEntity.registered) {
				SoloVisibilityAnchorManager.unregister(serverLevel, pos);
				blockEntity.registered = false;
			}
			return;
		}

		if (!blockEntity.registered) {
			SoloVisibilityAnchorManager.register(serverLevel, pos, config.anchorRadiusBlocks());
			blockEntity.registered = true;
			return;
		}

		SoloVisibilityAnchorManager.refresh(serverLevel, pos, config.anchorRadiusBlocks());
	}

	@Override
	public void setRemoved() {
		if (this.registered && this.level instanceof ServerLevel serverLevel) {
			SoloVisibilityAnchorManager.unregister(serverLevel, this.getBlockPos());
			this.registered = false;
		}

		super.setRemoved();
	}
}
