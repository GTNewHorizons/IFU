package com.encraft.dz.items;

import com.encraft.dz.DayNMod;
import com.encraft.dz.lib.Reference;
import com.encraft.dz.proxy.CommonProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class  DayNModItems {
	
	
	public static CommonProxy proxy;

    public static Item buildingKit;

    
	public static void init()
    {

    }

	
	public static void load()
	{
    	buildingKit = new ItemOreFinderTool().setCreativeTab(DayNMod.dnCVt).setUnlocalizedName(Reference.MOD_ID + "_" + "buildingKit");
    	GameRegistry.registerItem(buildingKit, buildingKit.getUnlocalizedName().substring(5));
    
	}

}
