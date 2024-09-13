package com.nodiumhosting.vaultmapper.mixin;


import com.nodiumhosting.vaultmapper.Snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.init.ModSounds;
import iskallia.vault.network.message.ServerboundOpenHistoricMessage;
import iskallia.vault.network.message.VaultPlayerStatsMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(VaultEndScreen.class)
public abstract class VaultEndScreenMixin extends AbstractElementScreen {
    @Unique
    protected ButtonElement<?> openMapButton;

    public VaultEndScreenMixin(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }


    @Inject(method = "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V", at=@At("TAIL"))
    private void addMaps(VaultSnapshot snapshot, Component title, UUID asPlayer, boolean isHistory, boolean fromLink, CallbackInfo ci) {
        UUID uuid = snapshot.getEnd().get(Vault.ID);
        if (!isHistory) {

            MapSnapshot.onVaultExit(uuid);
        }

       //Put button adding here
        //Button should:
        //  Optional<MapSnapshot> optMap = MapSnapshot.from(uuid);
        ////            if (optMap.isEmpty()) {
        ////                return; //or do whatever happens when theres no saved map
        ////            }
        ////            optMap.get().openScreen();
    }
}
