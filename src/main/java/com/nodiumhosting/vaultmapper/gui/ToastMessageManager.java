package com.nodiumhosting.vaultmapper.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TextComponent;

public class ToastMessageManager {
    public static void displayToast(String message) {
        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TextComponent("VaultMapper"), new TextComponent(message)));
    }
}
