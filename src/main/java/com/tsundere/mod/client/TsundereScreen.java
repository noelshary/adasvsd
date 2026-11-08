package com.tsundere.mod.client;

import com.tsundere.mod.ActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TsundereScreen extends Screen {
    private final int entityId;

    public TsundereScreen(int entityId) {
        super(Text.literal("Tsundere"));
        this.entityId = entityId;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 70, y = this.height / 2 - 45;
        // Titel als deaktivierter Button (vermeidet Text-Render-API-Unterschiede)
        ButtonWidget title = ButtonWidget.builder(Text.literal("Tsundere"), b -> { })
                .dimensions(x, y - 30, 140, 20).build();
        title.active = false;
        addDrawableChild(title);

        addAction(x, y,      "Folge mir",       0);
        addAction(x, y + 25, "Bleib stehen",    1);
        addAction(x, y + 50, "Mit ihr reden",   2);
        addAction(x, y + 75, "Essen schenken",  3);
    }

    private void addAction(int x, int y, String label, int action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            ClientPlayNetworking.send(new ActionPayload(entityId, action));
            if (action == 0 || action == 1) this.close();
        }).dimensions(x, y, 140, 20).build());
    }

    @Override
    public boolean shouldPause() { return false; }
}
