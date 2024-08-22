package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DimensionChangeEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onDimChange(ClientPlayerNetworkEvent.RespawnEvent event) {
        String dimensionNamespace = event.getNewPlayer().level.dimension().location().getNamespace();

        VaultMap.resetMap();

        if (dimensionNamespace.equals("the_vault")) {
            VaultMap.enabled = true;
            VaultMapOverlayRenderer.enabled = true;
            VaultMap.doMapUpdate();
        }
        else {
            VaultMap.enabled = false;
            VaultMapOverlayRenderer.enabled = false;
        }
    }
}
