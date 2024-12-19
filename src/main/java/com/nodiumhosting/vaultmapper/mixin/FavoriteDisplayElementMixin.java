package com.nodiumhosting.vaultmapper.mixin;

import com.nodiumhosting.vaultmapper.map.snapshots.MapSnapshot;
import iskallia.vault.client.gui.screen.summary.VaultHistoricDataScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(VaultHistoricDataScreen.FavoritesDisplayElement.class)
public class FavoriteDisplayElementMixin {

    @Shadow
    @Final
    private UUID vaultID;

    @ModifyArg(method= "<init>",
            at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/summary/VaultHistoricDataScreen$GrayedTextureAtlasElement;<init>(Liskallia/vault/client/gui/framework/spatial/spi/IPosition;Liskallia/vault/client/atlas/TextureAtlasRegion;Liskallia/vault/client/atlas/TextureAtlasRegion;Ljava/util/UUID;Ljava/lang/Runnable;)V") )
    private Runnable addFavoriteToMap(Runnable original) {
        return () -> {
            MapSnapshot.toggleFavorite(vaultID);
            original.run();
        };
    }
}
