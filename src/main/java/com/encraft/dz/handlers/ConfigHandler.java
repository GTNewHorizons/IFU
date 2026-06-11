package com.encraft.dz.handlers;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.encraft.dz.DayNMod;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

    public static Configuration cfg;

    public static boolean wandSound = true;
    public static int xzAreaRadius = 4;
    public static int yAreaRadius = 40;
    public static String[] blocklist = {};
    public static String[] materialBlocklist = {};
    public static String[] allowlist = {};
    public static boolean aEnableEverywhere = false;
    public static boolean debugBlockInfo = false;

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

        blocklist = cfg.getStringList(
                "Blocklist",
                cfg.CATEGORY_GENERAL,
                blocklist,
                "Blocks the Ore Finder must never search for, that it would otherwise match on its own. "
                        + "Use the block Item ID, same rules as Allowlist");

        materialBlocklist = cfg.getStringList(
                "Material Blocklist",
                cfg.CATEGORY_GENERAL,
                materialBlocklist,
                "Ore materials the Ore Finder must never search for. "
                        + "Use the material name printed by the Debug block info option, for example: "
                        + "\"Gold\" or \"MeteoricIron\".");

        allowlist = cfg.getStringList(
                "Allowlist",
                cfg.CATEGORY_GENERAL,
                allowlist,
                "Extra non-ore blocks allowed by Ore Finder. "
                        + "Use the block Item ID with an optional metadata suffix: "
                        + "\"minecraft:cobblestone\" matches any metadata, \"gregtech:gt.blockores2:307\" matches that specific one");

        debugBlockInfo = cfg.getBoolean(
                "Debug block info",
                cfg.CATEGORY_GENERAL,
                false,
                "If true, right-clicking a block with the Ore Finder prints its name, metadata, ore material/flags to chat. "
                        + "Useful for diagnosing ore matching and for finding what to put in an Allow/Block list");

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

}
