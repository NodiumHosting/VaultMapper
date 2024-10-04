package com.nodiumhosting.vaultmapper.roomdetection;

import com.ibm.icu.impl.Pair;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.vault.RegistryKeyAdapter;
import iskallia.vault.core.data.key.TemplateKey;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.ClassicVaultLayout;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.world.template.data.DirectTemplateEntry;
import iskallia.vault.core.world.template.data.IndirectTemplateEntry;
import iskallia.vault.core.world.template.data.TemplatePool;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class RoomData {

    public static List<RoomData> omegaRooms;
    public static List<RoomData> challengeRooms;
    public static void initRooms() {
        omegaRooms = new ArrayList<>();
        challengeRooms = new ArrayList<>();
        TemplatePoolKey challengeRef = VaultRegistry.TEMPLATE_POOL.getKey("the_vault:vault/rooms/challenge_rooms");
        TemplatePoolKey omegaRef = VaultRegistry.TEMPLATE_POOL.getKey("the_vault:vault/rooms/omega_rooms");
        if (challengeRef == null) {
            VaultMapper.LOGGER.info("challengeRef is null");
            return;
        }
        if (omegaRef == null) {
            VaultMapper.LOGGER.info("omegaRef is null");
            return;
        }
        if (!challengeRef.supports(Version.latest())) {
            VaultMapper.LOGGER.info("challengeRef doesnt support version");
            return;
        }
        if (!omegaRef.supports(Version.latest())) {
            VaultMapper.LOGGER.info("omegaRef doesnt support version");
            return;
        }
        TemplatePool challenge = challengeRef.get(Version.latest());
        TemplatePool omega = omegaRef.get(Version.latest());
        if (!challengeRef.supports(Version.latest()))
        if (challenge == null) {
            VaultMapper.LOGGER.info("Cant find challenge rooms");
            return;
        }
        challenge.iterate((entry -> {
            if (entry instanceof IndirectTemplateEntry roomBatchRef) {
                if (!roomBatchRef.getReference().supports(Version.latest())) {
                    return true;
                }
                TemplatePool roomBatch = roomBatchRef.getReference().get(Version.latest());
                String simpleName = roomBatchRef.getReference().getName();
                VaultMapper.LOGGER.info("Challenge Room Batch name: " + simpleName);
                roomBatch.iterate((inner) -> {
                    if (inner instanceof DirectTemplateEntry roomFileRef) {
                        if (!roomFileRef.getTemplate().supports(Version.latest())) {
                            return true;
                        }
                        Template roomFile = roomFileRef.getTemplate().get(Version.latest());
                        String name = roomFileRef.getTemplate().getName();
                        VaultMapper.LOGGER.info("Room File name: " + name);
                        challengeRooms.add(new RoomData("challenge", name,simpleName,roomFile));
                    }
                    return true;
                });
            }

            return true;
        }));

        omega.iterate((entry -> {
            if (entry instanceof IndirectTemplateEntry roomBatchRef) {
                if (!roomBatchRef.getReference().supports(Version.latest())) {
                    return true;
                }
                TemplatePool roomBatch = roomBatchRef.getReference().get(Version.latest());
                String simpleName = roomBatchRef.getReference().getName();
                VaultMapper.LOGGER.info("Omega Room Batch name: " + simpleName);
                roomBatch.iterate((inner) -> {
                    if (inner instanceof DirectTemplateEntry roomFileRef) {
                        if (!roomFileRef.getTemplate().supports(Version.latest())) {
                            return true;
                        }
                        Template roomFile = roomFileRef.getTemplate().get(Version.latest());
                        String name = roomFileRef.getTemplate().getName();
                        VaultMapper.LOGGER.info("Room File name: " + name);
                        omegaRooms.add(new RoomData("omega", name,simpleName,roomFile));
                    }
                    return true;
                });
            }

            return true;
        }));
    }




    public static boolean compareBlock(Block block1, Block block2) {
        if (block1 == Blocks.AIR || block1 instanceof VaultChestBlock || block1 instanceof CoinPileBlock) {
            block1 = null;
        }
        if (block2 == Blocks.AIR || block2 instanceof VaultChestBlock || block2 instanceof CoinPileBlock) {
            block2 = null;
        }
        if (block1 == block2) {
            return true;
        }
        if (block2 == ModBlocks.PLACEHOLDER) {
            return  block1 == Blocks.STONE || block1 == Blocks.COBBLESTONE || block1 == Blocks.ANDESITE || block1 instanceof VaultOreBlock;
        }
        if (block1 == ModBlocks.PLACEHOLDER) {
            return  block2 == Blocks.STONE || block2 == Blocks.COBBLESTONE || block2 == Blocks.ANDESITE || block2 instanceof VaultOreBlock;
        }
        if (block1 == null || block2 == null) {
            VaultMapper.LOGGER.info("Failed test on null");
            return false;
        }
        VaultMapper.LOGGER.info("Failed on " +  block1.getDescriptionId() +" "+block2.getDescriptionId() );
        return false;
    }
    public static boolean compareColumn(Map<Integer,Block> map1, Map<Integer,Block> map2) {
        Set<Integer> combinedKeySet = new HashSet<>(map1.keySet());
        combinedKeySet.addAll(map2.keySet());
        for (Integer key : combinedKeySet) {
            Block block1 = null;
            if (map1.containsKey(key)) {
                block1 = map1.get(key);
            }
            Block block2 = null;
            if (map2.containsKey(key)) {
                block2 = map2.get(key);
            }
            if (!compareBlock(block1,block2)) {
                return false;
            }
        }
        return true;
    }
    public static RoomData captureRoom(int cellX, int cellZ) {
        RoomData currentRoom = new RoomData();
        for (int i = 0; i < 47; i++) {
            Block block = VaultMap.getCellBlock(cellX,cellZ,0,i,0);
            currentRoom.northwestColumn.put(i,block);
            VaultMapper.LOGGER.info("1");
            if (block!=null) {
                VaultMapper.LOGGER.info(block.getDescriptionId() + " " + i);
            }
        }
        for (int i = 0; i < 47; i++) {
            Block block = VaultMap.getCellBlock(cellX,cellZ,46,i,0);
            currentRoom.northeastColumn.put(i,block);
            VaultMapper.LOGGER.info("2");
            if (block!=null) {
                VaultMapper.LOGGER.info(block.getDescriptionId() + " " + i);
            }
        }
        for (int i = 0; i < 47; i++) {
            Block block = VaultMap.getCellBlock(cellX,cellZ,0,i,46);
            currentRoom.southwestColumn.put(i,block);
            VaultMapper.LOGGER.info("3");
            if (block!=null) {
                VaultMapper.LOGGER.info(block.getDescriptionId() + " " + i);
            }
        }
        for (int i = 0; i < 47; i++) {
            Block block = VaultMap.getCellBlock(cellX,cellZ,46,i,46);
            currentRoom.southeastColumn.put(i,block);
            VaultMapper.LOGGER.info("4");
            if (block!=null) {
                VaultMapper.LOGGER.info(block.getDescriptionId() + " " + i);
            }

        }
        currentRoom.columnList.add(currentRoom.northwestColumn);
        currentRoom.columnList.add(currentRoom.northeastColumn);
        currentRoom.columnList.add(currentRoom.southwestColumn);
        currentRoom.columnList.add(currentRoom.southeastColumn);
        return currentRoom;
    }

    public String type;
    public String name;
    public String simpleName;
    public Template room;
    public Map<Integer, Block> northeastColumn = new HashMap<>();
    public Map<Integer,Block> northwestColumn = new HashMap<>();
    public Map<Integer,Block> southeastColumn = new HashMap<>();
    public Map<Integer,Block> southwestColumn = new HashMap<>();
    public List<Map<Integer,Block>> columnList = new ArrayList<>();

    public RoomData() {
        this.type = "current";
        this.simpleName = "current";
        this.name = "current";
        this.room = null;
    }

    public RoomData(String type, String simpleName, String name, Template room) {
        this.type = type;
        this.simpleName = simpleName;
        this.name = name;
        this.room = room;
        Iterator<PartialTile> tiles = room.getTiles(Template.ALL_TILES);
        while (tiles.hasNext()) {
            PartialTile tile = tiles.next();
            BlockPos pos = tile.getPos();
            Optional<Block> optBlock = tile.getState().getBlock().asWhole();
            if (optBlock.isEmpty()) {
                continue;
            }
            Block block = optBlock.get();
            if (pos.getX() == 0 && pos.getZ() == 0) {
                northwestColumn.put(pos.getY(),block);
            }
            if (pos.getX() == 46 && pos.getZ() == 0) {
                northeastColumn.put(pos.getY(),block);
            }
            if (pos.getX() == 0 && pos.getZ() == 46) {
                southwestColumn.put(pos.getY(),block);
            }
            if (pos.getX() == 46 && pos.getZ() == 46) {
                southeastColumn.put(pos.getY(),block);
            }
        }
        columnList.add(northeastColumn);
        columnList.add(northwestColumn);
        columnList.add(southeastColumn);
        columnList.add(southwestColumn);
    }
    public boolean compareRoom(RoomData roomData) {
        for (Map<Integer,Block> column1 : columnList) {
            boolean flag = false;
            for (Map<Integer,Block> column2 : roomData.columnList) {
                if (compareColumn(column1,column2)) {
                    flag = true;
                }
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    public Tuple<String,String> findRoom() {
        for (RoomData omegaRoom : omegaRooms) {
            if (this.compareRoom(omegaRoom)) {
                return new Tuple<String,String>("omega",omegaRoom.simpleName);
            }
        }
        for (RoomData challengeRoom : challengeRooms) {
            if (this.compareRoom(challengeRoom)) {
                return new Tuple<String,String>("challenge",challengeRoom.simpleName);
            }
        }
        return new Tuple<>("common", "none");
    }



}
