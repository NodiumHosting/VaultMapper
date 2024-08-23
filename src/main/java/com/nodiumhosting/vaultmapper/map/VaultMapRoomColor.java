package com.nodiumhosting.vaultmapper.map;

public enum VaultMapRoomColor {
    ROOM(0xFF0000FF),
    TUNNEL(0xFF0000FF),
    CURRENT(0xFF00FF00),
    START(0xFFFF0000),
    INSCRIPTION(0xFFFFD700),
    IMPORTANT(0xFFFF00FF);

    private final int color;

    VaultMapRoomColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
