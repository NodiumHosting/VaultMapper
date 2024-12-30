package com.nodiumhosting.vaultmapper.util;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.proto.RoomName;

import java.util.Map;

public class Util {
    private static final Map<String, RoomName> roomNameMap = Map.ofEntries(
            Map.entry("Blacksmith", RoomName.ROOMNAME_BLACKSMITH),
            Map.entry("Cove", RoomName.ROOMNAME_COVE),
            Map.entry("Crystal Caves", RoomName.ROOMNAME_CRYSTAL_CAVES),
            Map.entry("Dig Site", RoomName.ROOMNAME_DIG_SITE),
            Map.entry("Dragon", RoomName.ROOMNAME_DRAGON),
            Map.entry("Factory", RoomName.ROOMNAME_FACTORY),
            Map.entry("Library", RoomName.ROOMNAME_LIBRARY),
            Map.entry("Mine", RoomName.ROOMNAME_MINE),
            Map.entry("Mush Room", RoomName.ROOMNAME_MUSH_ROOM),
            Map.entry("Painting", RoomName.ROOMNAME_PAINTING),
            Map.entry("Vendor", RoomName.ROOMNAME_VENDOR),
            Map.entry("Village", RoomName.ROOMNAME_VILLAGE),
            Map.entry("Wild West", RoomName.ROOMNAME_WILD_WEST),
            Map.entry("X-mark", RoomName.ROOMNAME_X_MARK),
            Map.entry("Cube", RoomName.ROOMNAME_CUBE),
            Map.entry("Laboratory", RoomName.ROOMNAME_LAB),
            Map.entry("Raid", RoomName.ROOMNAME_RAID)
    );

    public static RoomName RoomFromName(String name) {
        if (name == null) {
            return RoomName.ROOMNAME_UNKNOWN;
        }
        if (roomNameMap.containsKey(name)) {
            return roomNameMap.get(name);
        }
        VaultMapper.LOGGER.info("Unknown Room detected: " + name);
        return RoomName.ROOMNAME_UNKNOWN;
    }

    public static String NameFromRoom(RoomName name) {
        for (Map.Entry<String, RoomName> entry : roomNameMap.entrySet()) {
            if (entry.getValue() == name) {
                return entry.getKey();
            }
        }
        return "Unknown";
    }

    public static String RandomColor() {
        return "#" + RandomHex() + RandomHex() + RandomHex() + RandomHex() + RandomHex() + RandomHex();
    }

    public static String RandomHex() {
        return Integer.toHexString((int)(Math.random()*0xF));
    }
}
