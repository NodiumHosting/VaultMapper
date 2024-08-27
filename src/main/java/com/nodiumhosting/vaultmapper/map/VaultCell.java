package com.nodiumhosting.vaultmapper.map;

public class VaultCell {
    CellType type;
    TunnelType tType;
    int x;
    int z;

    public VaultCell() {
        type = CellType.NONE;
        tType = TunnelType.NONE;
        x = 0;
        z = 0;
    }
    public VaultCell(CellType cellType, int room_x, int room_z) {
        type = cellType;
        x = room_x;
        z = room_z;
    }
    public VaultCell(CellType cellType, TunnelType tunnelType, int room_x, int room_z) {
        type = cellType;
        tType = tunnelType;
        x = room_x;
        z = room_z;
    }
}

