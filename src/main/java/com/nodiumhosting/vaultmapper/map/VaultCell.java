package com.nodiumhosting.vaultmapper.map;

import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.proto.CellType;
import com.nodiumhosting.vaultmapper.proto.RoomName;
import com.nodiumhosting.vaultmapper.proto.RoomType;

public class VaultCell {
    public int x;
    public int z;

    @SerializedName("e")
    public boolean explored = false;

    @SerializedName("m")
    public boolean marked = false;

    @SerializedName("i")
    public boolean inscripted = false;

    @SerializedName("c")
    public CellType cellType;

    @SerializedName("r")
    public RoomType roomType;

    @SerializedName("n")
    public RoomName roomName = RoomName.ROOMNAME_UNKNOWN;

    public VaultCell(int x, int z, CellType cellType, RoomType roomType) {
        this.x = x;
        this.z = z;
        this.cellType = cellType;
        this.roomType = roomType;
    }

    /**
     * @return Result of operation (true if switched from off to on)
     */
    public boolean switchMarked() {
        this.marked = !this.marked;
        return this.marked;
    }

    public void setExplored(boolean explored) {
        this.explored = explored;
    }

    public String toString() {
        return "Cell " + this.x + ":" + this.z + " CellType: " + this.cellType + " RoomType: " + this.roomType + " RoomName: " + this.roomName + " Explored: " + this.explored + " Marked: " + this.marked + " Inscripted: " + this.inscripted;
    }
}
