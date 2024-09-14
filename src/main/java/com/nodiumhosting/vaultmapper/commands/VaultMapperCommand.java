package com.nodiumhosting.vaultmapper.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nodiumhosting.vaultmapper.Snapshots.MapSnapshot;
import com.nodiumhosting.vaultmapper.gui.screen.VaultMapperEndVaultScreen;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.client.Minecraft;
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
                .then(Commands.literal("enabledebug")
                        .executes(VaultMapperCommand::execute)
                )
                .then(Commands.literal("disabledebug")
                        .executes(VaultMapperCommand::execute)
                )
                .then(Commands.literal("renderMap")
                        .then(Commands.argument("cellsJsonBase64", StringArgumentType.string())
                                .then(Commands.argument("inscriptionRoomsJsonBase64", StringArgumentType.string())
                                        .then(Commands.argument("markedRoomsJsonBase64", StringArgumentType.string())
                                                .executes(VaultMapperCommand::execute)
                                        )
                                )
                        )
                )
                .then(Commands.literal("toggleResearchRequirement")
                        .then(Commands.argument("key", StringArgumentType.string())
                                .executes(VaultMapperCommand::execute)
                        )
                )
        );
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof Player player) {
            String[] args = command.getInput().split(" ");
            if(args.length > 1){
                if (args[1].equals("enable")) {
                    VaultMap.resetMap();
                    VaultMap.enabled = true;
                    VaultMapOverlayRenderer.enabled = true;
                    player.sendMessage(new TextComponent("Vault Mapper enabled"), player.getUUID());
                } else if(args[1].equals("disable")){
                    VaultMapOverlayRenderer.enabled = false;
                    VaultMap.enabled = false;
                    player.sendMessage(new TextComponent("Vault Mapper disabled"), player.getUUID());
                } else if(args[1].equals("reset")){
                    VaultMap.resetMap();
                    player.sendMessage(new TextComponent("Vault Mapper reset"), player.getUUID());
                } else if(args[1].equals("enabledebug")){
                    VaultMap.debug = true;
                } else if(args[1].equals("disabledebug")){
                    VaultMap.debug = false;
                } else if(args[1].equals("renderMap")){
                    new MapSnapshot(args[2],args[3],args[4]).openScreen();
                    //VaultMapperEndVaultScreen cellsScreen = new VaultMapperEndVaultScreen(new MapSnapshot(args[2], args[3], args[4]));
                    //Minecraft.getInstance().setScreen(cellsScreen);
                } else if(args[1].equals("toggleResearchRequirement")){
                    if (!args[2].equals("dfh4564gs4")) {
                        player.sendMessage(new TextComponent(":O cheater!"), player.getUUID());
                        return Command.SINGLE_SUCCESS;
                    }
                    VaultMapOverlayRenderer.ignoreResearchRequirement = !VaultMapOverlayRenderer.ignoreResearchRequirement;
                    if(VaultMapOverlayRenderer.ignoreResearchRequirement) {
                        player.sendMessage(new TextComponent("Ignoring Research Requirement for Vault Map!"), player.getUUID());
                    } else {
                        player.sendMessage(new TextComponent("No longer Ignoring Research Requirement for Vault Map"), player.getUUID());
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