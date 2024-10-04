package com.nodiumhosting.vaultmapper.roomdetection;

import com.nodiumhosting.vaultmapper.VaultMapper;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class RoomData {

    public static List<RoomData> omegaRooms = new ArrayList<>();
    public static List<RoomData> challengeRooms = new ArrayList<>();
    public static void initRooms() {
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


    public String type;
    public String name;
    public String simpleName;
    public Template room;
    public Map<Integer, Block> northeastColumn = new HashMap<>();
    public Map<Integer,Block> northwestColumn = new HashMap<>();
    public Map<Integer,Block> southeastColumn = new HashMap<>();
    public Map<Integer,Block> southwestColumn = new HashMap<>();

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
    }



}
