package com.nodiumhosting.vaultmapper.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlay {
    public static boolean enabled = false;

    public static Direction vaultDirection = null;
    private static CompoundTag hologramData = null;
    private static boolean hologramChecked = false;

    private static CompoundTag getHologramData() {
        HashMap<BlockPos, Direction> hologramBlocks = new HashMap<>();
        hologramBlocks.put(new BlockPos(23, 27, 13), Direction.NORTH);
        hologramBlocks.put(new BlockPos(33, 27, 23), Direction.EAST);
        hologramBlocks.put(new BlockPos(13, 27, 23), Direction.WEST);
        hologramBlocks.put(new BlockPos(23, 27, 33), Direction.SOUTH);

        hologramChecked = true;

        for (Map.Entry<BlockPos, Direction> entry : hologramBlocks.entrySet()) {
            BlockPos hologramBlockPos = entry.getKey();
            Direction direction = entry.getValue();

            BlockState hologramBlockState = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).getLevel()).getBlockState(hologramBlockPos);
            if (!Objects.equals(hologramBlockState.getBlock().getRegistryName(), new ResourceLocation("the_vault:hologram"))) {
                continue;
            }

            BlockEntity hologramBlock = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().player).getLevel()).getBlockEntity(hologramBlockPos);
            CompoundTag hologramData = Objects.requireNonNull(hologramBlock).serializeNBT();

            Minecraft.getInstance().player.sendMessage(new TextComponent("Hologram block: " + hologramData), UUID.randomUUID());

            vaultDirection = direction;

            return hologramData;
        }

        return null;
    }

    private static int[] currentRoom = new int[]{0, 0};
    private static List<int[]> visitedRooms = new ArrayList<>();
    private static List<int[]> importantRooms = new ArrayList<>();

    private static boolean addVisitedRoom(int x, int z) {
        if (Arrays.equals(currentRoom, new int[]{x, z})) return false;
        currentRoom = new int[]{x, z};
        if(isVoidRoom(x, z) || visitedRooms.stream().anyMatch(room -> Arrays.equals(room, new int[]{x, z}))) return true;
        visitedRooms.add(new int[]{x, z});
        return true;
    }

    public static void toggleImportantRoom(int x, int z) {
        if (importantRooms.stream().anyMatch(room -> Arrays.equals(room, new int[]{x, z}))) {
            importantRooms = importantRooms.stream().filter(room -> !Arrays.equals(room, new int[]{x, z})).collect(Collectors.toList());
        } else {
            importantRooms.add(new int[]{x, z});
        }
    }

    public static void resetMap() {
        vaultDirection = null;
        hologramData = null;
        hologramChecked = false;
        visitedRooms.clear();
        importantRooms.clear();
        currentRoom = new int[]{0, 0};
        mapData = new int[29][29][3];
    }

    private static boolean isVoidRoom(int x, int z) {
        return Math.abs(x) % 2 == 1 && Math.abs(z) % 2 == 1;
    }

    private static int[][][] mapData = new int[29][29][3]; // x, z matrix of [type (0 = void, 1 = tunnelX, 2 = tunnelZ, 3 = room, 4 = start, 5 = current), size (0 = 0x0, = 3x3, 2 = 5x5, 3 = 7x7), visited (0 = false, 1 = true, 2 = true, important, 3 = false, important)]

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        Minecraft.getInstance().gui.setTitle(new TextComponent("direction: " + vaultDirection + " enabled: " + enabled + " hologram: " + (hologramData != null ? "Found" : "Not found") + (hologramChecked ? " (Checked)" : "(Not checked)") + " visited: " + visitedRooms.size() + " important: " + importantRooms.size()));
        if (!enabled) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);
        //start room will give 0,0

        boolean isStartRoom = playerRoomX == 0 && playerRoomZ == 0;
        boolean isVoid = Math.abs(playerRoomX) % 2 == 1 && Math.abs(playerRoomZ) % 2 == 1;
        boolean isTunnelX = Math.abs(playerRoomX) % 2 == 1 && playerRoomZ % 2 == 0;
        boolean isTunnelZ = playerRoomX % 2 == 0 && Math.abs(playerRoomZ) % 2 == 1;
        boolean isTunnel = isTunnelX || isTunnelZ;
        boolean isRoom = !isVoid && !isTunnel;

        List<String> roomTypes = new ArrayList<>();
        if (isStartRoom) roomTypes.add("[Start]");
        if (isVoid) roomTypes.add("[Void]");
        if (isTunnelX) roomTypes.add("[Tunnel (X)]");
        if (isTunnelZ) roomTypes.add("[Tunnel (Z)]");
        if (isRoom) roomTypes.add("[Room]");

        String roomTypeString = " - " + String.join(", ", roomTypes);

        Minecraft.getInstance().gui.setOverlayMessage(new TextComponent("Current room: " + playerRoomX + ", " + playerRoomZ + roomTypeString + " Hologram: " + (hologramData != null ? "Found" : "Not found") + (hologramChecked ? " (Checked)" : "(Not checked)")), false);

        if (hologramData == null && !hologramChecked) {
            hologramData = getHologramData();
        }

        //return if outside map bounds
        if (playerRoomX < -14 || playerRoomX > 14 || playerRoomZ < -14 || playerRoomZ > 14) {
            return;
        }

        boolean mapChanged = addVisitedRoom(playerRoomX, playerRoomZ);

        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();

        int mapWidth = (int) (w * 0.2f);
        int mapHeight = (int) (w * 0.2f);
        int mapStartX = w - mapWidth;
        int mapStartY = h - mapHeight;

        int mapRoomWidth = mapWidth / 29;

        if (mapChanged) {
            for (int x = -14; x <= 14; x++) {
                for (int z = -14; z <= 14; z++) {
                    final int[] r = new int[]{x, z};
                    boolean isImportant = importantRooms.stream().anyMatch(room -> Arrays.equals(room, r));

                    if (isVoidRoom(x, z)) {
                        mapData[x + 14][z + 14] = new int[]{0, 0, 0};
                        continue;
                    }

                    if (Arrays.equals(new int[]{x, z}, currentRoom)) {
                        mapData[x + 14][z + 14] = new int[]{5, 3, isImportant ? 2 : 1};
                        continue;
                    }

                    if (x == 0 && z == 0) {
                        mapData[x + 14][z + 14] = new int[]{4, 3, isImportant ? 2 : 1};
                        continue;
                    }

                    boolean mapTunnelX = Math.abs(x) % 2 == 1 && z % 2 == 0;
                    boolean mapTunnelZ = x % 2 == 0 && Math.abs(z) % 2 == 1;

                    if (visitedRooms.stream().anyMatch(room -> Arrays.equals(room, r))) {
                        if (mapTunnelX) {
                            mapData[x + 14][z + 14] = new int[]{1, 1, isImportant ? 2 : 1};
                        } else if (mapTunnelZ) {
                            mapData[x + 14][z + 14] = new int[]{2, 1, isImportant ? 2 : 1};
                        } else {
                            mapData[x + 14][z + 14] = new int[]{3, 2, isImportant ? 2 : 1};
                        }
                    } else {
                        if (mapTunnelX) {
                            mapData[x + 14][z + 14] = new int[]{1, 1, isImportant ? 3 : 0};
                        } else if (mapTunnelZ) {
                            mapData[x + 14][z + 14] = new int[]{2, 1, isImportant ? 3 : 0};
                        } else {
                            mapData[x + 14][z + 14] = new int[]{3, 2, isImportant ? 3 : 0};
                        }
                    }
                }
            }
        }

        HashMap<Integer, Integer> roomColors = new HashMap<>();
        roomColors.put(0, 0xFF000000); // void
        roomColors.put(1, 0xFF0000FF); // tunnelX
        roomColors.put(2, 0xFF0000FF); // tunnelZ
        roomColors.put(3, 0xFF0000FF); // room
        roomColors.put(4, 0xFFFF0000); // start
        roomColors.put(5, 0xFF00FF00); // current

        int importantRoomColor = 0xFFFF00FF;

        //draw map grid
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                int mapX = mapStartX + (x + 14) * mapRoomWidth;
                int mapZ = mapStartY + (z + 14) * mapRoomWidth;

                int[] roomData = mapData[x + 14][z + 14];

                int roomType = roomData[0];
                int roomSize = roomData[1];
                int roomVisited = roomData[2];

                if (roomVisited == 1 || roomVisited == 2) GuiComponent.fill(event.getMatrixStack(), mapX - roomSize, mapZ - roomSize, mapX + roomSize, mapZ + roomSize, roomColors.get(roomType));

                if (roomVisited == 2 || roomVisited == 3) GuiComponent.fill(event.getMatrixStack(), mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, importantRoomColor);
            }
        }

