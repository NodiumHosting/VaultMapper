package com.nodiumhosting.vaultmapper.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.util.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(value = TitleScreen.class, remap = true)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(
            method = {"render"},
            at = {@At("TAIL")}
    )
    protected void addVaultMapperVersionInfo(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (UpdateChecker.IS_RUNNING_LATEST) {
            return;
        }
        String message = "VaultMapper update available! Current: v" + UpdateChecker.CURRENT_VERSION + ", Latest: v" + UpdateChecker.LATEST_VERSION;
        GuiComponent.drawCenteredString(pPoseStack, this.font, message, this.width / 2, 5, 0xFFFFFFFF);
    }
}
