package com.nodiumhosting.vaultmapper.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.component.StatTabElement;
import com.nodiumhosting.vaultmapper.gui.component.VaultExitTabContainerMapElement;
import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.RenderIndexedElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.summary.element.VaultExitTabContainerElement;
import iskallia.vault.init.ModTextureAtlases;
import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(VaultExitTabContainerElement.class)
public abstract class MixinVaultExitTabContainerElement<E extends VaultExitTabContainerElement<E>> extends ElasticContainerElement<E> {
    protected MixinVaultExitTabContainerElement(ISpatial spatial) {
        super(spatial);
    }

    @Shadow
    private int selectedIndex;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addButtonIndex(IPosition position, Consumer selectedIndexChangeAction, boolean isCoop, CallbackInfo ci, @Local ObservableSupplier<Integer> selectedIndexObserver) {
        VaultExitTabContainerElement thisInstance = ((VaultExitTabContainerElement)(Object)this);

        TextureAtlasRegion textureAtlasRegion = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/map"));

        ((InvokerContainerElement)thisInstance).invokeAddElement(new StatTabElement(Spatials.positionZ(position).size(31, 28), new TextureAtlasElement(Spatials.positionXYZ(-392, 6, position.z() + 1), Spatials.size(17, 16), textureAtlasRegion), () -> {
            return selectedIndex == 5;
        }, () -> {
            selectedIndex = 5;
            selectedIndexObserver.ifChanged(selectedIndexChangeAction);
        }, false));
    }
}
