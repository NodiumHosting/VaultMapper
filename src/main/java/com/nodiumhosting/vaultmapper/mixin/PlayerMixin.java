package com.nodiumhosting.vaultmapper.mixin;

import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method="drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at=@At("RETURN"))
    public void dropitem(ItemStack p_36177_, boolean p_36178_, CallbackInfoReturnable<ItemEntity> cir) {
        VaultMapper.LOGGER.info("DROPPED STUFF");
    }
}
