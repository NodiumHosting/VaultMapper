package com.nodiumhosting.vaultmapper.map;

import com.google.gson.annotations.SerializedName;

public enum RoomType {
    @SerializedName("0") START,
    @SerializedName("1") BASIC,
    @SerializedName("2") ORE,
    @SerializedName("3") CHALLENGE,
    @SerializedName("4") OMEGA
}
