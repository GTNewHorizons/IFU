package com.encraft.dz.items;



import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.lib.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.common.blocks.GT_TileEntity_Ores;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemOreFinderTool extends Item {

	private ConfigHandler cfg;

	private static Long lastUpdate = Long.valueOf(0L);
	private static Long millisPerUpdate = Long.valueOf(100L);
	private static boolean isClient;
	private static int distanceMax;
	private static int toFind;
	private static String toFindStr;
	private static String toFindStr2;
	private static String configComment;
	private static int distancePerLevel;

	private String[] dupa = cfg.whitelist;
	private static IIcon[] iconIndexes;
	private static int distanceShortest = -1;
	private static int lastState = 0;
	private static ArrayList found = new ArrayList();

	private static int[] vectorShortest = new int[3];
	private int id;

	Block oreBlock = null;

	private String toFindStr3;


	public ItemOreFinderTool()
	{
		setUnlocalizedName(Reference.MOD_ID + "_" + "buildingKitItem");
		setMaxDamage(100);
		setMaxStackSize(1);
		setTextureName(Reference.MOD_ID + ":meter0");

		distanceMax = cfg.xzAreaRadius;

		distancePerLevel = distanceMax / 4;
		if (distancePerLevel < 1) {
			distancePerLevel = 1;
		}
	}


	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		iconIndexes = new IIcon[5];
		for (int i = 0; i <= 4; i++) {
			iconIndexes[i] = p_94581_1_.registerIcon(Reference.MOD_ID + ":meter" + i);
		}
	}


	public boolean canItemEditBlocks()
	{
		return false;
	}

	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
		itemStack.stackTagCompound = new NBTTagCompound();
	}

	//TODO -----------------------------------------------------------------------------------------------
	public static ItemStack inventoryContainsAAD(InventoryPlayer inventory) {
		ItemStack itemstack = null;
		for (ItemStack s : inventory.mainInventory) {
			if (s != null && s.getItem() instanceof ItemOreFinderTool ) {
				itemstack = s;
				break;
			}
		}
		return itemstack;
	}

	@SideOnly(Side.CLIENT)
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) 
	{
		if (!itemstack.hasTagCompound()) {
			itemstack.setTagCompound(new NBTTagCompound());
		}

		if (ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0) != null
				&& inventoryContainsAAD(Minecraft.getMinecraft().thePlayer.inventory) != null){

			toFindStr = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0).getUnlocalizedName();	

			if (new Date().getTime() <= lastUpdate.longValue()
					+ millisPerUpdate.longValue()) {
				return;
			}
			lastUpdate = Long.valueOf(new Date().getTime());
			distanceShortest = -1;

			if ((entity == null) || (world == null)) {
				return;
			}
			if (!entity.getClass().getSimpleName().equals("EntityClientPlayerMP")) {
				return;
			}



			double cur_x = entity.posX;
			double cur_y = entity.posY;
			double cur_z = entity.posZ;

			int min_x = (int) cur_x - distanceMax - 1;
			int min_y = (int) cur_y - cfg.yAreaRadius;
			int min_z = (int) cur_z - distanceMax;

			int max_x = (int) cur_x + distanceMax;
			int max_y = (int) cur_y + distanceMax;
			int max_z = (int) cur_z + distanceMax + 1;
			for (int z1 = min_z; z1 < max_z; z1++) {
				for (int x1 = min_x; x1 < max_x; x1++) {
					for (int y1 = min_y; y1 < max_y; y1++) {

						Block tBlock = world.getBlock(x1, y1, z1);
						
						if (tBlock.getUnlocalizedName().equals(toFindStr)) {
							found.add(new int[] { x1, y1, z1 });
						}
						if (world.getBlock(x1, y1, z1).getUnlocalizedName().equals("gt.blockores")) {
							//if (world.getBlock(x1, y1, z1).getMaterial() == Material.rock || world.getBlock(x1, y1, z1) == GregTech_API.sBlockOres1) {

							short tMetaID = (short)world.getBlockMetadata(x1, y1, z1);
							TileEntity tTileEntity = world.getTileEntity(x1, y1, z1);
							tMetaID = (short)((GT_TileEntity_Ores) tTileEntity).getMetaData();
							/*String name = tBlock.getUnlocalizedName() + "." + tMetaID;
							if (name.equals(toFindStr)) {
								for (String s :dupa) {
									if (s != null && !s.equals(name)) {
										found.add(new int[] { x1, y1, z1 });
										break;
									}
								}	
							}*/
							ArrayList<ItemStack> drops = tBlock.getDrops(world, x1, y1, z1, tMetaID, 0);
							for (int i = 0; i<drops.size();i++){
								if ( drops.get(i).getUnlocalizedName().toString().equals(toFindStr)) {
									for (String s :dupa) {
										if (s != null && !s.equals( drops.get(i).getUnlocalizedName().toString())) {
											found.add(new int[] { x1, y1, z1 });
											break;
										}
									}	
								}
							}
						}
					}
				}
			}
		}else{
			toFindStr = "Empty";
			found.clear();
		}
		if (found.size() > 0 && found.size() <= 2) {this.itemIcon = iconIndexes[1];}
		else if (found.size() >= 3 && found.size() <= 4) {this.itemIcon = iconIndexes[2];}
		else if (found.size() >= 5 && found.size() <= 6) {this.itemIcon = iconIndexes[3];}
		else if (found.size() >= 7 ) {this.itemIcon = iconIndexes[4];}
		else if (found.size() <= 0){this.itemIcon = iconIndexes[0];}
		if (cfg.dmgBar){itemstack.setItemDamage(found.size()+1);}
		//System.out.println("You find: " + found.size());

		if (found.size() >= 4 && cfg.wandSound) {
			world.playSound(entity.posX, entity.posY, entity.posZ,"ic2:tools.Treetap", 0.6F, 0.8F,true); //(Minecraft.getMinecraft().thePlayer, "ic2:tools.Treetap", 1.0F, 1.0F);
		}

		found.clear();
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) 
	{
		if (itemStack.stackTagCompound != null)
		{

			if (ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0) != null){
				toFindStr2 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0).getDisplayName();	
				toFindStr3 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0).getUnlocalizedName();	
			}else{
				toFindStr2 = "Empty";
			}

			list.add("I want to find: " + toFindStr2 );
			list.add(toFindStr3 );
			list.add("Put ore block you want to find in item inventory -");
			list.add("SHIFT+RIGHT CLICK on ground to open");
			list.add("debug tool radius X, Z: "+ cfg.xzAreaRadius +" Y: "+cfg.yAreaRadius);

		}
	}

	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityPlayer, World world, int x, int y, int z, int sciankaKliknieta, float objetosc_x, float pbjetosc_y, float objetosc_z)
	{	

		if (entityPlayer.isSneaking()) {
			if (!world.isRemote){

				entityPlayer.openGui(DayNMod.instance,DayNMod.GUI_CUSTOM_INV1, entityPlayer.worldObj, (int) entityPlayer.posX, (int) entityPlayer.posY, (int) entityPlayer.posZ);
			}
		}
		else{
			
			// Only for tests!
			world.playSoundAtEntity(entityPlayer, "ic2:tools.Treetap", 1.0F, 1.0F);
		
			Block tBlock = world.getBlock(x, y, z);
			short tMetaID = (short)world.getBlockMetadata(x, y, z);
			
			TileEntity tTileEntity = world.getTileEntity(x, y, z);
				
			if(tTileEntity !=null){
				tMetaID = (short)((GT_TileEntity_Ores) tTileEntity).getMetaData();
				ArrayList<ItemStack> drops = tBlock.getDrops(world, x, y, z, tMetaID, 0);
				//    tBlock.getDrops(world, x, y, z, metadata, fortune)
				String name = tBlock.getDrops(world, x, y, z,tMetaID , 0) + "." + tMetaID;
				//System.out.println(drops.get(0).getUnlocalizedName());
				
				for (int i = 0; i<drops.size();i++)
					System.out.println("drops :" + (i+1) + " " + drops.get(i).getDisplayName());
				System.out.println(" -----------------   " );
			}
		}
		return true;
	}
}
