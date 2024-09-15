package com.nodiumhosting.vaultmapper.mixin;


import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.framework.text.TextBorder;
import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.client.gui.screen.summary.VaultExitContainerScreenData;
import iskallia.vault.client.gui.screen.summary.element.*;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@Mixin(VaultEndScreen.class)
public abstract class VaultEndScreenMixin extends AbstractElementScreen {
    @Unique
    protected ButtonElement<?> openMapButton;

    @Shadow
    @Final
    private VaultSnapshot snapshot;

    @Shadow
    @Final
    private UUID asPlayer;

    public VaultEndScreenMixin(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }

    @Inject(method = "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V", at=@At("TAIL"))
    private void addMaps(VaultSnapshot snapshot, Component title, UUID asPlayer, boolean isHistory, boolean fromLink, CallbackInfo ci) {
        UUID uuid = snapshot.getEnd().get(Vault.ID);
        if (!isHistory) {

            MapSnapshot.onVaultExit(uuid);
        }
        VaultEndScreen instance = ((VaultEndScreen)(Object)this);
        Optional<MapSnapshot> optMap = MapSnapshot.from(uuid);
        if (optMap.isEmpty()) {
            return;
        }
        Component mapButtonText = new TextComponent("View Map").withStyle(ChatFormatting.WHITE);
        Component finalComponent = mapButtonText;
        this.addElement(
                (LabelElement)new LabelElement(Spatials.zero(), finalComponent, LabelTextStyle.border4(ChatFormatting.BLACK).center())
                        .layout(
                                (screen, gui, parent, world) -> world.translateZ(2)
                                        .translateX(gui.right() - gui.left() + gui.left() - 26 -79- 1 - TextBorder.DEFAULT_FONT.get().width(finalComponent) / 2)
                                        .translateY(instance.getTabContentSpatial().bottom() + gui.height() - 31)
                        )
        );
        this.openMapButton = this.addElement(
                new ButtonElement<>(Spatials.zero(), ScreenTextures.BUTTON_CLOSE_TEXTURES, () -> {
                    optMap.get().openScreen(Optional.of(instance));
                })
                        .layout(
                                (screen, gui, parent, world) -> world.width(52)
                                        .height(19)
                                        .translateX(gui.right() - gui.left() + gui.left() - 131)
                                        .translateY(instance.getTabContentSpatial().bottom() + gui.height() - 37)
                        )
                        .tooltip(
                                (tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                                    tooltipRenderer.renderTooltip(
                                            poseStack, List.of(new TextComponent("View Vault Mapper room history")), mouseX, mouseY, ItemStack.EMPTY, TooltipDirection.RIGHT
                                    );

                                    return false;
                                }
                        )
        );

       //Put button adding here
        //Button should:
        //  Optional<MapSnapshot> optMap = MapSnapshot.from(uuid);
        ////            if (optMap.isEmpty()) {
        ////                return; //or do whatever happens when theres no saved map
        ////            }
        ////            optMap.get().openScreen();

        //If it works, dont fix it
//        this.addElement((VaultExitTabContainerMapElement)(new VaultExitTabContainerMapElement(Spatials.positionXY(-3, 3), (index) -> {
//            if((int) index == 1) {
//                optMap.get().openScreen(Optional.of(instance));
//                Minecraft.getInstance().getSoundManager().play(
//                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
//                );
//            }
//        })).layout((screen, gui, parent, world) -> {
//            world.translateX(gui.left() - 43).translateY(instance.getTabContentSpatial().bottom());
//        }));
    }

    @ModifyArg(method= "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V",
    at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/summary/element/VaultExitTabContainerElement;<init>(Liskallia/vault/client/gui/framework/spatial/spi/IPosition;Ljava/util/function/Consumer;Z)V") )
    private Consumer<Integer> addContainerElement(Consumer<Integer> original)
    {
        return index -> {
            if(index == 5) {
                VaultEndScreen instance = (VaultEndScreen) (Object) this;
                VaultExitContainerScreenData screenData = new VaultExitContainerScreenData(snapshot, asPlayer);

                CrystalStatsContainerElement crystalStatsContainerElement = (CrystalStatsContainerElement)this.addElement((CrystalStatsContainerElement)(new CrystalStatsContainerElement(Spatials.positionX(4).width(-7).height(-16), screenData.getModifiers())).layout((screen, gui, parent, world) -> {
                    world.translateX(gui.left() + 2 - 26 + 7).translateY(instance.getTabContentSpatial().bottom()).width(world.width() + gui.right() - world.x() + 7).height(world.height() + gui.height() - 22);
                }));



                LabelElement<?> overviewLabel = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.zero(), (new TextComponent("Vault Map")).withStyle(ChatFormatting.BLACK), LabelTextStyle.left())).layout((screen, gui, parent, world) -> {
                    world.translateX(gui.left() - 8 - 26 + 13).translateY(48).translateZ(2);
                }));
                crystalStatsContainerElement.setEnabled(index == 5);
                crystalStatsContainerElement.setVisible(index == 5);
                overviewLabel.setEnabled(index == 5);
                overviewLabel.setVisible(index == 5);




//                UUID uuid = snapshot.getEnd().get(Vault.ID);
//                Optional<MapSnapshot> optMap = MapSnapshot.from(uuid);
//                if (optMap.isEmpty()) {
//                    return;
//                }
//                VaultEndScreen instance = (VaultEndScreen) (Object) this;
//                optMap.get().openScreen(Optional.of(instance));
//                Minecraft.getInstance().getSoundManager().play(
//                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
//                );
            }
            original.accept(index);
        };
    }

}
