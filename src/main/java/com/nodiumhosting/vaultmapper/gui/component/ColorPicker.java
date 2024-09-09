package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

import java.awt.*;

public class ColorPicker extends Button {
    private int selectedColor;
    private int selectedX;
    private int selectedY;

    public ColorPicker(int x, int y, int width, int height, int selectedColor, OnPress pOnPress) {
        super(x, y, width, height, new TextComponent(""), pOnPress);
        this.selectedColor = selectedColor;
    }

    private static Color getColor(int w, int h, int i, int j) {
        Color c;
        if (j < (h / 9) * 4) {
            c = new Color(Color.HSBtoRGB((float) i / w, (float) j / ((h / 9) * 4), 1.0F));
        } else if (j < (h / 9) * 8) {
            c = new Color(Color.HSBtoRGB((float) i / w, 1.0F, 1.0F - (float) (j - (h / 9) * 4) / ((h / 9) * 4)));
        } else {
            c = new Color(Color.HSBtoRGB(0.0F, 0.0F, 1.0F - (float) i / w));
        }
        return c;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
            GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            int startX = this.x + 1;
            int startY = this.y + 1;
            int w = this.width - 2;
            int h = this.height - 2;

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    Color c = getColor(w, h, i, j);
                    int color = c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                    bufferBuilder.vertex(startX + i, startY + j + 1, 0.0F).color(color).endVertex();
                    bufferBuilder.vertex(startX + i + 1, startY + j + 1, 0.0F).color(color).endVertex();
                    bufferBuilder.vertex(startX + i + 1, startY + j, 0.0F).color(color).endVertex();
                    bufferBuilder.vertex(startX + i, startY + j, 0.0F).color(color).endVertex();
                }
            }

            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();

            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (Math.abs(i) == 2 && Math.abs(j) == 2) {
                        continue;
                    }

                    if (this.selectedX + i >= startX && this.selectedX + i <= startX + w && this.selectedY + j >= startY && this.selectedY + j <= startY + h) {
                        GuiComponent.fill(pPoseStack, this.selectedX + i, this.selectedY + j, this.selectedX + i + 1, this.selectedY + j + 1, 0xFF000000);
                    }
                }
            }

            this.renderBg(pPoseStack, Minecraft.getInstance(), pMouseX, pMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.selectedX = (int) pMouseX;
            this.selectedY = (int) pMouseY;

            int startX = this.x + 1;
            int startY = this.y + 1;
            int w = this.width - 2;
            int h = this.height - 2;
            int i = this.selectedX - startX;
            int j = this.selectedY - startY;
            Color c = getColor(w, h, i, j);
            this.selectedColor = c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();

            ColorButton.selectedEditBox.setValue("#" + Integer.toHexString(this.selectedColor).substring(2));

            return true;
        } else {
            this.visible = false;
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDX, double pDY) {
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDX, pDY)) {
            int startX = this.x + 1;
            int startY = this.y + 1;
            int endX = this.x + this.width - 1;
            int endY = this.y + this.height - 1;

            boolean isInBounds = pMouseX >= startX && pMouseX <= endX && pMouseY >= startY && pMouseY <= endY;

            if (isInBounds) {
                this.selectedX = (int) pMouseX;
                this.selectedY = (int) pMouseY;

                int w = this.width - 2;
                int h = this.height - 2;
                int i = this.selectedX - startX;
                int j = this.selectedY - startY;
                Color c = getColor(w, h, i, j);
                this.selectedColor = c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();

                ColorButton.selectedEditBox.setValue("#" + Integer.toHexString(this.selectedColor).substring(2));

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;

        int startX = this.x + 1;
        int startY = this.y + 1;
        int w = this.width - 2;
        int h = this.height - 2;

        boolean found = false;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Color c = getColor(w, h, i, j);
                int cColor = c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                if (cColor == color) {
                    this.selectedX = startX + i;
                    this.selectedY = startY + j;

                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            this.selectedX = startX;
            this.selectedY = startY;
        }
    }
}