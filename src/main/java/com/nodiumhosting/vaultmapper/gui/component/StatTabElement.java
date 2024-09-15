package com.nodiumhosting.vaultmapper.gui.component;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.RenderIndexedElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.summary.element.VaultExitTabContainerElement;

import java.util.List;
import java.util.function.Supplier;

public class StatTabElement extends ElasticContainerElement<StatTabElement>{
    private final Runnable onClick;

    public StatTabElement(ISpatial spatial, IRenderedElement iconElement, Supplier<Boolean> selected, Runnable onClick, boolean disabled) {
        super(spatial);
        this.onClick = onClick;
        this.addElement(new RenderIndexedElement(Spatials.zero(), List.of(new TextureAtlasElement(Spatials.positionX(3), ScreenTextures.TAB_BACKGROUND_RIGHT), new TextureAtlasElement(ScreenTextures.TAB_BACKGROUND_RIGHT_SELECTED), new TextureAtlasElement(Spatials.positionX(3), ScreenTextures.TAB_BACKGROUND_RIGHT_DISABLED)), () -> {
            return disabled ? 2 : ((Boolean)selected.get() ? 1 : 0);
        }));
        this.addElement(iconElement);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        this.onClick.run();
        return true;
    }
}
