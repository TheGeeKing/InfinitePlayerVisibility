package com.infiniteplayervisibility.test;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SoloVisibilityAnchorBlockEntity extends BlockEntity {
	private boolean registered;

	public SoloVisibilityAnchorBlockEntity(BlockPos pos, BlockState state) {
		super(SoloVisibilityTestContent.SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, SoloVisibilityAnchorBlockEntity blockEntity) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		if (!blockEntity.registered) {
			SoloVisibilityAnchorManager.register(serverWorld, pos);
			blockEntity.registered = true;
		}
	}

	public void unregister(ServerWorld world) {
		if (!this.registered) {
			return;
		}

		SoloVisibilityAnchorManager.unregister(world, this.pos);
		this.registered = false;
	}
}
