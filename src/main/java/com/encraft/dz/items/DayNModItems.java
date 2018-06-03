package com.encraft.dz.items;

import java.util.ArrayList;

import com.encraft.dz.DayNMod;
import com.encraft.dz.lib.Reference;
import com.encraft.dz.proxy.CommonProxy;




import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.util.EnumHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
