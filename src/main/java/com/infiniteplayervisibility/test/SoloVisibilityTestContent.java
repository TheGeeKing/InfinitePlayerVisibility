package com.infiniteplayervisibility.test;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class SoloVisibilityTestContent {
	public static final Identifier SOLO_VISIBILITY_ANCHOR_ID = Identifier.of(InfinitePlayerVisibilityMod.MOD_ID, "solo_visibility_anchor");
	private static final RegistryKey<Block> SOLO_VISIBILITY_ANCHOR_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, SOLO_VISIBILITY_ANCHOR_ID);
	private static final RegistryKey<Item> SOLO_VISIBILITY_ANCHOR_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, SOLO_VISIBILITY_ANCHOR_ID);
	public static final Block SOLO_VISIBILITY_ANCHOR = Registry.register(
		Registries.BLOCK,
		SOLO_VISIBILITY_ANCHOR_ID,
		new SoloVisibilityAnchorBlock(
			AbstractBlock.Settings.create()
				.registryKey(SOLO_VISIBILITY_ANCHOR_BLOCK_KEY)
				.mapColor(MapColor.ORANGE)
				.strength(3.5F)
				.sounds(BlockSoundGroup.METAL)
		)
	);
	public static final Item SOLO_VISIBILITY_ANCHOR_ITEM = Registry.register(
		Registries.ITEM,
		SOLO_VISIBILITY_ANCHOR_ID,
		new BlockItem(
			SOLO_VISIBILITY_ANCHOR,
			new Item.Settings()
				.registryKey(SOLO_VISIBILITY_ANCHOR_ITEM_KEY)
				.useBlockPrefixedTranslationKey()
		)
	);
	public static final BlockEntityType<SoloVisibilityAnchorBlockEntity> SOLO_VISIBILITY_ANCHOR_BLOCK_ENTITY = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		SOLO_VISIBILITY_ANCHOR_ID,
		FabricBlockEntityTypeBuilder.create(SoloVisibilityAnchorBlockEntity::new, SOLO_VISIBILITY_ANCHOR).build()
	);

	private SoloVisibilityTestContent() {
	}

	public static void initialize() {
		InfinitePlayerVisibilityMod.LOGGER.info("Registered solo visibility test content.");
	}
}
