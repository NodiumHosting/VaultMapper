package com.nodiumhosting.vaultmapper.map;

import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.abs;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMap {
    public static boolean enabled;
    public static boolean debug;

    static List<VaultCell> cells = new ArrayList<>();
    static List<VaultCell> inscriptionRooms = new ArrayList<>();
    static List<VaultCell> markedRooms = new ArrayList<>();

    static VaultCell startRoom = new VaultCell();
    static VaultCell currentRoom = new VaultCell(); // might not be needed

    static int defaultMapSize = 21; // map size in cells
    static int defaultCoordLimit = 10; // limit for coords, so a cell at -6 on x or z would be considered out of bounds and trigger action

    static int currentMapSize = defaultMapSize;
    static int currentCoordLimit = defaultCoordLimit; // initialize to default values, will change and reset
    static CompoundTag hologramData;
    static boolean hologramChecked;

    public static void resetMap() {
        cells = new ArrayList<>();
        inscriptionRooms = new ArrayList<>();
        markedRooms = new ArrayList<>();
        startRoom = new VaultCell(CellType.ROOM, 0, 0);
        currentRoom = new VaultCell();
        hologramChecked = false;
        hologramData = null;

        currentMapSize = defaultMapSize;
        currentCoordLimit = defaultCoordLimit;
    }

    private static boolean isCurrentRoom(int x, int z) {
        return currentRoom.x == x && currentRoom.z == z;
    }

    private static boolean isWithinBounds(int x, int z) {
        return x >= -24 && x <= 24 && z >= -24 && z <= 24;
    }

    private static CellType getCellType(int x, int z) {
        if (abs(x) % 2 == 0 && abs(z) % 2 == 0) { // room
            return CellType.ROOM;
        } else if (abs(x) % 2 == 1 && abs(z) % 2 == 1) { //void
            return CellType.NONE;
        } else {
            return CellType.TUNNEL;
        }
    }

    private static TunnelType getTunnelType(int x, int z) {
        if (abs(x) % 2 == 1 && z % 2 == 0) {
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

        if (isNewCell(newCell, cells)) {
            if (abs(currentRoom.x) > currentCoordLimit || abs(currentRoom.z) > currentCoordLimit) { // resize map
                currentMapSize += 4;
                currentCoordLimit += 2;
                VaultMapOverlayRenderer.updateAnchor();
            }

            cells.add(newCell);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(MovementInputUpdateEvent event) {
        if (!enabled) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!hologramChecked) {
            if (player.level.isLoaded(player.getOnPos())) {
                if (hologramData == null && player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
                    hologramData = getHologramData();
                    hologramChecked = true;
                }
            }
        }

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);

        if (isWithinBounds(playerRoomX, playerRoomZ)) {
            if (debug)
                Minecraft.getInstance().gui.setOverlayMessage(new TextComponent("Current room: " + playerRoomX + ", " + playerRoomZ + " Hologram: " + (hologramData != null ? "Found" : "Not found") + (hologramChecked ? " (Checked)" : "(Not checked)") + " Vault Map Data Size: " + cells.size()), false);
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

        if (playerRoomX == 0 && playerRoomZ == 0) {
            player.sendMessage(new TextComponent("You can't mark the start room"), player.getUUID());
            return;
        }

        if (getCellType(playerRoomX, playerRoomZ) == CellType.ROOM) {
            VaultCell new_cell = new VaultCell(CellType.ROOM, playerRoomX, playerRoomZ);
            if (isNewCell(new_cell, markedRooms)) {
                markedRooms.add(new_cell);
                player.sendMessage(new TextComponent("The room has been marked"), player.getUUID());
            } else {
                markedRooms.removeIf((cell) -> {
                    return cell.type == CellType.ROOM && cell.x == playerRoomX && cell.z == playerRoomZ;
                });
                player.sendMessage(new TextComponent("The room has been unmarked"), player.getUUID());
            }
        } else {
            player.sendMessage(new TextComponent("You can only mark rooms"), player.getUUID());
        }
    }

    public static void toggleRendering() {
        Player player = Minecraft.getInstance().player;
        if (VaultMapOverlayRenderer.enabled) {
            VaultMapOverlayRenderer.enabled = false;
            return;
        }

        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
            player.sendMessage(new TextComponent("You can't use this outside of Vaults"), player.getUUID());
            return;
        }
        VaultMapOverlayRenderer.enabled = true;
    }


    private static CompoundTag getHologramData() {
        HashMap<BlockPos, Direction> hologramBlocks = new HashMap<>();
        hologramBlocks.put(new BlockPos(23, 27, 13), Direction.NORTH);
        hologramBlocks.put(new BlockPos(33, 27, 23), Direction.EAST);
        hologramBlocks.put(new BlockPos(13, 27, 23), Direction.WEST);
        hologramBlocks.put(new BlockPos(23, 27, 33), Direction.SOUTH);

        CompoundTag hologramNbt = null;

        // get the required data from hologram
        for (Map.Entry<BlockPos, Direction> entry : hologramBlocks.entrySet()) {
            BlockPos hologramBlockPos = entry.getKey();
            Direction direction = entry.getValue();

            BlockState hologramBlockState = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).getLevel()).getBlockState(hologramBlockPos);
            if (!Objects.equals(hologramBlockState.getBlock().getRegistryName(), new ResourceLocation("the_vault:hologram"))) {
                continue;
            }

            BlockEntity hologramBlock = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).getLevel()).getBlockEntity(hologramBlockPos);
            CompoundTag hologramData = Objects.requireNonNull(hologramBlock).serializeNBT();

            if (debug)
                Minecraft.getInstance().player.sendMessage(new TextComponent("Hologram block: " + hologramData), UUID.randomUUID());

            // vaultDirection = direction;

            hologramNbt = hologramData;
        }

        if (hologramNbt == null) return null;

        Tag children = hologramNbt.getCompound("tree").get("children");
        ListTag childrenList = (ListTag) children;

        // extract the inscription room locations and add them to the inscription room list
        childrenList.forEach(tag -> {
            CompoundTag compound = (CompoundTag) tag;
            CompoundTag stack = compound.getCompound("stack");
            String id = stack.getString("id");
            int model = stack.getCompound("tag").getCompound("data").getInt("model");
            CompoundTag translation = compound.getCompound("translation");
            byte translationX = translation.getByte("x");
            byte translationY = translation.getByte("y");

            int translationXInt = translationX;
            int translationYInt = translationY;

            inscriptionRooms.add(new VaultCell(CellType.ROOM, translationXInt * 2, translationYInt * -2));
        });

        return hologramNbt;
    }

    /**
     * Gets the block in cell on specific coordinate
     *
     * @param cellX  X coord of cell
     * @param cellZ  Z coord of cell
     * @param blockX X coord of block inside a cell
     * @param blockZ Z coord of block inside a cell
     * @return Block or null if unavailable
     */
    public static Block getCellBlock(int cellX, int cellZ, int blockX, int blockY, int blockZ) {
        Player player = Minecraft.getInstance().player;

        if (player == null) return null;
        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) return null;

        int xCoord = cellX * 49 + blockX;
        int zCoord = cellZ * 49 + blockZ;

        if (!player.level.isLoaded(new BlockPos(xCoord, blockY, zCoord))) return null;

        return player.level.getBlockState(new BlockPos(xCoord, blockY, zCoord)).getBlock();
    }
}
