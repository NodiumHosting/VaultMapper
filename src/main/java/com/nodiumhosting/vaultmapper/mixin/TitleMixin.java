package com.nodiumhosting.vaultmapper.mixin;

import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleMixin {
    @Inject(at = @At("HEAD"), method = "init")
    private void init(CallbackInfo info) {
        VaultMapper.LOGGER.info("This line is printed by an example mod mixin!");
    }
}