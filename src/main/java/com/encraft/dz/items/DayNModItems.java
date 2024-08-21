package com.encraft.dz.items;

import net.minecraft.item.Item;

import com.encraft.dz.DayNMod;
import com.encraft.dz.proxy.CommonProxy;

import cpw.mods.fml.common.registry.GameRegistry;

public class DayNModItems {

    public static CommonProxy proxy;

    public static Item buildingKit;

    public static void init() {

    }

    public static void load() {
        buildingKit = new ItemOreFinderTool().setCreativeTab(DayNMod.dnCVt)
                .setUnlocalizedName(DayNMod.MOD_ID + "_" + "buildingKit");
        GameRegistry.registerItem(buildingKit, buildingKit.getUnlocalizedName().substring(5));

    }

}
