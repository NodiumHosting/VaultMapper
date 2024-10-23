package com.nodiumhosting.vaultmapper.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.gui.component.ColorButton;
import com.nodiumhosting.vaultmapper.gui.component.ColorPicker;
import com.nodiumhosting.vaultmapper.gui.component.Slider;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.util.Clamp;
import it.unimi.dsi.fastutil.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class VaultMapperConfigScreen extends Screen {
    private static final boolean webMapEnabled = ClientConfig.WEBMAP_ENABLED.get();

    public VaultMapperConfigScreen() {
        super(new TextComponent("Vault Mapper Config"));
    }

    // copied block from overlay renderer, should move elsewhere
    private static int parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }

            if (hexColor.length() == 6) {
                hexColor = "FF" + hexColor;  // Add full opacity if not specified
            }

            // Cast to int to use it as a 32-bit ARGB color
            return (int) Long.parseLong(hexColor, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF; // Default color (white with full opacity)
        }
    }

    private int getScaledY(float y) {
        float height = Minecraft.getInstance().getWindow().getGuiScaledHeight(); //Minecraft.getInstance().getWindow().getHeight() / 2;
        float piece = height / 16;
        float scaledY = piece * y;
        return (int) scaledY;
    }

    protected void init() {
        super.init();

        int elHeight = getScaledY(1) / 2;
        int elWidth = 200;
        int width = 100;
        int elWidthColor = width - elHeight - 5;

        Button mapEnabledButton = new Button(this.width / 2 - 100, getScaledY(1.25f), elWidth, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("Map Enabled: " + ClientConfig.MAP_ENABLED.get()), button -> {
            ClientConfig.MAP_ENABLED.set(!ClientConfig.MAP_ENABLED.get());
            ClientConfig.SPEC.save();
            button.setMessage(new TextComponent("Map Enabled: " + ClientConfig.MAP_ENABLED.get()));
        });
        this.addRenderableWidget(mapEnabledButton);

        Button webMapEnabledButton = new Button(this.width / 2 - 100, getScaledY(2), elWidth, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("WebMap Enabled: " + ClientConfig.WEBMAP_ENABLED.get()), button -> {
            ClientConfig.WEBMAP_ENABLED.set(!ClientConfig.WEBMAP_ENABLED.get());
            ClientConfig.SPEC.save();
            button.setMessage(new TextComponent("WebMap Enabled: " + ClientConfig.WEBMAP_ENABLED.get()));
        });
        this.addRenderableWidget(webMapEnabledButton);

        Function<Float, String> mapScaleGetter = (value) -> {
            int valueInt = (int) value;
            float floatValue = (float) valueInt / 10;
            String stringValue = floatValue + "x";
//            if (valueInt == 10) {
//                stringValue += " (Default)";
//            }
            return stringValue;
        };

        Slider mapScale = new Slider(this.width / 2 + 10, getScaledY(3), "", ClientConfig.MAP_SCALE.get(),30,3, mapScaleGetter, width, elHeight);
        this.addRenderableWidget(mapScale);

        EditBox mapXOffset = new EditBox(this.font, this.width / 2 + 10, getScaledY(4), width, elHeight, new TextComponent("MAP_X_OFFSET"));
        mapXOffset.setValue(ClientConfig.MAP_X_OFFSET.get().toString());
        this.addRenderableWidget(mapXOffset);

        EditBox mapYOffset = new EditBox(this.font, this.width / 2 + 10, getScaledY(5), width, elHeight, new TextComponent("MAP_Y_OFFSET"));
        mapYOffset.setValue(ClientConfig.MAP_Y_OFFSET.get().toString());
        this.addRenderableWidget(mapYOffset);

        Function<Float, String> anchorGetterX = (value) -> {
            switch ((int) value) {
                case 0:
                    return "Left";
                case 1:
                    return "Left Center";
                case 2:
                    return "Center";
                case 3:
                    return "Right Center";
                case 4:
                    return "Right";
                default:
                    return "Unknown";
            }
        };
        Slider mapXAnchor = new Slider(this.width / 2 + 10, getScaledY(6), "", ClientConfig.MAP_X_ANCHOR.get(), 4, 0, anchorGetterX, width, elHeight);
        this.addRenderableWidget(mapXAnchor);

        Function<Float, String> anchorGetterY = (value) -> {
            switch ((int) value) {
                case 0:
                    return "Top";
                case 1:
                    return "Top Center";
                case 2:
                    return "Center";
                case 3:
                    return "Bottom Center";
                case 4:
                    return "Bottom";
                default:
                    return "Unknown";
            }
        };

        Slider mapYAnchor = new Slider(this.width / 2 + 10, getScaledY(7), "", ClientConfig.MAP_Y_ANCHOR.get(), 4, 0, anchorGetterY, width, elHeight);
        this.addRenderableWidget(mapYAnchor);

        ColorPicker colorPicker = new ColorPicker(Clamp.clamp(this.width / 2 + 200, 0, this.width - 200), getScaledY(7), 200, 200, parseColor("#000000"), button -> {
        });
        colorPicker.visible = false;
        this.addRenderableWidget(colorPicker);

        EditBox pointerColor = new EditBox(this.font, this.width / 2 + 10, getScaledY(8), elWidthColor, elHeight, new TextComponent("POINTER_COLOR"));
        pointerColor.setValue(ClientConfig.POINTER_COLOR.get());
        this.addRenderableWidget(pointerColor);
        ColorButton pointerColorPicker = new ColorButton(this.width / 2 + elWidthColor + 5 + 10, getScaledY(8), elHeight, elHeight, parseColor(ClientConfig.POINTER_COLOR.get()), button -> {

        }, pointerColor, colorPicker);
        this.addRenderableWidget(pointerColorPicker);
        pointerColor.setResponder((value) -> {
            pointerColorPicker.setColor(parseColor(value));
        });

        EditBox roomColor = new EditBox(this.font, this.width / 2 + 10, getScaledY(9), elWidthColor, elHeight, new TextComponent("ROOM_COLOR"));
        roomColor.setValue(ClientConfig.ROOM_COLOR.get());
        this.addRenderableWidget(roomColor);
        ColorButton roomColorPicker = new ColorButton(this.width / 2 + elWidthColor + 5 + 10, getScaledY(9), elHeight, elHeight, parseColor(ClientConfig.ROOM_COLOR.get()), button -> {

        }, roomColor, colorPicker);
        this.addRenderableWidget(roomColorPicker);
        roomColor.setResponder((value) -> {
            roomColorPicker.setColor(parseColor(value));
        });

        EditBox startRoomColor = new EditBox(this.font, this.width / 2 + 10, getScaledY(10), elWidthColor, elHeight, new TextComponent("START_ROOM_COLOR"));
        startRoomColor.setValue(ClientConfig.START_ROOM_COLOR.get());
        this.addRenderableWidget(startRoomColor);
        ColorButton startRoomColorPicker = new ColorButton(this.width / 2 + elWidthColor + 5 + 10, getScaledY(10), elHeight, elHeight, parseColor(ClientConfig.START_ROOM_COLOR.get()), button -> {

        }, startRoomColor, colorPicker);
        this.addRenderableWidget(startRoomColorPicker);
        startRoomColor.setResponder((value) -> {
            startRoomColorPicker.setColor(parseColor(value));
        });

        EditBox markedRoomColor = new EditBox(this.font, this.width / 2 + 10, getScaledY(11), elWidthColor, elHeight, new TextComponent("MARKED_ROOM_COLOR"));
        markedRoomColor.setValue(ClientConfig.MARKED_ROOM_COLOR.get());
        this.addRenderableWidget(markedRoomColor);
        ColorButton markedRoomColorPicker = new ColorButton(this.width / 2 + elWidthColor + 5 + 10, getScaledY(11), elHeight, elHeight, parseColor(ClientConfig.MARKED_ROOM_COLOR.get()), button -> {

        }, markedRoomColor, colorPicker);
        this.addRenderableWidget(markedRoomColorPicker);
        markedRoomColor.setResponder((value) -> {
            markedRoomColorPicker.setColor(parseColor(value));
        });

        EditBox inscriptionRoomColor = new EditBox(this.font, this.width / 2 + 10, getScaledY(12), elWidthColor, elHeight, new TextComponent("INSCRIPTION_ROOM_COLOR"));
        inscriptionRoomColor.setValue(ClientConfig.INSCRIPTION_ROOM_COLOR.get());
        this.addRenderableWidget(inscriptionRoomColor);
        ColorButton inscriptionRoomColorPicker = new ColorButton(this.width / 2 + elWidthColor + 5 + 10, getScaledY(12), elHeight, elHeight, parseColor(ClientConfig.INSCRIPTION_ROOM_COLOR.get()), button -> {

        }, inscriptionRoomColor, colorPicker);
        this.addRenderableWidget(inscriptionRoomColorPicker);
        inscriptionRoomColor.setResponder((value) -> {
            inscriptionRoomColorPicker.setColor(parseColor(value));
        });
        Checkbox showInscription = new Checkbox(this.width / 2 + elWidthColor + 5 + 10 + elHeight + 5 - 2, getScaledY(12) - 2, 20, 20, new TextComponent(""), ClientConfig.SHOW_INSCRIPTIONS.get());
        this.addRenderableWidget(showInscription);

        Button saveButton = new Button(this.width / 2 - 100, getScaledY(13), 200, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("Save"), button -> {
            try {
                ClientConfig.MAP_X_OFFSET.set(Integer.parseInt(mapXOffset.getValue()));
            } catch (NumberFormatException e) {
                mapXOffset.setValue("0");
                ClientConfig.MAP_X_OFFSET.set(0);
            }
            try {
                ClientConfig.MAP_Y_OFFSET.set(Integer.parseInt(mapYOffset.getValue()));
            } catch (NumberFormatException e) {
                mapYOffset.setValue("0");
                ClientConfig.MAP_Y_OFFSET.set(0);
            }
            ClientConfig.MAP_SCALE.set(mapScale.sliderValue);
            ClientConfig.MAP_X_ANCHOR.set(mapXAnchor.sliderValue);
            ClientConfig.MAP_Y_ANCHOR.set(mapYAnchor.sliderValue);
            ClientConfig.POINTER_COLOR.set(pointerColor.getValue());
            ClientConfig.ROOM_COLOR.set(roomColor.getValue());
            ClientConfig.START_ROOM_COLOR.set(startRoomColor.getValue());
            ClientConfig.MARKED_ROOM_COLOR.set(markedRoomColor.getValue());
            ClientConfig.INSCRIPTION_ROOM_COLOR.set(inscriptionRoomColor.getValue());
            ClientConfig.SHOW_INSCRIPTIONS.set(showInscription.selected());

            ClientConfig.SPEC.save();

            VaultMapOverlayRenderer.onWindowResize();

            // send config to webmap
            VaultMapper.wsServer.sendConfig();
        });
        this.addRenderableWidget(saveButton);

        Button resetButton = new Button(this.width / 2 - 100, getScaledY(14), 200, Math.min((getScaledY(1) / 3) * 2, 20), new TextComponent("Reset"), button -> {
            mapScale.sliderValue = 10;
            mapXOffset.setValue("0");
            mapYOffset.setValue("0");
            mapXAnchor.sliderValue = 4;
            mapYAnchor.sliderValue = 4;
            pointerColor.setValue("#00FF00");
            roomColor.setValue("#0000FF");
            startRoomColor.setValue("#FF0000");
            markedRoomColor.setValue("#FF00FF");
            inscriptionRoomColor.setValue("#FFFF00");
            if (!showInscription.selected()) {
                showInscription.onPress();
            }

            ClientConfig.MAP_SCALE.set(10);
            ClientConfig.MAP_X_OFFSET.set(0);
            ClientConfig.MAP_Y_OFFSET.set(0);
            ClientConfig.MAP_X_ANCHOR.set(4);
            ClientConfig.MAP_Y_ANCHOR.set(4);
            ClientConfig.POINTER_COLOR.set("#00FF00");
            ClientConfig.ROOM_COLOR.set("#0000FF");
            ClientConfig.START_ROOM_COLOR.set("#FF0000");
            ClientConfig.MARKED_ROOM_COLOR.set("#FF00FF");
            ClientConfig.INSCRIPTION_ROOM_COLOR.set("#FFFF00");
            ClientConfig.SHOW_INSCRIPTIONS.set(true);

            ClientConfig.SPEC.save();

            VaultMapOverlayRenderer.onWindowResize();

            // send config to webmap
            VaultMapper.wsServer.sendConfig();
        });
        this.addRenderableWidget(resetButton);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);

        this.font.draw(pose, "Vault Mapper Config", this.width / 2 - this.font.width("Vault Mapper Config") / 2, 20, 0xFFFFFFFF);

        if (webMapEnabled != ClientConfig.WEBMAP_ENABLED.get()) {
            // draw warning to restart the game
            this.font.draw(pose, "Changes require a game restart", this.width / 2 - this.font.width("Changes require a game restart") / 2, 30, 0xFFFF0000);
        }

        // labels
        int offsetY = getScaledY(1) / 2;
        offsetY = getScaledY(1) / 4;
        this.font.draw(pose,"Map Scale",this.width/2-110,getScaledY(3)+offsetY,0xFFFFFFFF);
        this.font.draw(pose, "Map X Offset", this.width / 2 - 110, getScaledY(4) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map Y Offset", this.width / 2 - 110, getScaledY(5) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map X Anchor", this.width / 2 - 110, getScaledY(6) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Map Y Anchor", this.width / 2 - 110, getScaledY(7) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Pointer Color", this.width / 2 - 110, getScaledY(8) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Room Color", this.width / 2 - 110, getScaledY(9) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Start Room Color", this.width / 2 - 110, getScaledY(10) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Marked Room Color", this.width / 2 - 110, getScaledY(11) + offsetY, 0xFFFFFFFF);
        this.font.draw(pose, "Inscription Room Color", this.width / 2 - 110, getScaledY(12) + offsetY, 0xFFFFFFFF);

        super.render(pose, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
    }

    @Override
    public void onClose() {
        // Stop any handlers here

        // Call last in case it interferes with the override
        super.onClose();

        ColorButton.clearListeners();
    }

    @Override
    public void removed() {
        // Reset initial states here

        // Call last in case it interferes with the override
        super.removed();

        ColorButton.clearListeners();

    }
}
