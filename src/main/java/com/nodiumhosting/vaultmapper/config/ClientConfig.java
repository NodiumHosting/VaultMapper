package com.nodiumhosting.vaultmapper.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> MAP_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_X_ANCHOR;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_Y_ANCHOR;
    public static final ForgeConfigSpec.ConfigValue<String> POINTER_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> START_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> MARKED_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> INSCRIPTION_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OMEGA_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> CHALLENGE_ROOM_COLOR;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_INSCRIPTIONS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_ROOM_ICONS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WEBMAP_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_MAPS_SAVED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> IGNORE_RESEARCH_REQUIREMENT;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAP_SCALE;

    static {
        BUILDER.push("VaultMapper Client Config");

        MAP_ENABLED = BUILDER.comment("Enable rendering the map").define("MAP_ENABLED", true);

        MAP_X_OFFSET = BUILDER.comment("Offset the Map from the default position (bottom right) on the x-axis").define("MAP_X_OFFSET", 0);
        MAP_Y_OFFSET = BUILDER.comment("Offset the Map from the default position (bottom right) on the y-axis").define("Map_Y_OFFSET", 0);

        MAP_X_ANCHOR = BUILDER.comment("Anchor the Map on the x-axis (0-left, 2-center, 4-right)").define("MAP_X_ANCHOR", 4);
        MAP_Y_ANCHOR = BUILDER.comment("Anchor the Map on the y-axis (0-top, 2-center, 4-bottom)").define("MAP_Y_ANCHOR", 4);

        MAP_SCALE = BUILDER.comment("Scale of the map, 3 to 30").define("MAP_SCALE",10);

        POINTER_COLOR = BUILDER.comment("Color for the current player position").define("POINTER_COLOR", "#00FF00");
        ROOM_COLOR = BUILDER.comment("Color for normal Rooms & Hallways").define("ROOM_COLOR", "#0000FF");
        START_ROOM_COLOR = BUILDER.comment("Color for the Start room of a Vault").define("START_ROOM_COLOR", "#FF0000");
        MARKED_ROOM_COLOR = BUILDER.comment("Color for a Marked Vault Room").define("MARKED_ROOM_COLOR", "#FF00FF");
        INSCRIPTION_ROOM_COLOR = BUILDER.comment("Color for a Inscripted Vault Room").define("INSCRIPTION_ROOM_COLOR", "#FFFF00");
        OMEGA_ROOM_COLOR = BUILDER.comment("Color for an explored Omega Room").define("OMEGA_ROOM_COLOR", "#55FF55");
        CHALLENGE_ROOM_COLOR = BUILDER.comment("Color for an explored Challenge Room").define("CHALLENGE_ROOM_COLOR", "#F09E00");
        SHOW_INSCRIPTIONS = BUILDER.comment("Show Inscripted Rooms on the Map").define("SHOW_INSCRIPTIONS", true);
        SHOW_ROOM_ICONS = BUILDER.comment("Show Room Icons on the Map").define("SHOW_ROOM_ICONS", true);

        WEBMAP_ENABLED = BUILDER.comment("Enable the WebMap Server").define("WEBMAP_ENABLED", false);

        MAX_MAPS_SAVED = BUILDER.comment("The maximum amount of map history snapshots that can be saved on file.\n" +
                "Favorites will be saved forever regardless of this number.\n" +
                "This number is a global number, not per world/server.\n" +
                "Inputting a negative number disables the cap").define("MAX_MAPS_SAVED", -1);

        IGNORE_RESEARCH_REQUIREMENT = BUILDER.comment("Option to ignore the Vault Compass research requirement for Vault Map.\n" +
                        "Please don't abuse this option on servers where you don't have permission to do so.")
                .define("IGNORE_RESEARCH_REQUIREMENT", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
