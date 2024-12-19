package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.map.snapshots.MapCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class NetworkEvent {
    // NEEDS MORE TESTING

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void loginHandler(ClientPlayerNetworkEvent.LoggedInEvent event) {
        if (event.getPlayer().level.dimension().location().getNamespace().equals("the_vault") && VaultMapper.isVaultDimension(event.getPlayer().level.dimension().location().getPath())) {
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
        VaultMapOverlayRenderer.enabled = false;
        VaultMap.enabled = false;
        VaultMap.stopSync();
    }
}
