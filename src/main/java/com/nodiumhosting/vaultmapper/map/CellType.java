package com.nodiumhosting.vaultmapper.map;

import com.google.gson.annotations.SerializedName;

public enum CellType {
    @SerializedName("0") NONE,
    @SerializedName("1") ROOM,
    @SerializedName("2") TUNNEL_X,
    @SerializedName("3") TUNNEL_Z
}
