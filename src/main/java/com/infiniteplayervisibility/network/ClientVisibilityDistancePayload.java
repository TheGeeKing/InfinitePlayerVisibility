package com.infiniteplayervisibility.network;

import com.infiniteplayervisibility.InfinitePlayerVisibilityMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientVisibilityDistancePayload(int visibilityDistanceBlocks) implements CustomPacketPayload {
	public static final Type<ClientVisibilityDistancePayload> TYPE = new Type<>(
		Identifier.fromNamespaceAndPath(InfinitePlayerVisibilityMod.MOD_ID, "client_visibility_distance")
	);
	public static final StreamCodec<FriendlyByteBuf, ClientVisibilityDistancePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientVisibilityDistancePayload::visibilityDistanceBlocks,
		ClientVisibilityDistancePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
