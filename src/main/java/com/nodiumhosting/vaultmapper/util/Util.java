package com.nodiumhosting.vaultmapper.util;

public class Util {
    public static String RandomColor() {
        return "#" + RandomHex() + RandomHex() + RandomHex() + RandomHex() + RandomHex() + RandomHex();
    }

    public static String RandomHex() {
        return Integer.toHexString((int)(Math.random()*0xF));
    }
}
