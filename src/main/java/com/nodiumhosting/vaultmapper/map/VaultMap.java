package com.nodiumhosting.vaultmapper.map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMap {
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

        CompoundTag hologramNbt = null;

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

            hologramNbt = hologramData;
        }

        if (hologramNbt == null) return null;

        Tag children = hologramNbt.getCompound("tree").get("children");
        ListTag childrenList = (ListTag) children;

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

            inscriptionRooms.add(new int[]{translationXInt * 2, translationYInt * -2});
        });

        return hologramNbt;
    }

    private static int[] currentRoom = null;
    private static List<int[]> visitedRooms = new ArrayList<>();
    private static List<int[]> importantRooms = new ArrayList<>();
    private static List<int[]> inscriptionRooms = new ArrayList<>();

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
        inscriptionRooms.clear();
        currentRoom = null;
        mapData = new int[49][49][3];
    }

    private static boolean isVoidRoom(int x, int z) {
        return Math.abs(x) % 2 == 1 && Math.abs(z) % 2 == 1;
    }

    public static int[][][] mapData = new int[49][49][3]; // x, z matrix of [type (0 = void, 1 = tunnelX, 2 = tunnelZ, 3 = room, 4 = start, 5 = current, 6 = inscription), size (0 = 0x0, = 3x3, 2 = 5x5, 3 = 7x7), visited (0 = false, 1 = true, 2 = true, important, 3 = false, important)]

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(MovementInputUpdateEvent event) {
        if (!enabled) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        HashMap<Vec3, Direction> spawnLocations = new HashMap<>();
        spawnLocations.put(new Vec3(23.5f, 29.0f, 14.5f), Direction.NORTH);
        spawnLocations.put(new Vec3(32.5f, 29.0f, 23.5f), Direction.EAST);
        spawnLocations.put(new Vec3(14.5f, 29.0f, 23.5f), Direction.WEST);
        spawnLocations.put(new Vec3(23.5f, 29.0f, 32.5f), Direction.SOUTH);

        if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) return;
        if (spawnLocations.keySet().stream().anyMatch(spawnLocation -> spawnLocation.x == player.getX() && spawnLocation.z == player.getZ())) return;
        if (!VaultMapOverlayRenderer.enabled) VaultMapOverlayRenderer.enabled = true;

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

        boolean mapChanged = addVisitedRoom(playerRoomX, playerRoomZ);
        if (!mapChanged) return;

        if (hologramData == null && !hologramChecked) {
            hologramData = getHologramData();
        }

        //return if outside map bounds
        if (playerRoomX < -24 || playerRoomX > 24 || playerRoomZ < -24 || playerRoomZ > 24) {
            return;
        }

        for (int x = -24; x <= 24; x++) {
            for (int z = -24; z <= 24; z++) {
                final int[] r = new int[]{x, z};
                boolean isImportant = importantRooms.stream().anyMatch(room -> Arrays.equals(room, r));
                boolean isInscriptionRoom = inscriptionRooms.stream().anyMatch(room -> Arrays.equals(room, r));

                if (isVoidRoom(x, z)) {
                    mapData[x + 24][z + 24] = new int[]{0, 0, 0};
                    continue;
                }

                if (Arrays.equals(new int[]{x, z}, currentRoom)) {
                    if (isTunnel) {
                        mapData[x + 24][z + 24] = new int[]{5, 1, isImportant ? 2 : 1};
                    } else if (isStartRoom) {
                        mapData[x + 24][z + 24] = new int[]{5, 3, isImportant ? 2 : 1};
                    } else if (isRoom) {
                        mapData[x + 24][z + 24] = new int[]{5, 2, isImportant ? 2 : 1};
                    }
                    continue;
                }

                if (x == 0 && z == 0) {
                    mapData[x + 24][z + 24] = new int[]{4, 3, isImportant ? 2 : 1};
                    continue;
                }

                boolean mapTunnelX = Math.abs(x) % 2 == 1 && z % 2 == 0;
                boolean mapTunnelZ = x % 2 == 0 && Math.abs(z) % 2 == 1;

                if (visitedRooms.stream().anyMatch(room -> Arrays.equals(room, r))) {
                    if (mapTunnelX) {
                        mapData[x + 24][z + 24] = new int[]{1, 1, isImportant ? 2 : 1};
                    } else if (mapTunnelZ) {
                        mapData[x + 24][z + 24] = new int[]{2, 1, isImportant ? 2 : 1};
                    } else {
                        if (isInscriptionRoom) {
                            mapData[x + 24][z + 24] = new int[]{6, 2, isImportant ? 2 : 1};
                        } else {
                            mapData[x + 24][z + 24] = new int[]{3, 2, isImportant ? 2 : 1};
                        }
                    }
                } else {
                    if (mapTunnelX) {
                        mapData[x + 24][z + 24] = new int[]{1, 1, isImportant ? 3 : 0};
                    } else if (mapTunnelZ) {
                        mapData[x + 24][z + 24] = new int[]{2, 1, isImportant ? 3 : 0};
                    } else {
                        if (isInscriptionRoom) {
                            mapData[x + 24][z + 24] = new int[]{6, 2, isImportant ? 2 : 1};
                        } else {
                            mapData[x + 24][z + 24] = new int[]{3, 2, isImportant ? 3 : 0};
                        }
                    }
                }
            }
        }
    }
}
