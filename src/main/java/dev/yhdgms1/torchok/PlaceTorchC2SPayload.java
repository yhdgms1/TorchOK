package dev.yhdgms1.torchok;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlaceTorchC2SPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Identifier PLACE_TORCH_PAYLOAD_ID = Identifier.fromNamespaceAndPath(TorchOK.MOD_ID, "place_torch");
    public static final CustomPacketPayload.Type<PlaceTorchC2SPayload> TYPE = new CustomPacketPayload.Type<>(PLACE_TORCH_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceTorchC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PlaceTorchC2SPayload::pos,
            PlaceTorchC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
