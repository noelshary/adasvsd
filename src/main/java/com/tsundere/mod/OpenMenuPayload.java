package com.tsundere.mod;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> Client: öffne das Menü für diese Entity. */
public record OpenMenuPayload(int entityId) implements CustomPayload {
    public static final CustomPayload.Id<OpenMenuPayload> ID =
            new CustomPayload.Id<>(Identifier.of(TsundereMod.MOD_ID, "open_menu"));
    public static final PacketCodec<RegistryByteBuf, OpenMenuPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, OpenMenuPayload::entityId, OpenMenuPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
