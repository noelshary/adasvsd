package com.tsundere.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class TsundereMod implements ModInitializer {
    public static final String MOD_ID = "tsundere";

    public static final RegistryKey<EntityType<?>> TSUNDERE_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "tsundere"));
    public static final EntityType<TsundereEntity> TSUNDERE = Registry.register(
            Registries.ENTITY_TYPE, TSUNDERE_KEY,
            EntityType.Builder.create(TsundereEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.8f)
                    .build(TSUNDERE_KEY));

    public static final RegistryKey<Item> EGG_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "tsundere_spawn_egg"));
    public static final Item SPAWN_EGG = Registry.register(
            Registries.ITEM, EGG_KEY,
            new SpawnEggItem(new Item.Settings().registryKey(EGG_KEY).spawnEgg(TSUNDERE)));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(TSUNDERE, TsundereEntity.createAttributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(SPAWN_EGG));

        PayloadTypeRegistry.playS2C().register(OpenMenuPayload.ID, OpenMenuPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ActionPayload.ID, ActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.ID, (payload, context) -> {
            Entity e = context.player().getEntityWorld().getEntityById(payload.entityId());
            if (e instanceof TsundereEntity t && t.squaredDistanceTo(context.player()) < 100) {
                t.handleAction(context.player(), payload.action());
            }
        });
    }
}