//        //draw map grid
//        for (int x = -14; x <= 14; x++) {
//            for (int z = -14; z <= 14; z++) {
//                if (isVoidRoom(x, z)) continue;
//
//                boolean mapTunnel = Math.abs(x) % 2 == 1 && z % 2 == 0 || x % 2 == 0 && Math.abs(z) % 2 == 1;
//
//                int mapX = mapOutlineX + (x + 14) * mapRoomWidth;
//                int mapZ = mapOutlineY + (z + 14) * mapRoomWidth;
//
//                if (Arrays.equals(new int[]{x, z}, currentRoom)) {
//                    GuiComponent.fill(event.getMatrixStack(), mapX - 3, mapZ - 3, mapX + 3, mapZ + 3, 0xFF00FF00);
//                    continue;
//                }
//
//                if (x == 0 && z == 0) {
//                    GuiComponent.fill(event.getMatrixStack(), mapX - 3, mapZ - 3, mapX + 3, mapZ + 3, 0xFFFF0000);
//                    continue;
//                }
//
//                final int[] r = new int[]{x, z};
//
//                if (visitedRooms.stream().anyMatch(room -> Arrays.equals(room, r))) {
//                    if (mapTunnel) {
//                        GuiComponent.fill(event.getMatrixStack(), mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, 0xFF0000FF);
//                    } else {
//                        GuiComponent.fill(event.getMatrixStack(), mapX - 2, mapZ - 2, mapX + 2, mapZ + 2, 0xFF0000FF);
//                    }
//                } else {
//                    if (mapTunnel) {
//                        GuiComponent.fill(event.getMatrixStack(), mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, 0xFF000000);
//                    } else {
//                        GuiComponent.fill(event.getMatrixStack(), mapX - 2, mapZ - 2, mapX + 2, mapZ + 2, 0xFF000000);
//                    }
//                }
//            }
//        }
    }
}