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
        if (!event.getMessage().startsWith("!nbt")) return;
        event.setCanceled(true);

        Player player = Minecraft.getInstance().player;

        player.sendMessage(new TextComponent("e"), player.getUUID());

        Direction playerDirection = Objects.requireNonNull(player).getDirection();

//        player.sendMessage(new TextComponent("Player direction: " + playerDirection), player.getUUID());

        HashMap<Direction, BlockPos> hologramBlocks = new HashMap<>();
        hologramBlocks.put(Direction.NORTH, new BlockPos(23, 27, 13));
        hologramBlocks.put(Direction.EAST, new BlockPos(33, 27, 23));
        hologramBlocks.put(Direction.WEST, new BlockPos(13, 27, 23));
        hologramBlocks.put(Direction.SOUTH, new BlockPos(23, 27, 33));

        BlockPos hologramBlockPos = hologramBlocks.get(playerDirection);

//        player.sendMessage(new TextComponent("Hologram block position: " + hologramBlockPos), player.getUUID());

        BlockState hologramBlockState = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).clientLevel).getChunk(hologramBlockPos).getBlockState(hologramBlockPos);
        if (!Objects.equals(hologramBlockState.getBlock().getRegistryName(), new ResourceLocation("the_vault:hologram"))) {
            return;
        }
//        player.sendMessage(new TextComponent("Hologram block registry name: " + hologramBlockState.getBlock().getRegistryName()), player.getUUID()); // the_vault:hologram

        BlockEntity hologramBlock = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).getLevel()).getBlockEntity(hologramBlockPos);

        player.sendMessage(new TextComponent("Hologram block: " + Objects.requireNonNull(hologramBlock).serializeNBT()), player.getUUID());
    }
}
