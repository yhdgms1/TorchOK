package dev.yhdgms1.torchok;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.List;

public record SupportedTorchesS2CPayload(List<String> torches) implements CustomPacketPayload {
    public static final Identifier SUPPORTED_TORCHES_PAYLOAD_ID = Identifier.fromNamespaceAndPath(TorchOK.MOD_ID, "supported_torches");
    public static final CustomPacketPayload.Type<SupportedTorchesS2CPayload> TYPE = new CustomPacketPayload.Type<>(SUPPORTED_TORCHES_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SupportedTorchesS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SupportedTorchesS2CPayload::torches,
            SupportedTorchesS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
