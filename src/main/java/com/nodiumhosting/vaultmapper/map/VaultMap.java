package com.nodiumhosting.vaultmapper.map;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.roomdetection.RoomData;
import com.nodiumhosting.vaultmapper.snapshots.MapCache;
import com.nodiumhosting.vaultmapper.util.ResearchUtil;
import com.nodiumhosting.vaultmapper.webmap.SocketServer;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
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

    public static List<VaultCell> cells = new ArrayList<>();
    static VaultCell startRoom = new VaultCell(0, 0, CellType.ROOM, RoomType.START);
    static VaultCell currentRoom; // might not be needed
    static int defaultMapSize = 21; // map size in cells
    static int defaultCoordLimit = 10; // limit for coords, so a cell at -6 on x or z would be considered out of bounds and trigger action
    static int currentMapSize = defaultMapSize;
    static int currentCoordLimit = defaultCoordLimit; // initialize to default values, will change and reset
    static CompoundTag hologramData;
    static boolean hologramChecked;

    public static List<VaultCell> getCells() {
        return cells;
    }

    public static void resetMap() {
        cells = new ArrayList<>();
        startRoom = new VaultCell(0, 0, CellType.ROOM, RoomType.START);
        currentRoom = null;
        hologramChecked = false;
        hologramData = null;

        currentMapSize = defaultMapSize;
        currentCoordLimit = defaultCoordLimit;

        VaultMapper.wsServer.sendReset();
    }

    private static boolean isCurrentRoom(int x, int z) {
        if (currentRoom == null) return false;
        return currentRoom.x == x && currentRoom.z == z;
    }

    private static CellType getCellType(int x, int z) {
        if (abs(x) % 2 == 0 && abs(z) % 2 == 0) { // room
            return CellType.ROOM;
        } else if (abs(x) % 2 == 1 && abs(z) % 2 == 1) { //void
            return CellType.NONE;
        } else { // tunnel
            if (abs(x) % 2 == 1 && z % 2 == 0) { // x tunnel
                return CellType.TUNNEL_X;
            } else { // z tunnel
                return CellType.TUNNEL_Z;
            }
        }
    }

    public static String getCellColor(VaultCell cell) {
        if (cell.roomType == RoomType.START) {
            return ClientConfig.START_ROOM_COLOR.get();
        }
        if (cell.marked) {
            return ClientConfig.MARKED_ROOM_COLOR.get();
        }
        if (cell.inscripted) {
            return ClientConfig.INSCRIPTION_ROOM_COLOR.get();
        }
        if (cell.roomType == RoomType.OMEGA) {
            return ClientConfig.OMEGA_ROOM_COLOR.get();
        }
        if (cell.roomType == RoomType.CHALLENGE) {
            return ClientConfig.CHALLENGE_ROOM_COLOR.get();
        }
        return ClientConfig.ROOM_COLOR.get();
    }

    private static boolean isNewCell(VaultCell new_cell, List<VaultCell> cell_list) {
        AtomicBoolean isNew = new AtomicBoolean(true);
        cell_list.forEach((cell) -> {
            if (cell.x == new_cell.x && cell.z == new_cell.z) {
                isNew.set(false);

                cell.setExplored(true);
            }
        });
        return isNew.get();
    }

    private static VaultCell getCell(int x, int z) {
        return cells.stream().filter((cell) -> cell.x == x && cell.z == z).findFirst().orElse(null);
    }

    private static void addOrReplaceCell(VaultCell cell) {
        cells.removeIf((c) -> c.x == cell.x && c.z == cell.z);
        cells.add(cell);
    }

    /**
     * Updates the map data and sends it to connected web clients (like OBS)
     */
    private static void updateMap() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);

        int playerRelativeX = (int) Math.abs(Math.floor(player.getX() % 47));
        int playerRelativeZ = (int) Math.abs(Math.floor(player.getZ() % 47));

        CellType cellType = getCellType(playerRoomX, playerRoomZ);

        // only update tunnel if player is actually in a tunnel to prevent dungeons and doors from being detected as tunnels
        int playerY = (int) player.getY();
        if ((playerY < 27 || playerY > 37) && (cellType == CellType.TUNNEL_X || cellType == CellType.TUNNEL_Z)) return;
        if (cellType == CellType.TUNNEL_X && (playerRelativeZ < 18 || playerRelativeZ > 28)) return;
        if (cellType == CellType.TUNNEL_Z && (playerRelativeX < 18 || playerRelativeX > 28)) return;

        VaultCell newCell;
        newCell = getCell(playerRoomX, playerRoomZ);
        if (newCell == null) newCell = new VaultCell(playerRoomX, playerRoomZ, cellType, RoomType.BASIC); // update current roomv
        currentRoom = newCell;
        newCell.setExplored(true);

        if (playerRoomX == 0 && playerRoomZ == 0) {
            newCell.roomType = RoomType.START;
        }

        if (isNewCell(newCell, cells)) {
            if (abs(currentRoom.x) > currentCoordLimit || abs(currentRoom.z) > currentCoordLimit) { // resize map
                currentMapSize += 4;
                currentCoordLimit += 2;
                VaultMapOverlayRenderer.updateAnchor();
            }

            if (cellType != CellType.NONE) {
                if (!(playerRoomX == 0 && playerRoomZ == 0)) { //dont detect start room
                    if (cellType == CellType.ROOM) {
                        Tuple<RoomType, RoomName> detectedRoom = RoomData.captureRoom(playerRoomX, playerRoomZ).findRoom();
                        RoomType roomType = detectedRoom.getA();
                        RoomName roomName = detectedRoom.getB();
                        newCell.roomName = roomName;
                        newCell.roomType = roomType;
                    }
                }
            }
        }

        addOrReplaceCell(newCell);
        MapCache.updateCache();
        sendCell(newCell);
    }

