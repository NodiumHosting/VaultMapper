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

import static com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer.*;

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
    private static List<int[]> inscriptionRooms = new ArrayList<>();

    private static boolean addVisitedRoom(int x, int z) {
        if (Arrays.equals(currentRoom, new int[]{x, z})) return false;
        currentRoom = new int[]{x, z};
        if(isVoidRoom(x, z) || visitedRooms.stream().anyMatch(room -> Arrays.equals(room, new int[]{x, z}))) return true;
        visitedRooms.add(new int[]{x, z});
        return true;
    }

    public static void resetMap() {
        vaultDirection = null;
        hologramData = null;
        hologramChecked = false;
        visitedRooms.clear();
        inscriptionRooms.clear();
        currentRoom = null;
        mapData = new ArrayList<>();
    }

    private static boolean isVoidRoom(int x, int z) {
        return Math.abs(x) % 2 == 1 && Math.abs(z) % 2 == 1;
    }

    public static List<VaultMapRoom> mapData = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(MovementInputUpdateEvent event) {
        doMapUpdate();
    }

    public static void doMapUpdate() {
        if (!enabled) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        HashMap<Vec3, Direction> spawnLocations = new HashMap<>();
        spawnLocations.put(new Vec3(23.5f, 29.0f, 14.5f), Direction.NORTH);
        spawnLocations.put(new Vec3(32.5f, 29.0f, 23.5f), Direction.EAST);
        spawnLocations.put(new Vec3(14.5f, 29.0f, 23.5f), Direction.WEST);
        spawnLocations.put(new Vec3(23.5f, 29.0f, 32.5f), Direction.SOUTH);

        //if (!player.getLevel().dimension().location().getNamespace().equals("the_vault")) return;
        if (spawnLocations.keySet().stream().anyMatch(spawnLocation -> spawnLocation.x == player.getX() && spawnLocation.z == player.getZ())) return;
        //if (!VaultMapOverlayRenderer.enabled) VaultMapOverlayRenderer.enabled = true;

        int playerRoomX = (int) Math.floor(player.getX() / 47);
        int playerRoomZ = (int) Math.floor(player.getZ() / 47);

        Minecraft.getInstance().gui.setOverlayMessage(new TextComponent("Current room: " + playerRoomX + ", " + playerRoomZ + " Hologram: " + (hologramData != null ? "Found" : "Not found") + (hologramChecked ? " (Checked)" : "(Not checked)") + " Vault Map Data Size: " + mapData.size()), false);

        boolean mapChanged = addVisitedRoom(playerRoomX, playerRoomZ);
        if (!mapChanged) return;

        if (hologramData == null && !hologramChecked) {
            hologramData = getHologramData();
        }

        //return if outside map bounds
        if (playerRoomX < -24 || playerRoomX > 24 || playerRoomZ < -24 || playerRoomZ > 24) {
            return;
        }

        mapData.clear();

        for (int x = -24; x <= 24; x++) {
            for (int z = -24; z <= 24; z++) {
                int mapX = mapStartX + (x + 24) * mapRoomWidth;
                int mapZ = mapStartY + (z + 24) * mapRoomWidth;
                VaultMapRoom vmr = null;

                if (isVoidRoom(x, z)) {
                    continue;
                }

                final int[] r = new int[]{x, z};
                boolean isInscriptionRoom = inscriptionRooms.stream().anyMatch(room -> Arrays.equals(room, r));
                boolean isStartRoom = x == 0 && z == 0;
                boolean isRoom = Math.abs(x) % 2 == 0 && Math.abs(z) % 2 == 0;
                boolean isTunnelX = Math.abs(x) % 2 == 1 && z % 2 == 0;
                boolean isTunnelZ = x % 2 == 0 && Math.abs(z) % 2 == 1;

                if (Arrays.equals(new int[]{x, z}, currentRoom)) {
                    if (isTunnelX) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.CURRENT, mapX - 2, mapZ - 1, mapX + 2, mapZ + 1);
                    } else if (isTunnelZ) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.CURRENT, mapX - 1, mapZ - 2, mapX + 1, mapZ + 2);
                    } else if (isStartRoom) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.CURRENT, mapX - 3, mapZ - 3, mapX + 3, mapZ + 3);
                    } else if (isRoom) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.CURRENT, mapX - 2, mapZ - 2, mapX + 2, mapZ + 2);
                    }

                    mapData.add(vmr);
                    continue;
                }

                if (isStartRoom) {
                    vmr = new VaultMapRoom(x, z, VaultMapRoomColor.START, mapX - 3, mapZ - 3, mapX + 3, mapZ + 3);

                    mapData.add(vmr);
                    continue;
                }

                if (isInscriptionRoom) {
                    vmr = new VaultMapRoom(x, z, VaultMapRoomColor.INSCRIPTION, mapX - 2, mapZ - 2, mapX + 2, mapZ + 2);

                    mapData.add(vmr);
                    continue;
                }

                if (visitedRooms.stream().anyMatch(room -> Arrays.equals(room, r))) {
                    if (isTunnelX) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.TUNNEL, mapX - 2, mapZ - 1, mapX + 2, mapZ + 1);
                    } else if (isTunnelZ) {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.TUNNEL, mapX - 1, mapZ - 2, mapX + 1, mapZ + 2);
                    } else {
                        vmr = new VaultMapRoom(x, z, VaultMapRoomColor.ROOM, mapX - 2, mapZ - 2, mapX + 2, mapZ + 2);
                    }

                    mapData.add(vmr);
                    continue;
                }
            }
        }
    }
}
