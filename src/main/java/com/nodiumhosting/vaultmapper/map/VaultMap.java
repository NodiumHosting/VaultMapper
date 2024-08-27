package com.nodiumhosting.vaultmapper.map;

import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMap {
    public static boolean enabled;
    public static boolean debug;

    static List<VaultCell> cells = new ArrayList<>();
    static List<VaultCell> inscriptionRooms = new ArrayList<>();
    static List<VaultCell> markedRooms = new ArrayList<>();

    static VaultCell startRoom = new VaultCell();
    static VaultCell currentRoom = new VaultCell(); // might not be needed

    public static void resetMap() {
        cells = new ArrayList<>();
        inscriptionRooms = new ArrayList<>();
        markedRooms = new ArrayList<>();
        startRoom = new VaultCell(CellType.ROOM, 0, 0);
        currentRoom = new VaultCell();
    }

    private static boolean isCurrentRoom(int x, int z) {
        return currentRoom.x == x && currentRoom.z == z;
    }

    private static boolean isWithinBounds(int x, int z) {
        return x >= -24 && x <= 24 && z >= -24 && z <= 24;
    }
    
    private static CellType getCellType(int x, int z) {
        if (Math.abs(x) % 2 == 0 && Math.abs(z) % 2 == 0) { // room
            return CellType.ROOM;
        } else if (Math.abs(x) % 2 == 1 && Math.abs(z) % 2 == 1) { //void
            return CellType.NONE;
        } else {
            return CellType.TUNNEL;
        }
    }

    private static TunnelType getTunnelType(int x, int z) {
        if (Math.abs(x) % 2 == 1 && z % 2 == 0) {
            return TunnelType.X_FACING;
        } else {
            return TunnelType.Z_FACING;
        }
    }

    private static boolean isNewCell(VaultCell new_cell, List<VaultCell> cell_list) {
        AtomicBoolean isNew = new AtomicBoolean(true);
        cell_list.forEach((cell) -> {
            if (cell.x == new_cell.x && cell.z == new_cell.z) isNew.set(false);
        });
        return isNew.get();
    }

    private static void updateMap() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);

        VaultCell newCell;
        CellType t = getCellType(playerRoomX, playerRoomZ);
        if (t == CellType.ROOM) {
            newCell = new VaultCell(getCellType(playerRoomX, playerRoomZ), playerRoomX, playerRoomZ); // update current room
        } else if (t == CellType.TUNNEL) {
            newCell = new VaultCell(getCellType(playerRoomX, playerRoomZ), getTunnelType(playerRoomX, playerRoomZ), playerRoomX, playerRoomZ);
        } else {
            newCell = new VaultCell();
        }
        currentRoom = newCell;
        VaultMapper.LOGGER.info(newCell.toString());

        if (isNewCell(newCell, cells)) {
            cells.add(newCell);
        }
        VaultMapper.LOGGER.info(cells.toString());

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(MovementInputUpdateEvent event) {
        if (!enabled) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);

        if (isWithinBounds(playerRoomX, playerRoomZ)) {
            //VaultMapper.LOGGER.info("within bounds");
            if (debug) Minecraft.getInstance().gui.setOverlayMessage(new TextComponent("Current room: " + playerRoomX + ", " + playerRoomZ + " Vault Map Data Size: " + cells.size()), false);
            if (!isCurrentRoom(playerRoomX, playerRoomZ)) { // if were in a different room
                updateMap();
            }
        }
    }

    public static void markCurrentCell() {
        Player player = Minecraft.getInstance().player;
        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
            player.sendMessage(new TextComponent("You can't use this outside of Vaults"), player.getUUID());
            return;
        }

        VaultMapper.LOGGER.info("Marking room");
        if (player == null) return;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);


        if (getCellType(playerRoomX, playerRoomZ) == CellType.ROOM) {
            VaultCell new_cell = new VaultCell(CellType.ROOM, playerRoomX, playerRoomZ);
            if (isNewCell(new_cell, markedRooms)) {
                markedRooms.add(new_cell);
                player.sendMessage(new TextComponent("The room has been marked"), player.getUUID());
            } else {
                player.sendMessage(new TextComponent("This room is already marked"), player.getUUID());
            }
        } else {
            player.sendMessage(new TextComponent("You can only mark rooms"), player.getUUID());
        }
    }

    public static void toggleRendering() {
        Player player = Minecraft.getInstance().player;
        if (VaultMapOverlayRenderer.enabled){
            VaultMapOverlayRenderer.enabled = false;
            return;
        }

        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
            player.sendMessage(new TextComponent("You can't use this outside of Vaults"), player.getUUID());
            return;
        }
        VaultMapOverlayRenderer.enabled = true;
    }
}
