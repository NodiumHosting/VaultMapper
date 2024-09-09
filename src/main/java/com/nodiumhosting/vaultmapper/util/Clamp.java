package com.nodiumhosting.vaultmapper.util;

public class Clamp {
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
