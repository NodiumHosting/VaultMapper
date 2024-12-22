package com.nodiumhosting.vaultmapper.util;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.proto.RoomName;

public class Util {
    public static RoomName RoomFromName(String name) {
        for (RoomName roomName : RoomName.values()) {
            if (roomName.name().equals(name)) {
                return roomName;
            }
        }
        VaultMapper.LOGGER.info("Unknown Room detected: " + name);
        return RoomName.ROOMNAME_UNKNOWN;
    }

    public static String NameFromRoom(RoomName name) {
        if (name == null) {
            return "UNKNOWN";
        }
        switch (name) {
            case ROOMNAME_UNKNOWN:
                return "UNKNOWN";
            case ROOMNAME_BLACKSMITH:
                return "Blacksmith";
            case ROOMNAME_COVE:
                return "Cove";
            case ROOMNAME_CRYSTAL_CAVES:
                return "Crystal Caves";
            case ROOMNAME_DIG_SITE:
                return "Dig Site";
            case ROOMNAME_DRAGON:
                return "Dragon";
            case ROOMNAME_FACTORY:
                return "Factory";
            case ROOMNAME_LIBRARY:
                return "Library";
            case ROOMNAME_MINE:
                return "Mine";
            case ROOMNAME_MUSH_ROOM:
                return "Mush Room";
            case ROOMNAME_PAINTING:
                return "Painting";
            case ROOMNAME_VENDOR:
                return "Vendor";
            case ROOMNAME_VILLAGE:
                return "Village";
            case ROOMNAME_WILD_WEST:
                return "Wild West";
            case ROOMNAME_X_MARK:
                return "X-mark";
            case ROOMNAME_CUBE:
                return "Cube";
            case ROOMNAME_LAB:
                return "Laboratory";
            case ROOMNAME_RAID:
                return "Raid";
            default:
                return "UNKNOWN";
        }
    }
}
