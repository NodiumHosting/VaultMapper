package com.nodiumhosting.vaultmapper.Snapshots;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.screen.VaultMapperEndVaultScreen;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import net.minecraft.client.Minecraft;

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
        ArrayList<VaultCell> inscriptionRooms = new ArrayList<>(VaultMap.getInscriptionRooms());
        ArrayList<VaultCell> markedRooms = new ArrayList<>(VaultMap.getMarkedRooms());
        return new MapSnapshot(cells,inscriptionRooms,markedRooms);
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
    public List<VaultCell> inscriptionRooms;
    public List<VaultCell> markedRooms;
    public boolean isFavorite = false;
    public MapSnapshot() {

    }
    public MapSnapshot(List<VaultCell> cells, List<VaultCell> inscriptionRooms, List<VaultCell> markedRooms) {
        this.cells = cells;
        this.inscriptionRooms = inscriptionRooms;
        this.markedRooms = markedRooms;
    }

    public MapSnapshot(String cellsJson, String inscriptionRoomsJson, String markedRoomsJson) {
        String newCellsJson = new String(java.util.Base64.getDecoder().decode(cellsJson.replaceAll("-", "=")));
        String newInscriptionRoomsJson = new String(java.util.Base64.getDecoder().decode(inscriptionRoomsJson.replaceAll("-", "=")));
        String newMarkedRoomsJson = new String(java.util.Base64.getDecoder().decode(markedRoomsJson.replaceAll("-", "=")));

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<VaultCell>>(){}.getType();
        cells = gson.fromJson(newCellsJson, listType);
        inscriptionRooms = gson.fromJson(newInscriptionRoomsJson, listType);
        markedRooms = gson.fromJson(newMarkedRoomsJson, listType);
    }
    public void openScreen() {
        VaultMapperEndVaultScreen cellsScreen = new VaultMapperEndVaultScreen(this);
        Minecraft.getInstance().setScreen(cellsScreen);
    }


}
