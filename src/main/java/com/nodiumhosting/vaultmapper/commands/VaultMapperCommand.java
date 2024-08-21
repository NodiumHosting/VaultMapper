package com.nodiumhosting.vaultmapper.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class VaultMapperCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("vaultmapper")
                .executes(VaultMapperCommand::execute)
                .then(Commands.literal("enable")
                        .executes(VaultMapperCommand::execute)
                )
                .then(Commands.literal("disable")
                        .executes(VaultMapperCommand::execute)
                )
                .then(Commands.literal("reset")
                        .executes(VaultMapperCommand::execute)
                )
                .then(Commands.literal("important")
                        .executes(VaultMapperCommand::execute)
                )
        );
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof Player player) {
            String[] args = command.getInput().split(" ");
            if(args.length > 1){
                if(args[1].equals("enable")){
                    VaultMap.resetMap();
                    VaultMap.enabled = true;
                    player.sendMessage(new TextComponent("Vault Mapper enabled"), player.getUUID());
                } else if(args[1].equals("disable")){
                    VaultMapOverlayRenderer.enabled = false;
                    VaultMap.enabled = false;
                    player.sendMessage(new TextComponent("Vault Mapper disabled"), player.getUUID());
                } else if(args[1].equals("reset")){
                    VaultMap.resetMap();
                    player.sendMessage(new TextComponent("Vault Mapper reset"), player.getUUID());
                } else if (args[1].equals("important")) {
                    int x = (int) Math.floor(player.getX() / 47);
                    int z = (int) Math.floor(player.getZ() / 47);
                    try {
                        VaultMap.toggleImportantRoom(x, z);
                        player.sendMessage(new TextComponent("Toggled important room at " + x + ", " + z), player.getUUID());
                    } catch (NumberFormatException e) {
                        player.sendMessage(new TextComponent("Invalid coordinates"), player.getUUID());
                    }
                } else {
                    player.sendMessage(new TextComponent("Usage: /vaultmapper <enable|disable|reset>"), player.getUUID());
                }
            } else {
                player.sendMessage(new TextComponent("Usage: /vaultmapper <enable|disable|reset>"), player.getUUID());
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}