package com.nodiumhosting.vaultmapper.map;

public class VaultMapRoom {
    public int x;
    public int z;
    public VaultMapRoomColor mapColor;
    public int mapStartX;
    public int mapStartZ;
    public int mapEndX;
    public int mapEndZ;

    public VaultMapRoom(int x, int z, VaultMapRoomColor mapColor, int mapStartX, int mapStartZ, int mapEndX, int mapEndZ) {
        this.x = x;
        this.z = z;
        this.mapColor = mapColor;
        this.mapStartX = mapStartX;
        this.mapStartZ = mapStartZ;
        this.mapEndX = mapEndX;
        this.mapEndZ = mapEndZ;
    }
}
