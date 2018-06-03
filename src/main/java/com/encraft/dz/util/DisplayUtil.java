package com.encraft.dz.util;


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

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