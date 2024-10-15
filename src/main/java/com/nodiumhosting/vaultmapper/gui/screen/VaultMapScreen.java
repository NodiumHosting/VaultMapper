package com.nodiumhosting.vaultmapper.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.component.MapContainerElement;
import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.framework.text.TextBorder;
import iskallia.vault.client.gui.screen.player.element.PointLabelContainerElement;
import iskallia.vault.client.gui.screen.summary.element.OverviewContainerElement;
import iskallia.vault.client.gui.screen.summary.element.VaultExitTabContainerElement;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.init.ModSounds;
import iskallia.vault.network.message.ServerboundOpenHistoricMessage;
import iskallia.vault.network.message.VaultPlayerStatsMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VaultMapScreen extends AbstractElementScreen {

    ButtonElement<?> closeButton;
    MapContainerElement openMapContainerElement;

    LabelElement<?> openMapLabel;


    public VaultMapScreen(String fileName) {
        this(MapSnapshot.from(fileName));
    }

    public VaultMapScreen(UUID uuid) {
        this(MapSnapshot.from(uuid));
    }

    public VaultMapScreen(Optional<MapSnapshot> snapshot) {
        super(new TextComponent("Map"), ScreenRenderers.getBuffered(), ScreenTooltipRenderer::create);
        this.setGuiSize(Spatials.size(350, 186));
        openMapContainerElement = (MapContainerElement)this.addElement((MapContainerElement)(new MapContainerElement(Spatials.positionX(4).width(-7).height(-16), snapshot )).layout((screen, gui, parent, world) -> {
            world.translateX(gui.left() + 2 - 26 + 7).translateY(this.getTabContentSpatial().bottom()).width(world.width() + gui.right() - world.x() + 7).height(world.height() + gui.height() - 22);
        }));
        openMapLabel = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.zero(), (new TextComponent("Map Preview")).withStyle(ChatFormatting.BLACK), LabelTextStyle.left())).layout((screen, gui, parent, world) -> {
            world.translateX(gui.left() - 8 - 26 + 13).translateY(48).translateZ(2);
        }));
        /*
        this.addElement(
                (NineSliceElement)new NineSliceElement(Spatials.positionY(-4).positionZ(-12).positionY(6), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)
                        .layout((screen, gui, parent, world) -> world.width(128).height(32).translateX((gui.right() - gui.left()) / 2 + gui.left() - 64))
        );
        */
        this.addElement(
                (NineSliceElement)new NineSliceElement(
                        Spatials.positionXY(0, 42).size(this.width, 19).height(this.getTabContentSpatial()), ScreenTextures.DEFAULT_WINDOW_BACKGROUND
                )
                        .layout(
                                (screen, gui, parent, world) -> world.translateX(gui.left() - 8 - 26 + 7).size(gui.width() + 16 + 26, this.getTabContentSpatial().height())
                        )
        );
        this.addElement(
                (NineSliceElement)new NineSliceElement(Spatials.positionY(-4).positionZ(-10), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)
                        .layout(
                                (screen, gui, parent, world) -> world.translateX(gui.x() - 26 + 7)
                                        .translateY(this.getTabContentSpatial().bottom())
                                        .size(gui.width() + 26, gui.height() - 10)
                        )
        );
        MutableComponent finalComponent = new TextComponent("Close").withStyle(ChatFormatting.WHITE);
        this.addElement(
                (LabelElement)new LabelElement(Spatials.zero(), finalComponent, LabelTextStyle.border4(ChatFormatting.BLACK).center())
                        .layout(
                                (screen, gui, parent, world) -> world.translateZ(2)
                                        .translateX(gui.right() - gui.left() + gui.left() - 26 - 1 - TextBorder.DEFAULT_FONT.get().width(finalComponent) / 2)
                                        .translateY(this.getTabContentSpatial().bottom() + gui.height() - 31)
                        )
        );


        this.closeButton = this.addElement(
                new ButtonElement<>(Spatials.zero(), ScreenTextures.BUTTON_CLOSE_TEXTURES, () -> {
                        this.onClose();
                })
                        .layout(
                                (screen, gui, parent, world) -> world.width(52)
                                        .height(19)
                                        .translateX(gui.right() - gui.left() + gui.left() - 52)
                                        .translateY(this.getTabContentSpatial().bottom() + gui.height() - 37)
                        )
        );

    }

    public ISpatial getTabContentSpatial() {
        int padLeft = 21;
        int padTop = 42;
        int width = this.width - padLeft * 2;
        int height = 19;
        return Spatials.positionXY(padLeft, padTop).size(width, height);
    }
    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
    @Override
    protected void layout(ISpatial parent) {
        super.layout(parent);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
