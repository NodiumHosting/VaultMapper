package com.nodiumhosting.vaultmapper.map;

import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

public class RoomBlockData {
    public int blocks;
    public int ores;
    public int chests;
    public int coins;
    public int bedrock;
    public int chromaticIron;

    public RoomBlockData() {
    }

    @Override public String toString() {
        return "RoomBlockData{" +
            "blocks=" + blocks +
            ", ores=" + ores +
            ", chests=" + chests +
            ", coins=" + coins +
            ", bedrock=" + bedrock +
            ", nonBedrock=" + (blocks - bedrock) +
            '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoomBlockData that = (RoomBlockData) o;
        return blocks == that.blocks && ores == that.ores && chests == that.chests && coins == that.coins &&
            bedrock == that.bedrock;
    }

    @Override public int hashCode() {
        return Objects.hash(blocks, ores, chests, coins, bedrock);
    }

    public static RoomBlockData getRoomBlockData(int cellX, int cellZ) {
        return IntStream.rangeClosed(9, 54).parallel().mapToObj(y ->
            IntStream.rangeClosed(0, 46).parallel().mapToObj(x ->
                IntStream.rangeClosed(0, 46).parallel().mapToObj(z -> VaultMap.getCellBlock(cellX, cellZ, x, y, z))
            ).flatMap(Function.identity())
        ).flatMap(Function.identity()).collect(
            RoomBlockData::new,
            (data, block) -> {
                if (block == null) return;
                if (block == ModBlocks.VAULT_STONE || block instanceof VaultOreBlock) {
                    data.ores++;
                } else if (block == ModBlocks.COIN_PILE) {
                    data.coins++;
                } else if (block instanceof VaultChestBlock) {
                    data.chests++;
                } else if (block == ModBlocks.VAULT_BEDROCK) {
                    data.bedrock++;
                } else if (block == ModBlocks.CHROMATIC_IRON_ORE) {
                    data.chromaticIron++;
                }
                data.blocks++;
            },
            (data1, data2) -> {
                data1.blocks += data2.blocks;
                data1.ores += data2.ores;
                data1.coins += data2.coins;
                data1.chests += data2.chests;
                data1.bedrock += data2.bedrock;
                data1.chromaticIron += data2.chromaticIron;
            }
        );
    }
}
