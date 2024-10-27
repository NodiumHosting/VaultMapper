package com.nodiumhosting.vaultmapper.snapshots;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.util.BooleanSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class MapCache {
    public static String cachePath = "vaultmaps/cache.json";

    public static void deleteCache() {
        MapSnapshot.makeSureFoldersExist();
        File mapFile = new File(cachePath);
        if (mapFile.exists()) {
            mapFile.delete();
        }
    }

    public static void updateCache() {
        MapSnapshot.makeSureFoldersExist();
        GsonBuilder gsonBuilder = new GsonBuilder();
        BooleanSerializer serializer = new BooleanSerializer();
        gsonBuilder.registerTypeAdapter(Boolean.class, serializer);
        gsonBuilder.registerTypeAdapter(boolean.class, serializer);
        Gson gson = gsonBuilder.create();
        try {
            FileWriter writer = new FileWriter(cachePath);
            gson.toJson(VaultMap.getCells(), writer);
            writer.close();
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Couldn't create map cache file");
        }
    }

    public static void readCache() {
        MapSnapshot.makeSureFoldersExist();
        File mapFile = new File(cachePath);
        if (!mapFile.exists()) {
            return;
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        BooleanSerializer serializer = new BooleanSerializer();
        gsonBuilder.registerTypeAdapter(Boolean.class, serializer);
        gsonBuilder.registerTypeAdapter(boolean.class, serializer);
        Gson gson = gsonBuilder.create();
        try {
            FileReader reader = new FileReader(cachePath);
            Type saveType = new TypeToken<List<VaultCell>>() {
            }.getType();
            VaultMap.cells = (gson.fromJson(reader, saveType));
        } catch (FileNotFoundException e) {
        }
    }

}
