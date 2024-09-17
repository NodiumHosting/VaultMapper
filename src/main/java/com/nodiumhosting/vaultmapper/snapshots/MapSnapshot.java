package com.nodiumhosting.vaultmapper.snapshots;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
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

    public static LinkedHashMap<UUID,MapSnapshot> favoriteMaps;

    public static final String mapSavePath = "config/vaultmaps.json";

    public static final String favoriteMapsPath = "config/vaultmapsfavs.json";


    public static MapSnapshot lastSnapshotCache;

    public static void onVaultExit(UUID vaultUUID) {
        if (lastSnapshotCache == null) {
            return;
        }
        MapSnapshot.addMap(vaultUUID,lastSnapshotCache);
    }

    public static void readSavesFromJsonFile() {
        File savedMapsFile = new File(mapSavePath);
        if (!savedMapsFile.exists()) {
            savedMaps = new LinkedHashMap<>();
            writeSavesToJsonFile();
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
    public static void writeSavesToJsonFile() {
        Gson gson = new Gson();
        try {
            FileWriter writer = new FileWriter(mapSavePath);
            gson.toJson(savedMaps, writer);
            writer.close();
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Couldn't update map save file");
        }
    }

    public static void toggleFavorite(UUID uuid) {
        if (favoriteMaps == null) {
            readFavFromJsonFile();
        }
        if (savedMaps == null) {
            readSavesFromJsonFile();
        }
        if (favoriteMaps.containsKey(uuid)) {
            favoriteMaps.remove(uuid);
            writeFavToJsonFile();
            return;
        }
        if (savedMaps.containsKey(uuid)) {
            favoriteMaps.put(uuid,savedMaps.get(uuid));
            writeFavToJsonFile();
        }
    }

    public static void setFavorite(UUID uuid, boolean favorite) {
        if (favoriteMaps == null) {
            readFavFromJsonFile();
        }
        if (savedMaps == null) {
            readSavesFromJsonFile();
        }
        if (favorite) {
            if (savedMaps.containsKey(uuid) && !favoriteMaps.containsKey(uuid)) {
                favoriteMaps.put(uuid,savedMaps.get(uuid));
                writeFavToJsonFile();
            }
        } else {
            favoriteMaps.remove(uuid);
            writeFavToJsonFile();
        }
    }


    public static void readFavFromJsonFile() {
        File favMapsFile = new File(favoriteMapsPath);
        if (!favMapsFile.exists()) {
            favoriteMaps = new LinkedHashMap<>();
            writeFavToJsonFile();
            VaultMapper.LOGGER.info("Map favorites file created");
            return;
        }

        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader(favoriteMapsPath);
            Type saveType = new TypeToken<LinkedHashMap<UUID, MapSnapshot>>() {}.getType();
            favoriteMaps = gson.fromJson(reader, saveType);
        } catch (FileNotFoundException e) {
            VaultMapper.LOGGER.error("Couldn't read map favorites save file");
        }
    }

    public static void writeFavToJsonFile() {
        Gson gson = new Gson();
        try {
            FileWriter writer = new FileWriter(favoriteMapsPath);
            gson.toJson(favoriteMaps, writer);
            writer.close();
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Couldn't update map fav save file");
        }
    }


    public static MapSnapshot takeSnapshot() {
        ArrayList<VaultCell> cells = new ArrayList<>(VaultMap.getCells());
        return new MapSnapshot(cells);
    }

    public static void addMap(UUID uuid, MapSnapshot snapshot) {
        if (savedMaps == null) {
            readSavesFromJsonFile();
        }
        if (!savedMaps.containsKey(uuid)) {
            savedMaps.put(uuid,snapshot);
        }
        removeExcessMaps();
        writeSavesToJsonFile();
    }

    public static void removeExcessMaps() {
        int maxMaps = ClientConfig.MAX_MAPS_SAVED.get();
        if (maxMaps < 0) {
            return;
        }

        if (!(savedMaps.size() > maxMaps)) {
            return;
        }
        for (Map.Entry<UUID,MapSnapshot> map : savedMaps.entrySet()) {
                savedMaps.remove(map.getKey());
                if ((savedMaps.size() <= maxMaps)) {
                    break;
                }
        }
    }

    public static Optional<MapSnapshot> from(UUID uuid) {
        if (savedMaps == null) {
            readSavesFromJsonFile();
        }
        if (savedMaps.containsKey(uuid)) {
            return Optional.of(savedMaps.get(uuid));
        }
        if (favoriteMaps == null) {
            readFavFromJsonFile();
        }

        if (favoriteMaps.containsKey(uuid)) {
            return Optional.of(favoriteMaps.get(uuid));
        }
        return Optional.empty();
    }

    public List<VaultCell> cells;
    public MapSnapshot(List<VaultCell> cells) {
        this.cells = cells;
    }

    public void openScreen(Optional<Screen> previousScreen) {
        VaultMapPreviewScreen cellsScreen = new VaultMapPreviewScreen(this, previousScreen);
        Minecraft.getInstance().setScreen(cellsScreen);
    }
}
