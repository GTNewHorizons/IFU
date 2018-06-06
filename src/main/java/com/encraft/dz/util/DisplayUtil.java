package com.encraft.dz.util;


import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DisplayUtil
{
 

 
  public static List<String> itemDisplayNameMultiline(ItemStack itemstack)
  {
    List<String> namelist = null;
    try
    {
      namelist = itemstack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
    }
    catch (Throwable ignored) {}
    if (namelist == null) {
      namelist = new ArrayList();
    }
    if (namelist.size() == 0) {
      namelist.add("Unnamed");
    }
    if ((namelist.get(0) == null) || (((String)namelist.get(0)).equals(""))) {
      namelist.set(0, "Unnamed");
    }
    namelist.set(0, itemstack.getRarity().rarityColor.toString() + namelist.get(0));
    for (int i = 1; i < namelist.size(); i++) {
      namelist.set(i, "�7" + (String)namelist.get(i));
    }
    return namelist;
  }
  
  public static String itemDisplayNameShort(ItemStack itemstack)
  {
    List<String> list = itemDisplayNameMultiline(itemstack);
    return (String)list.get(0);
  }
  
 
}