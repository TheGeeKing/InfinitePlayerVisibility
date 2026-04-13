package com.infiniteplayervisibility.test;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class SoloVisibilityTestContent {
	public static final Identifier SOLO_VISIBILITY_ANCHOR_ID = Identifier.fromNamespaceAndPath(InfinitePlayerVisibilityMod.MOD_ID, "solo_visibility_anchor");
	private static final ResourceKey<Block> SOLO_VISIBILITY_ANCHOR_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, SOLO_VISIBILITY_ANCHOR_ID);
	private static final ResourceKey<Item> SOLO_VISIBILITY_ANCHOR_ITEM_KEY = ResourceKey.create(Registries.ITEM, SOLO_VISIBILITY_ANCHOR_ID);
	public static final Block SOLO_VISIBILITY_ANCHOR = Registry.register(
		BuiltInRegistries.BLOCK,
		SOLO_VISIBILITY_ANCHOR_ID,
		new SoloVisibilityAnchorBlock(
			BlockBehaviour.Properties.of()
				.setId(SOLO_VISIBILITY_ANCHOR_BLOCK_KEY)
				.mapColor(MapColor.COLOR_ORANGE)
				.strength(3.5F)
				.sound(SoundType.METAL)
		)
	);
	public static final Item SOLO_VISIBILITY_ANCHOR_ITEM = Registry.register(
		BuiltInRegistries.ITEM,
		SOLO_VISIBILITY_ANCHOR_ID,
		new BlockItem(
			SOLO_VISIBILITY_ANCHOR,
			new Item.Properties()
				.setId(SOLO_VISIBILITY_ANCHOR_ITEM_KEY)
				.useBlockDescriptionPrefix()
		)
	);
	public static final BlockEntityType<SoloVisibilityAnchorBlockEntity> SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		SOLO_VISIBILITY_ANCHOR_ID,
		FabricBlockEntityTypeBuilder.create(SoloVisibilityAnchorBlockEntity::new, SOLO_VISIBILITY_ANCHOR).build()
	);

	private SoloVisibilityTestContent() {
	}

	public static void initialize() {
		InfinitePlayerVisibilityMod.LOGGER.info("Registered solo visibility test content.");
	}
}
