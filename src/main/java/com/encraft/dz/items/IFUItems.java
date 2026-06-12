package com.encraft.dz.items;

import net.minecraft.item.Item;

import com.encraft.dz.IFU;

import cpw.mods.fml.common.registry.GameRegistry;

public class IFUItems {

    public static Item oreFinder;

    public static void load() {
        oreFinder = new ItemOreFinderTool().setCreativeTab(IFU.creativeTab)
                .setUnlocalizedName(IFU.MOD_ID + "_" + "buildingKit");
        GameRegistry.registerItem(oreFinder, oreFinder.getUnlocalizedName().substring(5));

    }

}
