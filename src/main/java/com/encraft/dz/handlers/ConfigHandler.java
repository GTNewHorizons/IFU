package com.encraft.dz.handlers;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.encraft.dz.DayNMod;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

    public static Configuration cfg;

    public static boolean updateCheck = true;
    public static boolean wandSound = true;
    public static int xzAreaRadius = 4;
    public static int yAreaRadius = 40;
    public static String[] blacklist = {};
    public static boolean aEnableEverywhere = false;

    public static boolean dmgBar = true;

    public static void init(String configDir) {
        if (cfg == null) {
            File path = new File(configDir + "/" + DayNMod.MOD_ID + ".cfg");
            cfg = new Configuration(path);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {

        xzAreaRadius = cfg.getInt(
                "X Z Area radius",
                cfg.CATEGORY_GENERAL,
                xzAreaRadius,
                1,
                16,
                " change scanning radius from player");
        yAreaRadius = cfg.getInt(
                "Y Area radius",
                cfg.CATEGORY_GENERAL,
                yAreaRadius,
                1,
                60,
                " change scanning distance above and below the player");
        aEnableEverywhere = cfg.getBoolean(
                "Enable Everywhere",
                cfg.CATEGORY_GENERAL,
                false,
                "If this is set to false, the OreFinder will only work in the Owerworld, Nether and Twilight Forest");
        wandSound = cfg.getBoolean("Sounds", cfg.CATEGORY_GENERAL, wandSound, "If true, Ore finder will play sounds");

        blacklist = cfg.getStringList(
                "Blacklist ",
                cfg.CATEGORY_GENERAL,
                blacklist,
                "List blocks that can't be read by ore Finder. Use unlocalizedName.");

        if (cfg.hasChanged()) {

            cfg.save();

        }
    }

    @SubscribeEvent
    public void onConfigChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(DayNMod.MOD_ID)) {
            loadConfiguration();
        }
    }

    public static Configuration getConfiguration() {
        return cfg;
    }

}
