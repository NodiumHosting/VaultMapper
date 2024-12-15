package com.nodiumhosting.vaultmapper.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.auth.Token;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin({JoinMultiplayerScreen.class})
public class JoinMultiplayerScreenMixin extends Screen {
    protected JoinMultiplayerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(
            method = {"init"},
            at = {@At("TAIL")}
    )
    protected void init(CallbackInfo ci) {
        boolean tokenExists = Token.hasToken();

        MutableComponent textAuth = new TextComponent("VaultMapper Sync: ").withStyle(Style.EMPTY).append(new TextComponent("✓").withStyle(Style.EMPTY.applyFormats(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        MutableComponent textNoAuth = new TextComponent("VaultMapper Sync: ").withStyle(Style.EMPTY).append(new TextComponent("✗").withStyle(Style.EMPTY.applyFormats(ChatFormatting.RED, ChatFormatting.BOLD)));

        Component text = tokenExists ? textAuth : textNoAuth;

        Button configScreenButton = new Button(
                this.width - 126, 6, 120, 20, text, button -> ConnectScreen.startConnecting(Objects.requireNonNull(Minecraft.getInstance().screen), Minecraft.getInstance(), ServerAddress.parseString("127.0.0.1:25565"), null)
        );
        this.addRenderableWidget(configScreenButton);
    }

    @Inject(
            method = {"render"},
            at = {@At("TAIL")}
    )
    protected void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        boolean tokenExists = Token.hasToken();
        if (tokenExists) return;

        FormattedCharSequence text = FormattedCharSequence.forward("You are currently not authenticated with VaultMapper Sync, please authenticate to use this feature.", Style.EMPTY.applyFormats(ChatFormatting.DARK_GRAY));
        this.font.draw(pPoseStack, text, this.width - this.minecraft.font.width(text) - 126 - 6, 6, 0xFFFFFF);
    }
}