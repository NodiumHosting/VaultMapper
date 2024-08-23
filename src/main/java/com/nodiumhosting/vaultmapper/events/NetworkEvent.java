package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class NetworkEvent {
    // NEEDS MORE TESTING

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void loginHandler(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer().level.dimension().location().getNamespace().equals("the_vault")) {
            VaultMap.enabled = true;
        } else {
            VaultMapOverlayRenderer.enabled = false;
            VaultMap.enabled = false;
            VaultMap.resetMap();
        }
    }
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void logoutHandler(PlayerEvent.PlayerLoggedOutEvent event) {
        VaultMapOverlayRenderer.enabled = false;
        VaultMap.enabled = false;
    }
}
