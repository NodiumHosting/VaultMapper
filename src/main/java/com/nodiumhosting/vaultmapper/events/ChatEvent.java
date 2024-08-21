package com.nodiumhosting.vaultmapper.events;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Objects;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class ChatEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(ClientChatEvent event) {
        if (!event.getMessage().startsWith("!test")) return;
        event.setCanceled(true);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        player.sendMessage(new TextComponent("Test command received"), player.getUUID());
    }
}
