package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ColorButton extends Button {
    private static List<Consumer<Boolean>> listeners = new ArrayList<>();

    public static EditBox selectedEditBox = null;

    private int color;
    private boolean selected = false;
    private EditBox editBox;
    private ColorPicker picker;

    public ColorButton(int x, int y, int width, int height, int color, Button.OnPress pOnPress, EditBox editBox, ColorPicker picker) {
        super(x, y, width, height, new TextComponent(""), pOnPress);
        this.color = color;
        this.editBox = editBox;
        this.picker = picker;

        listeners.add((Boolean b) -> {
            this.selected = b;
        });
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        if (this.visible)
        {
            GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, selected ? 0xFFFFFFFF : 0xFFA0A0A0);
            GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
            GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, this.color);
            this.renderBg(pPoseStack, Minecraft.getInstance(), pMouseX, pMouseY);
        }
    }

    public void setColor(int color) {
        this.color = color;

        if (this.selected) {
            picker.setSelectedColor(this.color);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            boolean b = !this.selected;
            listeners.forEach((Consumer<Boolean> l) -> l.accept(false));
            this.selected = b;

            if (this.selected) {
                selectedEditBox = this.editBox;
                picker.setSelectedColor(this.color);
                picker.visible = true;
            } else {
                selectedEditBox = null;
                picker.visible = false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static void clearListeners() {
        listeners.clear();
    }
}