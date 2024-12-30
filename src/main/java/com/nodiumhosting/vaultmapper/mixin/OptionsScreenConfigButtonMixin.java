package com.nodiumhosting.vaultmapper.mixin;

import com.nodiumhosting.vaultmapper.gui.screen.VaultMapperConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OptionsScreen.class, remap = true)
public class OptionsScreenConfigButtonMixin extends Screen {
    protected OptionsScreenConfigButtonMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(
            method = {"init"},
            at = {@At("TAIL")}
    )
    protected void addVaultMapperConfigButton(CallbackInfo ci) {
        Button configScreenButton = new Button(
                this.width - 136, 6, 130, 20, new TextComponent("Vault Mapper Config"), button -> Minecraft.getInstance().setScreen(new VaultMapperConfigScreen())
        );
        this.addRenderableWidget(configScreenButton);
    }
}