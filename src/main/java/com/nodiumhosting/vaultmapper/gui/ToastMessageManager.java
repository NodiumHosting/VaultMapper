package com.nodiumhosting.vaultmapper.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.jetbrains.annotations.NotNull;

public class ToastMessageManager {
    public static void displayToast(String message, int duration, int x, int y) {
        Minecraft.getInstance().getToasts().addToast(new Toast() {
            @Override
            public @NotNull Visibility render(@NotNull PoseStack poseStack, @NotNull ToastComponent toastComponent, long l) {
                toastComponent.getMinecraft().font.draw(poseStack, message, x, y, -1);
//                return l >= duration ? Visibility.HIDE : Visibility.SHOW;
                return Visibility.SHOW;
            }
        });
    }
}
