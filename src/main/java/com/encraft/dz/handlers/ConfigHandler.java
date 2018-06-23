package com.encraft.dz.handlers;

import java.io.File;

import com.encraft.dz.lib.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	
	public static Configuration cfg;
	
	public static boolean updateCheck = true;
	public static boolean wandSound = true;
	public static int xzAreaRadius = 4;
	public static int yAreaRadius = 40;
	public static  String[] blacklist = {};
	
	
	
	public static boolean dmgBar = true;
	
	public static void init(String configDir){
		if (cfg == null){
			File path = new File(configDir +"/" +Reference.MOD_ID + ".cfg");
			cfg =  new Configuration(path);
			loadConfiguration();
		}
	}

	private static void loadConfiguration() {
		
		xzAreaRadius = cfg.getInt("X Z Area radius", cfg.CATEGORY_GENERAL, xzAreaRadius, 1, 16, " change scanning radius from player");
		yAreaRadius = cfg.getInt("Y Area radius", cfg.CATEGORY_GENERAL, yAreaRadius, 1, 60, " change scanning distance above and below the player");
		
		wandSound = cfg.getBoolean("Sounds",  cfg.CATEGORY_GENERAL, wandSound, "If true, Ore finder will play sounds");
		
		blacklist = cfg.getStringList("Blacklist ", cfg.CATEGORY_GENERAL, blacklist, "List of GT ores that can't be readed by ore Finder. Use block unlocalizedName with meta < 1000, f.e. \"gt.blockores.27\"");
		
		if (cfg.hasChanged()){
			
			cfg.save();
			
		}
	}
	
	@SubscribeEvent
	public void onConfigChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event){
		if (event.modID.equalsIgnoreCase(Reference.MOD_ID)){
			loadConfiguration();
		}
	}
	
	public static Configuration getConfiguration(){return cfg;}
	
}
