package com.tsundere.mod;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> Server: Menü-Aktion (0 folgen, 1 stehen, 2 reden, 3 Geschenk). */
public record ActionPayload(int entityId, int action) implements CustomPayload {
    public static final CustomPayload.Id<ActionPayload> ID =
            new CustomPayload.Id<>(Identifier.of(TsundereMod.MOD_ID, "action"));
    public static final PacketCodec<RegistryByteBuf, ActionPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, ActionPayload::entityId,
                    PacketCodecs.VAR_INT, ActionPayload::action, ActionPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
