package com.infiniteplayervisibility.test;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class SoloVisibilityAnchorBlock extends BlockWithEntity implements BlockEntityProvider {
	public static final MapCodec<SoloVisibilityAnchorBlock> CODEC = createCodec(SoloVisibilityAnchorBlock::new);

	public SoloVisibilityAnchorBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SoloVisibilityAnchorBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (world.isClient()) {
			return null;
		}

		return validateTicker(type, SoloVisibilityTestContent.SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY, SoloVisibilityAnchorBlockEntity::serverTick);
	}

	@Override
	protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof SoloVisibilityAnchorBlockEntity anchorBlockEntity) {
			anchorBlockEntity.unregister(world);
		} else {
			SoloVisibilityAnchorManager.unregister(world, pos);
		}

		super.onStateReplaced(state, world, pos, moved);
	}
}
