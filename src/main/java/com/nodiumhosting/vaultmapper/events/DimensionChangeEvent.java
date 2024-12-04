package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.snapshots.MapCache;
import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DimensionChangeEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onDimChange(ClientPlayerNetworkEvent.RespawnEvent event) {
        String dimensionNamespace = event.getNewPlayer().level.dimension().location().getNamespace();

        MapSnapshot.lastSnapshotCache = MapSnapshot.takeSnapshot();
        VaultMap.resetMap();

        if (dimensionNamespace.equals("the_vault")) {
            VaultMap.enabled = true;
            VaultMapOverlayRenderer.enabled = true;
            VaultMap.startSync(event.getNewPlayer().getUUID().toString(), event.getNewPlayer().level.dimension().location().getPath());
        } else {
            VaultMap.enabled = false;
            VaultMapOverlayRenderer.enabled = false;
            VaultMap.stopSync();
            MapCache.deleteCache();
        }
    }
}
