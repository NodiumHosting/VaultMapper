package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.gui.overlay.VaultMapOverlay;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DimensionChangeEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();

        String dimensionNamespace = event.getTo().location().getNamespace(); // namespace

        VaultMapOverlay.resetMap();

        if (dimensionNamespace.equals("the_vault")) {
            Direction playerDirection = Objects.requireNonNull(player).getDirection();
            player.sendMessage(new TextComponent("Player direction: " + playerDirection), player.getUUID());
            player.sendMessage(new TextComponent("Player position: " + player.position()), player.getUUID());
            VaultMapOverlay.enabled = true;
        }
        else {
            VaultMapOverlay.enabled = false;
        }
    }
}
