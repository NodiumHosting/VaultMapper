package com.nodiumhosting.vaultmapper.gui.component;

import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class EditBoxReset extends EditBox {
    private String defaultText;

    public EditBoxReset(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage, String defaultText) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        this.defaultText = defaultText;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1 && this.isMouseOver(pMouseX, pMouseY)) {
            this.setValue(defaultText);
            return false; //idk what this is for
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
