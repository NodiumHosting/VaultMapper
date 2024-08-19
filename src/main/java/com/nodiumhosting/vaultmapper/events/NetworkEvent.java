package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.gui.overlay.VaultMapOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class NetworkEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void loginHandler(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer().level.dimension().location().getNamespace().equals("the_vault")) {
            VaultMapOverlay.enabled = true;
        } else {
            VaultMapOverlay.enabled = false;
            VaultMapOverlay.resetMap();
        }
    }
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void logoutHandler(PlayerEvent.PlayerLoggedOutEvent event) {
        VaultMapOverlay.enabled = false;
    }
}
