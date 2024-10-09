package com.nodiumhosting.vaultmapper.map;

import com.google.gson.annotations.SerializedName;

public enum RoomName {
    @SerializedName("0") UNKNOWN("UNKNOWN"),
    @SerializedName("1") BLACKSMITH("Blacksmith"),
    @SerializedName("2") COVE("Cove"),
    @SerializedName("3") CRYSTAL_CAVES("Crystal Caves"),
    @SerializedName("4") DIGSITE("Digsite"),
    @SerializedName("5") DRAGON("Dragon"),
    @SerializedName("6") FACTORY("Factory"),
    @SerializedName("7") LIBRARY("Library"),
    @SerializedName("8") MINE("Mine"),
    @SerializedName("9") MUSH_ROOM("Mush Room"),
    @SerializedName("10") PAINTING("Painting"),
    @SerializedName("11") VENDOR("Vendor"),
    @SerializedName("12") VILLAGE("Village"),
    @SerializedName("13") WILD_WEST("Wild West"),
    @SerializedName("14") X_MARK("X-mark");

    private final String name;

    RoomName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static RoomName fromName(String name) {
        for (RoomName roomName : values()) {
            if (roomName.getName().equals(name)) {
                return roomName;
            }
        }
        return RoomName.UNKNOWN;
    }
}
