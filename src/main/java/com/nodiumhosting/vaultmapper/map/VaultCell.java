package com.nodiumhosting.vaultmapper.map;

public class VaultCell {
    public int x;
    public int z;
    public boolean marked = false;
    public boolean inscripted = false;
    public CellType cellType;
    public RoomType roomType;

    public VaultCell(int x, int z, CellType cellType, RoomType roomType) {
        this.x = x;
        this.z = z;
        this.cellType = cellType;
        this.roomType = roomType;
    }
}
