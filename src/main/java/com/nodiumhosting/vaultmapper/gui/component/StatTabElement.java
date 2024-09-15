package com.nodiumhosting.vaultmapper.gui.component;

import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.RenderIndexedElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.summary.element.VaultExitTabContainerElement;
import iskallia.vault.init.ModTextureAtlases;

import java.util.List;
import java.util.function.Supplier;

public class StatTabElement extends ElasticContainerElement<StatTabElement>{
    private final Runnable onClick;

    public StatTabElement(ISpatial spatial, IRenderedElement iconElement, Supplier<Boolean> selected, Runnable onClick, boolean disabled) {
        super(spatial);
        this.onClick = onClick;
        TextureAtlasRegion TAB_BACKGROUND_LEFT = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/tab_background_left"));
        TextureAtlasRegion TAB_BACKGROUND_LEFT_SELECTED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/tab_background_left_selected"));
        TextureAtlasRegion TAB_BACKGROUND_LEFT_DISABLED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/tab_background_left_disabled"));

        this.addElement(new RenderIndexedElement(Spatials.positionX(-397), List.of(new TextureAtlasElement(TAB_BACKGROUND_LEFT), new TextureAtlasElement(Spatials.positionX(-4), TAB_BACKGROUND_LEFT_SELECTED), new TextureAtlasElement(TAB_BACKGROUND_LEFT_DISABLED)), () -> {
            return disabled ? 2 : ((Boolean)selected.get() ? 1 : 0);
        }));
        this.addElement(iconElement);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        this.onClick.run();
        return true;
    }
}
