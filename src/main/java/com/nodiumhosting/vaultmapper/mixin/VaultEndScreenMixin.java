package com.nodiumhosting.vaultmapper.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import com.nodiumhosting.vaultmapper.gui.component.MapContainerElement;
import com.nodiumhosting.vaultmapper.map.snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.client.gui.screen.summary.VaultExitContainerScreenData;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.*;
import java.util.function.Consumer;

@Mixin(VaultEndScreen.class)
public abstract class VaultEndScreenMixin extends AbstractElementScreen {

    @Unique
    MapContainerElement openMapContainerElement;

    @Unique
    LabelElement<?> openMapLabel;




    @Shadow
    @Final
    private VaultSnapshot snapshot;

    @Shadow @Final private boolean isHistory;

    public VaultEndScreenMixin(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }

    @Unique
    boolean registeredAlready = false;

    @ModifyArg(method= "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V",
    at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/summary/element/VaultExitTabContainerElement;<init>(Liskallia/vault/client/gui/framework/spatial/spi/IPosition;Ljava/util/function/Consumer;Z)V") )
    private Consumer<Integer> addContainerElement(Consumer<Integer> original, @Local VaultExitContainerScreenData screenData)
    {
        return index -> {
            if (!isHistory && !registeredAlready) {
                UUID uuid = snapshot.getEnd().get(Vault.ID);
                MapSnapshot.onVaultExit(uuid);
                registeredAlready = true;
            }
            if (openMapContainerElement == null || openMapLabel == null) {
                VaultEndScreen instance = ((VaultEndScreen)(Object)this);
                openMapContainerElement = (MapContainerElement)this.addElement((MapContainerElement)(new MapContainerElement(Spatials.positionX(4).width(-7).height(-16), screenData.getSnapshot().getEnd().get(Vault.ID))).layout((screen, gui, parent, world) -> {
                    world.translateX(gui.left() + 2 - 26 + 7).translateY(instance.getTabContentSpatial().bottom()).width(world.width() + gui.right() - world.x() + 7).height(world.height() + gui.height() - 22);
                }));
                openMapLabel = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.zero(), (new TextComponent("Vault Map")).withStyle(ChatFormatting.BLACK), LabelTextStyle.left())).layout((screen, gui, parent, world) -> {
                    world.translateX(gui.left() - 8 - 26 + 13).translateY(48).translateZ(2);
                }));
            }
                openMapContainerElement.setEnabled(index == 5);
                openMapContainerElement.setVisible(index == 5);
                openMapLabel.setEnabled(index == 5);
                openMapLabel.setVisible(index == 5);
            original.accept(index);
        };
    }

}
