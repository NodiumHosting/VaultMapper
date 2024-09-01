package com.nodiumhosting.vaultmapper.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> Map_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<String> POINTER_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> START_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> MARKED_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> INSCRIPTION_ROOM_COLOR;

    static {
        BUILDER.push("VaultMapper Client Config");

        MAP_X_OFFSET = BUILDER.comment("Offset the Map from the default position (bottom right) on the x-axis").define("MAP_X_OFFSET", 0);
        Map_Y_OFFSET = BUILDER.comment("Offset the Map from the default position (bottom right) on the y-axis").define("Map_Y_OFFSET", 0);
        POINTER_COLOR = BUILDER.comment("Color for the current player position").define("POINTER_COLOR", "#00FF00");
        ROOM_COLOR = BUILDER.comment("Color for normal Rooms & Hallways").define("ROOM_COLOR", "#0000FF");
        START_ROOM_COLOR = BUILDER.comment("Color for the Start room of a Vault").define("START_ROOM_COLOR", "#FF0000");
        MARKED_ROOM_COLOR = BUILDER.comment("Color for a Marked Vault Room").define("MARKED_ROOM_COLOR", "#FF00FF");
        INSCRIPTION_ROOM_COLOR = BUILDER.comment("Color for a Inscripted Vault Room").define("INSCRIPTION_ROOM_COLOR", "#FFFF00");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
