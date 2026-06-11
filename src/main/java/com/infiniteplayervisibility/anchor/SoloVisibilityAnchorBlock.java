package com.infiniteplayervisibility.anchor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class SoloVisibilityAnchorBlock extends BaseEntityBlock {
	public static final MapCodec<SoloVisibilityAnchorBlock> CODEC = simpleCodec(SoloVisibilityAnchorBlock::new);

	public SoloVisibilityAnchorBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SoloVisibilityAnchorBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}

		return createTickerHelper(type, SoloVisibilityAnchorContent.SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY, SoloVisibilityAnchorBlockEntity::serverTick);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		SoloVisibilityAnchorManager.unregister(level, pos);
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}
}
