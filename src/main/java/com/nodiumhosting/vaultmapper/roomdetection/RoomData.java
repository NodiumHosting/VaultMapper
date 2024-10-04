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
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.ClassicVaultLayout;
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.world.template.data.DirectTemplateEntry;
import iskallia.vault.core.world.template.data.IndirectTemplateEntry;
import iskallia.vault.core.world.template.data.TemplatePool;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RoomData {

    public static List<RoomData> omegaRooms;
    public static List<RoomData> challengeRooms;
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
                VaultMapper.LOGGER.info("Challenge Room Batch name: " + roomBatch.getPath());
                roomBatch.iterate((inner) -> {
                    if (inner instanceof DirectTemplateEntry roomFileRef) {
                        if (!roomFileRef.getTemplate().supports(Version.latest())) {
                            return true;
                        }
                        Template roomFile = roomFileRef.getTemplate().get(Version.latest());

                        VaultMapper.LOGGER.info("Room File name: " + roomFileRef.getTemplate().getId());
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
                VaultMapper.LOGGER.info("Omega Room Batch name: " + roomBatch.getPath());
                roomBatch.iterate((inner) -> {
                    if (inner instanceof DirectTemplateEntry roomFileRef) {
                        if (!roomFileRef.getTemplate().supports(Version.latest())) {
                            return true;
                        }
                        Template roomFile = roomFileRef.getTemplate().get(Version.latest());

                        VaultMapper.LOGGER.info("Room File name: " + roomFileRef.getTemplate().getId());
                    }
                    return true;
                });
            }

            return true;
        }));
    }


    public String type;
    public String name;
    public Template room;
    public List<String> northeastColumn;
    public List<String> northwestColumn;
    public List<String> southeastColumn;
    public List<String> southwestColumn;

    public RoomData(String type, String name, Template room) {
        this.type = type;
        this.name = name;
        this.room = room;
    }

}
