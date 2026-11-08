package com.tsundere.mod.client;

import com.tsundere.mod.TsundereEntity;
import com.tsundere.mod.TsundereMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

/** Rendert die Tsundere mit dem Spieler-Modell + deinem Skin (1:1). */
public class TsundereRenderer extends MobEntityRenderer<TsundereEntity, PlayerEntityRenderState, PlayerEntityModel> {
    // Dein Skin hat breite Arme (Steve). Für dünne Arme (Alex): true setzen.
    private static final boolean SLIM_ARMS = false;
    private static final Identifier TEXTURE = Identifier.of(TsundereMod.MOD_ID, "textures/entity/tsundere.png");

    public TsundereRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel(
                ctx.getPart(EntityModelLayers.PLAYER), false),
                0.5f);
    }

    @Override
    public PlayerEntityRenderState createRenderState() { return new PlayerEntityRenderState(); }

    @Override
    public void updateRenderState(TsundereEntity entity, PlayerEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        // Zweite Skin-Ebene (Haare/Jacke/Ärmel/Hose) sichtbar machen
        state.hatVisible = true;
        state.jacketVisible = true;
        state.leftSleeveVisible = true;
        state.rightSleeveVisible = true;
        state.leftPantsLegVisible = true;
        state.rightPantsLegVisible = true;
        state.capeVisible = false;
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) { return TEXTURE; }
}
