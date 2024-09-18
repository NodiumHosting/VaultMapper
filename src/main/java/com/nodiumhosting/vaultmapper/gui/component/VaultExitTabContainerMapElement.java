package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.RenderIndexedElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.summary.element.VaultExitTabContainerElement;
import iskallia.vault.init.ModTextureAtlases;
import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VaultExitTabContainerMapElement<E extends VaultExitTabContainerElement<E>> extends ElasticContainerElement<E> {
    private int selectedIndex;

    public VaultExitTabContainerMapElement(IPosition position, Consumer<Integer> selectedIndexChangeAction) {
        super(Spatials.positionXYZ(position));
        ObservableSupplier<Integer> selectedIndexObserver = ObservableSupplier.of(() -> {
            return this.selectedIndex;
        }, Integer::equals);
        selectedIndexObserver.ifChanged(selectedIndexChangeAction);

        this.addElement(new VaultExitTabContainerMapElement.StatTabElement(Spatials.positionY(61).positionZ(position).size(31, 28), new TextureAtlasElement(Spatials.positionXYZ(4, 6, position.z() + 1), ScreenTextures.BLANK), () -> {
            return this.selectedIndex == 0;
        }, () -> {
            this.selectedIndex = 0;
            selectedIndexObserver.ifChanged(selectedIndexChangeAction);
        }, false, true));

        this.addElement(new VaultExitTabContainerMapElement.StatTabElement(Spatials.positionY(0).positionZ(position).size(31, 28), new TextureAtlasElement(Spatials.positionXYZ(8, 6, position.z() + 1), ScreenTextures.TAB_ICON_PORTAL_VAULT), () -> {
            return this.selectedIndex == 1;
        }, () -> {
            this.selectedIndex = 1;
            selectedIndexObserver.ifChanged(selectedIndexChangeAction);
            this.selectedIndex = 0;
            selectedIndexObserver.ifChanged(selectedIndexChangeAction);
        }, false, false));
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        //testing
        float width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        float height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        bufferBuilder.vertex(width / 2 - 1, 125 + 1, 0).color(0xffffffff).endVertex();
        bufferBuilder.vertex(width / 2 + 1, 125 + 1, 0).color(0xffffffff).endVertex();
        bufferBuilder.vertex(width / 2 + 1, 125 - 1, 0).color(0xffffffff).endVertex();
        bufferBuilder.vertex(width / 2 - 1, 125 - 1, 0).color(0xffffffff).endVertex();

        bufferBuilder.end();
        BufferUploader.end(bufferBuilder); // render the map

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        System.out.println("rendering the thing");
    }

    private static class StatTabElement extends ElasticContainerElement<VaultExitTabContainerMapElement.StatTabElement> {
        private final Runnable onClick;

        public StatTabElement(ISpatial spatial, IRenderedElement iconElement, Supplier<Boolean> selected, Runnable onClick, boolean disabled, boolean hidden) {
            super(spatial);
            this.onClick = onClick;
            this.addElement(new RenderIndexedElement(
                    Spatials.zero(),
                    List.of(
                            new TextureAtlasElement(Spatials.positionX(3), TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/tab_background_left"))), // Index 0: Default
                            new TextureAtlasElement(ScreenTextures.TAB_BACKGROUND_TOP_SELECTED), // Index 1: Selected - unused
                            new TextureAtlasElement(Spatials.positionX(3), ScreenTextures.TAB_BACKGROUND_TOP), // Index 2: Disabled - unused
                            new TextureAtlasElement(Spatials.positionX(3), ScreenTextures.BLANK) // Index 3: Hidden
                    ),
                    () -> {
                        if (hidden) {
                            return 3;
                        } else if (disabled) {
                            return 2;
                        } else if (selected.get()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
            ));
            this.addElement(iconElement);
        }

        public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
            this.onClick.run();
            return true;
        }
    }
}
