package com.nodiumhosting.vaultmapper.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class VaultMapperConfigScreen extends Screen {
    public VaultMapperConfigScreen() {
        super(new TextComponent("Vault Mapper Config"));
    }

    private int getScaledY(int y) {
        int height = Minecraft.getInstance().getWindow().getHeight() / 2;
        int piece = height / 16;
        return piece * y;
    }

    protected void init() {
        super.init();

        EditBox mapXOffset = new EditBox(this.font, this.width / 2 - 100, getScaledY(2), 200, getScaledY(1) / 2, new TextComponent("MAP_X_OFFSET"));
        mapXOffset.setValue(ClientConfig.MAP_X_OFFSET.get().toString());
        this.addRenderableWidget(mapXOffset);

        EditBox mapYOffset = new EditBox(this.font, this.width / 2 - 100, getScaledY(3), 200, getScaledY(1) / 2, new TextComponent("MAP_Y_OFFSET"));
        mapYOffset.setValue(ClientConfig.MAP_Y_OFFSET.get().toString());
        this.addRenderableWidget(mapYOffset);

        EditBox mapXAnchor = new EditBox(this.font, this.width / 2 - 100, getScaledY(4), 200, getScaledY(1) / 2, new TextComponent("MAP_X_ANCHOR"));
        mapXAnchor.setValue(ClientConfig.MAP_X_ANCHOR.get().toString());
        this.addRenderableWidget(mapXAnchor);

        EditBox mapYAnchor = new EditBox(this.font, this.width / 2 - 100, getScaledY(5), 200, getScaledY(1) / 2, new TextComponent("MAP_Y_ANCHOR"));
        mapYAnchor.setValue(ClientConfig.MAP_Y_ANCHOR.get().toString());
        this.addRenderableWidget(mapYAnchor);

        EditBox pointerColor = new EditBox(this.font, this.width / 2 - 100, getScaledY(6), 200, getScaledY(1) / 2, new TextComponent("POINTER_COLOR"));
        pointerColor.setValue(ClientConfig.POINTER_COLOR.get());
        this.addRenderableWidget(pointerColor);

        EditBox roomColor = new EditBox(this.font, this.width / 2 - 100, getScaledY(7), 200, getScaledY(1) / 2, new TextComponent("ROOM_COLOR"));
        roomColor.setValue(ClientConfig.ROOM_COLOR.get());
        this.addRenderableWidget(roomColor);

        EditBox startRoomColor = new EditBox(this.font, this.width / 2 - 100, getScaledY(8), 200, getScaledY(1) / 2, new TextComponent("START_ROOM_COLOR"));
        startRoomColor.setValue(ClientConfig.START_ROOM_COLOR.get());
        this.addRenderableWidget(startRoomColor);

        EditBox markedRoomColor = new EditBox(this.font, this.width / 2 - 100, getScaledY(9), 200, getScaledY(1) / 2, new TextComponent("MARKED_ROOM_COLOR"));
        markedRoomColor.setValue(ClientConfig.MARKED_ROOM_COLOR.get());
        this.addRenderableWidget(markedRoomColor);

        EditBox inscriptionRoomColor = new EditBox(this.font, this.width / 2 - 100, getScaledY(10), 200, getScaledY(1) / 2, new TextComponent("INSCRIPTION_ROOM_COLOR"));
        inscriptionRoomColor.setValue(ClientConfig.INSCRIPTION_ROOM_COLOR.get());
        this.addRenderableWidget(inscriptionRoomColor);

        Button saveButton = new Button(this.width / 2 - 100, getScaledY(11), 200, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("Save"), button -> {
            ClientConfig.MAP_X_OFFSET.set(Integer.parseInt(mapXOffset.getValue()));
            ClientConfig.MAP_Y_OFFSET.set(Integer.parseInt(mapYOffset.getValue()));
            ClientConfig.MAP_X_ANCHOR.set(Integer.parseInt(mapXAnchor.getValue()));
            ClientConfig.MAP_Y_ANCHOR.set(Integer.parseInt(mapYAnchor.getValue()));
            ClientConfig.POINTER_COLOR.set(pointerColor.getValue());
            ClientConfig.ROOM_COLOR.set(roomColor.getValue());
            ClientConfig.START_ROOM_COLOR.set(startRoomColor.getValue());
            ClientConfig.MARKED_ROOM_COLOR.set(markedRoomColor.getValue());
            ClientConfig.INSCRIPTION_ROOM_COLOR.set(inscriptionRoomColor.getValue());

            ClientConfig.SPEC.save();

            VaultMapOverlayRenderer.updateAnchor();
        });
        this.addRenderableWidget(saveButton);

        Button resetButton = new Button(this.width / 2 - 100, getScaledY(12), 200, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("Reset"), button -> {
            mapXOffset.setValue("0");
            mapYOffset.setValue("0");
            mapXAnchor.setValue("2");
            mapYAnchor.setValue("2");
            pointerColor.setValue("#00FF00");
            roomColor.setValue("#0000FF");
            startRoomColor.setValue("#FF0000");
            markedRoomColor.setValue("#FF00FF");
            inscriptionRoomColor.setValue("#FFFF00");

            ClientConfig.MAP_X_OFFSET.set(0);
            ClientConfig.MAP_Y_OFFSET.set(0);
            ClientConfig.MAP_X_ANCHOR.set(2);
            ClientConfig.MAP_Y_ANCHOR.set(2);
            ClientConfig.POINTER_COLOR.set("#00FF00");
            ClientConfig.ROOM_COLOR.set("#0000FF");
            ClientConfig.START_ROOM_COLOR.set("#FF0000");
            ClientConfig.MARKED_ROOM_COLOR.set("#FF00FF");
            ClientConfig.INSCRIPTION_ROOM_COLOR.set("#FFFF00");

            ClientConfig.SPEC.save();
        });
        this.addRenderableWidget(resetButton);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);

        this.font.draw(pose, "Vault Mapper Config", this.width / 2 - this.font.width("Vault Mapper Config") / 2, 20, 0xFFFFFFFF);

        // labels
        int offsetY = getScaledY(1) / 3;
        this.font.draw(pose, "Map X Offset", this.width / 2 - 100, getScaledY(2) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map Y Offset", this.width / 2 - 100, getScaledY(3) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map X Anchor (0-left, 1-center, 2-right)", this.width / 2 - 100, getScaledY(4) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map Y Anchor (0-top, 1-center, 2-bottom)", this.width / 2 - 100, getScaledY(5) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Pointer Color", this.width / 2 - 100, getScaledY(6) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Room Color", this.width / 2 - 100, getScaledY(7) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Start Room Color", this.width / 2 - 100, getScaledY(8) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Marked Room Color", this.width / 2 - 100, getScaledY(9) - offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Inscription Room Color", this.width / 2 - 100, getScaledY(10) - offsetY, 0xFFFFFFFF);

        super.render(pose, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
    }

    @Override
    public void onClose() {
        // Stop any handlers here

        // Call last in case it interferes with the override
        super.onClose();
    }

    @Override
    public void removed() {
        // Reset initial states here

        // Call last in case it interferes with the override
        super.removed();

    }
}