//    public static void sendMap() {
//        VaultMap.cells.forEach((cell) -> {
//            if (!(cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get())) {
//                VaultMapper.wsServer.sendData(cell);
//            }
//        });
//    }

    public static void sendCell(VaultCell cell) {
        VaultMapper.wsServer.sendCell(cell);
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

        float yaw = player.getYHeadRot();
        String username = player.getName().getString();

        if (currentRoom != null) VaultMapper.wsServer.sendArrow(currentRoom.x, currentRoom.z, yaw, username, ClientConfig.POINTER_COLOR.get());

        if (debug) {
            Minecraft.getInstance().gui.setOverlayMessage(new TextComponent("Current room: " + playerRoomX + ", " + playerRoomZ + " Hologram: " + (hologramData != null ? "Found" : "Not found") + (hologramChecked ? " (Checked)" : "(Not checked)") + " Vault Map Data Size: " + cells.size() + " (" + cells.stream().filter(cell -> cell.cellType == CellType.ROOM && cell.explored).count() + " Explored Rooms)"), false);
        }
        if (!isCurrentRoom(playerRoomX, playerRoomZ)) { // if were in a different room
            updateMap();
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
            boolean marked = cells.stream().filter((cell) -> cell.x == playerRoomX && cell.z == playerRoomZ).findFirst().orElseThrow().switchMarked();
            if (marked) {
                player.sendMessage(new TextComponent("Room marked"), player.getUUID());
            } else {
                player.sendMessage(new TextComponent("Room unmarked"), player.getUUID());
            }
        } else {
            player.sendMessage(new TextComponent("You can only mark rooms"), player.getUUID());
        }

        sendCell(cells.stream().filter((cell) -> cell.x == playerRoomX && cell.z == playerRoomZ).findFirst().orElseThrow());
    }

    public static void toggleRendering() {
        Player player = Minecraft.getInstance().player;

        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
            player.sendMessage(new TextComponent("You can't use this outside of Vaults"), player.getUUID());
            return;
        }

        if (ClientConfig.MAP_ENABLED.get()) {
            ClientConfig.MAP_ENABLED.set(false);
            player.sendMessage(new TextComponent("Vault Map rendering disabled"), player.getUUID());
        } else {
            if (!ResearchUtil.hasResearch("Vault Compass") && !VaultMapOverlayRenderer.ignoreResearchRequirement) {
                player.sendMessage(new TextComponent("Cannot enable. The Research \"Vault Compass\" is not unlocked."), player.getUUID());
                return;
            }
            ClientConfig.MAP_ENABLED.set(true);
            player.sendMessage(new TextComponent("Vault Map rendering enabled"), player.getUUID());
        }

        ClientConfig.SPEC.save();
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

//            if (debug) {
//                Minecraft.getInstance().player.sendMessage(new TextComponent("Hologram block: " + hologramData), UUID.randomUUID());
//            }

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
            Tuple<RoomType, RoomName> room = roomFromModel(model);
            CompoundTag translation = compound.getCompound("translation");
            byte translationX = translation.getByte("x");
            byte translationY = translation.getByte("y");

            int translationXInt = translationX;
            int translationYInt = translationY;

            VaultCell newCell = new VaultCell(translationXInt * 2, translationYInt * -2, CellType.ROOM, room.getA());
            newCell.roomName = room.getB();
            // TODO change this later when we do detection of room types
            newCell.inscripted = true;
            cells.add(newCell);

            sendCell(newCell);
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

        int xCoord = cellX * 47 + blockX;
        int zCoord = cellZ * 47 + blockZ;
        //VaultMapper.LOGGER.info("X " + xCoord + " Z " + zCoord);

        if (!player.level.isLoaded(new BlockPos(xCoord, blockY, zCoord))) return null;

        return player.level.getBlockState(new BlockPos(xCoord, blockY, zCoord)).getBlock();
    }
    public static Tuple<RoomType, RoomName> roomFromModel(int model) {
        ResourceLocation room = null;
        for (Map.Entry<ResourceLocation,Integer> entry : ModConfigs.INSCRIPTION.poolToModel.entrySet()) {
            if (entry.getValue() == model) {
                room = entry.getKey();
                break;
            }
        }
        if (room == null) {
            return new Tuple<>(RoomType.BASIC,RoomName.UNKNOWN);
        }
        RoomType type = RoomType.BASIC;
        if (room.getPath().contains("omega")) {
            type = RoomType.OMEGA;
        } else if (room.getPath().contains("challenge")) {
            type = RoomType.OMEGA;
        }
        RoomName name = RoomName.fromName(VaultRegistry.TEMPLATE_POOL.getKey(room).getName());
        return new Tuple<RoomType,RoomName>(type,name);
    }
}
