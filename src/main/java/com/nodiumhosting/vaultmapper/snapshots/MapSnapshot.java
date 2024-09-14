package com.nodiumhosting.vaultmapper.snapshots;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.screen.VaultMapPreviewScreen;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class MapSnapshot {
    public static LinkedHashMap<UUID,MapSnapshot> savedMaps;

    public static final String mapSavePath = "config/vaultmaps.json";

    public static final int maxMaps = 50;

    public static MapSnapshot lastSnapshotCache;

    public static void onVaultExit(UUID vaultUUID) {
        if (lastSnapshotCache == null) {
            return;
        }
        MapSnapshot.addMap(vaultUUID,lastSnapshotCache);
    }

    public static void readFromJsonFile() {
        File savedMapsFile = new File(mapSavePath);
        if (!savedMapsFile.exists()) {
            savedMaps = new LinkedHashMap<>();
            writeToJsonFile();
            VaultMapper.LOGGER.info("Map saves file created");
            return;
        }

        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader(mapSavePath);
            Type saveType = new TypeToken<LinkedHashMap<UUID, MapSnapshot>>() {}.getType();
            savedMaps = gson.fromJson(reader, saveType);
        } catch (FileNotFoundException e) {
            VaultMapper.LOGGER.error("Couldn't read map save file");
        }
    }

    public static MapSnapshot takeSnapshot() {
        ArrayList<VaultCell> cells = new ArrayList<>(VaultMap.getCells());
        return new MapSnapshot(cells);
    }

    public static void writeToJsonFile() {
        Gson gson = new Gson();
        try {
            FileWriter writer = new FileWriter(mapSavePath);
            gson.toJson(savedMaps, writer);
            writer.close();
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Couldn't update map save file");
        }
    }

    public static void addMap(UUID uuid, MapSnapshot snapshot) {
        if (savedMaps == null) {
            readFromJsonFile();
        }
        savedMaps.put(uuid,snapshot);
        removeExcessMaps();
        writeToJsonFile();
    }

    public static void removeExcessMaps() {
        if (!(savedMaps.size() > maxMaps)) {
            return;
        }
        for (Map.Entry<UUID,MapSnapshot> map : savedMaps.entrySet()) {
            if (!map.getValue().isFavorite) {
                savedMaps.remove(map.getKey());
                break;
            }
        }
    }

    public static Optional<MapSnapshot> from(UUID uuid) {
        if (savedMaps == null) {
            readFromJsonFile();
        }
        if (!savedMaps.containsKey(uuid)) {
            return Optional.empty();
        }
        return Optional.of(savedMaps.get(uuid));
    }

    public List<VaultCell> cells;
    public boolean isFavorite = false;
    public MapSnapshot(List<VaultCell> cells) {
        this.cells = cells;
    }

    public void openScreen(Optional<Screen> previousScreen) {
        VaultMapPreviewScreen cellsScreen = new VaultMapPreviewScreen(this, previousScreen);
        Minecraft.getInstance().setScreen(cellsScreen);
    }
}
