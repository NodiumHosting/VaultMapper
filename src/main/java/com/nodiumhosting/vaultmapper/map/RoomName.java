package com.nodiumhosting.vaultmapper.map;

import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;

public enum RoomName {
    @SerializedName("0") UNKNOWN("UNKNOWN"),
    @SerializedName("1") BLACKSMITH("Blacksmith"),
    @SerializedName("2") COVE("Cove"),
    @SerializedName("3") CRYSTAL_CAVES("Crystal Caves"),
    @SerializedName("4") DIG_SITE("Dig Site"),
    @SerializedName("5") DRAGON("Dragon"),
    @SerializedName("6") FACTORY("Factory"),
    @SerializedName("7") LIBRARY("Library"),
    @SerializedName("8") MINE("Mine"),
    @SerializedName("9") MUSH_ROOM("Mush Room"),
    @SerializedName("10") PAINTING("Painting"),
    @SerializedName("11") VENDOR("Vendor"),
    @SerializedName("12") VILLAGE("Village"),
    @SerializedName("13") WILD_WEST("Wild West"),
    @SerializedName("14") X_MARK("X-mark"),
    @SerializedName("15") CUBE("Cube"),
    @SerializedName("16") LAB("Laboratory"),
    @SerializedName("17") RAID("Raid");
    private final String name;

    RoomName(String name) {
        this.name = name;
    }

    public static RoomName fromName(String name) {
        for (RoomName roomName : values()) {
            if (roomName.getName().equals(name)) {
                return roomName;
            }
        }
        VaultMapper.LOGGER.info("Unknown Room detected: " + name);
        return RoomName.UNKNOWN;
    }

    public String getName() {
        return name;
    }
}
