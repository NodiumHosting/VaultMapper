package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.VaultMapper;
import it.unimi.dsi.fastutil.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class Slider extends Button {
    private final int defaultValue;
    public int sliderValue;
    public float sliderMaxValue;
    public float sliderMinValue;
    public boolean dragging = false;
    public String text;
    public Function<Float, String> optionGetter;

    public Slider(int x, int y, String text, int startingValue, float maxValue, float minValue, Function<Float, String> optionGetter, int width, int height, int defaultValue) {
        super(x, y, width, height, new TextComponent(text), (button) -> {
        });

        this.text = text;
        this.defaultValue = defaultValue;
        this.sliderValue = startingValue;
        this.sliderMaxValue = maxValue;
        this.sliderMinValue = minValue;
        this.optionGetter = optionGetter;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            Font font = Minecraft.getInstance().font;
            this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
            GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFFA0A0A0);
            GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
            GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + (int) ((this.sliderValue - sliderMinValue) / (sliderMaxValue - sliderMinValue) * (this.width - 2)), this.y + this.height - 1, 0xFF00FF00);
            this.renderBg(pPoseStack, Minecraft.getInstance(), pMouseX, pMouseY);
            TextComponent tc = new TextComponent(text + optionGetter.get(sliderValue) + " (" + sliderValue + ")");
            GuiComponent.drawCenteredString(pPoseStack, font, tc, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (int) ((pMouseX - this.x) / (float) this.width * (sliderMaxValue - sliderMinValue) + sliderMinValue + 0.5F);

                if (this.sliderValue < sliderMinValue) {
                    this.sliderValue = (int) sliderMinValue;
                }

                if (this.sliderValue > sliderMaxValue) {
                    this.sliderValue = (int) sliderMaxValue;
                }
            }

            GuiComponent.fill(new PoseStack(), this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
            GuiComponent.fill(new PoseStack(), this.x + 1, this.y + 1, this.x + (int) ((this.sliderValue - sliderMinValue) / (sliderMaxValue - sliderMinValue) * (this.width - 2)), this.y + this.height - 1, 0xFF00FF00);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1 && this.isMouseOver(pMouseX, pMouseY)) {
            this.sliderValue = defaultValue;
            return false; //idk what this is for
        }

        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.sliderValue = (int) ((pMouseX - this.x) / (float) this.width * (sliderMaxValue - sliderMinValue) + sliderMinValue + 0.5F);

            if (this.sliderValue < sliderMinValue) {
                this.sliderValue = (int) sliderMinValue;
            }

            if (this.sliderValue > sliderMaxValue) {
                this.sliderValue = (int) sliderMaxValue;
            }

            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.dragging = false;
        return true;
    }
}