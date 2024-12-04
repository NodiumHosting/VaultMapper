package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.snapshots.MapCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.logging.Logger;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class NetworkEvent {
    // NEEDS MORE TESTING

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void loginHandler(ClientPlayerNetworkEvent.LoggedInEvent event) {
        Logger.getAnonymousLogger().info("logged in, player:" + event.getPlayer().getName().getString());
        if (Objects.requireNonNull(event.getPlayer()).level.dimension().location().getNamespace().equals("the_vault")) {
            MapCache.readCache();
            VaultMap.enabled = true;
            VaultMapOverlayRenderer.enabled = true;
            VaultMap.startSync(event.getPlayer().getUUID().toString(), event.getPlayer().level.dimension().location().getPath());
        } else {
            VaultMapOverlayRenderer.enabled = false;
            VaultMap.enabled = false;
            VaultMap.resetMap();
        }
    }


    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void logoutHandler(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        Logger.getAnonymousLogger().info("logged off");
        VaultMapOverlayRenderer.enabled = false;
        VaultMap.enabled = false;
        VaultMap.stopSync();
    }
}
