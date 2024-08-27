package com.nodiumhosting.vaultmapper.mixin;

import com.mojang.blaze3d.platform.Window;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRendererold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "onResize", at=@At("RETURN"))
    private void onResizeWindow(CallbackInfo ci) {
        VaultMapOverlayRenderer.onWindowResize();
    }
}
