package com.encraft.dz.items;



import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.lib.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.items.GT_MetaBase_Item;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import gregtech.common.blocks.GT_TileEntity_Ores;
import gregtech.common.items.behaviors.Behaviour_None;
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
    private static Long millisPerUpdate = Long.valueOf(250L);
    private static int MAX_DAMAGE = 10;
    private static boolean isClient;
    private static int toFind;
    private static String toFindStr;
    private static String toFindStr2;
    private static String configComment;

    private String[] dupe = cfg.blacklist;
    private static IIcon[] iconIndexes;
    private static int tickcount = 0;
    private static int lastState = 0;
    private static int found = 0;

    private int id;

    Block oreBlock = null;

    private String toFindStr3;


    public ItemOreFinderTool()
    {
        setUnlocalizedName(Reference.MOD_ID + "_" + "buildingKitItem");
        setMaxDamage(MAX_DAMAGE);
        setMaxStackSize(1);
        setTextureName(Reference.MOD_ID + ":meter0");

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

    public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) 
    {
        if ((entity == null) || (world == null)) {
            return;
        }
        if (new Date().getTime() <= lastUpdate.longValue() ) { // Only search for ore blocks occasionally
            return;
        }
        lastUpdate = Long.valueOf(new Date().getTime()) + millisPerUpdate.longValue();

        if(world.isRemote){ //Client side stuff
            found = MAX_DAMAGE - itemstack.getItemDamage();
            if( found < 0 ) { found = 0; }
            
            switch( found ) {
                case 0:
                    this.itemIcon = iconIndexes[0];
                    break;
                case 1:
                case 2:
                case 3:
                    this.itemIcon = iconIndexes[1];
                    break;
                case 4:
                case 5:
                case 6:
                    this.itemIcon = iconIndexes[2];
                    break;
                case 7:
                case 8:
                case 9:
                    this.itemIcon = iconIndexes[3];
                    break;
                default:  // 10 or more found
                    this.itemIcon = iconIndexes[4];
                    break;
            }
            if (found >= (MAX_DAMAGE/2) && cfg.wandSound) {
                world.playSound(entity.posX, entity.posY, entity.posZ,"ic2:tools.Treetap", 0.6F, 0.8F,true); //(Minecraft.getMinecraft().thePlayer, "ic2:tools.Treetap", 1.0F, 1.0F);
            }
        }
        else { //Server side stuff
            if (!itemstack.hasTagCompound()) {  /// Can this be removed?  Don't think this is used right now
                itemstack.setTagCompound(new NBTTagCompound());
            }

            if ( !(entity instanceof EntityPlayer) ) {
                return;
            } 
            ItemStack searchItem = ExtendedPlayer.get((EntityPlayer) entity).inventorybk.getStackInSlot(0);
            if ( searchItem == null ) {
                itemstack.setItemDamage(MAX_DAMAGE);
                return;
            }
            if( inventoryContainsAAD( ((EntityPlayer) entity).inventory ) == null ) {
                itemstack.setItemDamage(MAX_DAMAGE);
                return;
            }

            toFindStr = searchItem.getUnlocalizedName();
            String splitFind[] = toFindStr.split("\\.");
            if (splitFind[1].equals("blockores")) { // Convert background materials to standard stone
                short tMetaID = (short) (Integer.parseInt(splitFind[2]) % 1000);
                toFindStr = splitFind[0] + "." + splitFind[1] + "." + tMetaID;
            }

            double cur_x = entity.posX;
            double cur_y = entity.posY;
            double cur_z = entity.posZ;

            int min_x = (int) cur_x - cfg.xzAreaRadius - 1;
            int min_y = (int) cur_y - cfg.yAreaRadius;
            int min_z = (int) cur_z - cfg.xzAreaRadius;

            int max_x = (int) cur_x + cfg.xzAreaRadius;
            int max_y = (int) cur_y + cfg.yAreaRadius;
            int max_z = (int) cur_z + cfg.xzAreaRadius + 1;
            boolean keepLooking = true;
            found = 0;

            for (int z1 = min_z; (z1 < max_z) && (keepLooking); z1++) {
                for (int x1 = min_x; (x1 < max_x) && (keepLooking); x1++) {
                    for (int y1 = min_y; (y1 < max_y) && (keepLooking); y1++) {
                        Block tBlock = world.getBlock(x1, y1, z1);
                        if (tBlock instanceof GT_Block_Ores_Abstract) {
                            TileEntity tTileEntity = world.getTileEntity(x1,y1,z1);
                            short tMetaID = (short)((GT_TileEntity_Ores) tTileEntity).getMetaData();
                            String name = tBlock.getUnlocalizedName() + "." + (tMetaID % 1000);
                            if (name.equals(toFindStr)) {
                                boolean blacklisted = false;
                                for (String s : dupe) {
                                    //System.out.println("Blacklisted ore " + s + " compared to " + name );
                                    if (s != null && s.equals(name)) {
                                        //System.out.println("Ignore, ore is blacklisted");
                                        // ore is blacklisted, do not report
                                        blacklisted = true;
                                    }
                                }
                                if (!blacklisted ) {
                                    found++;
                                    //System.out.println("Exact match found");
                                    if (found >= MAX_DAMAGE) { 
                                        keepLooking = false;
                                    }
                                    continue;
                                }
                            }
                        }
                        
                        if (tBlock.getUnlocalizedName().equals(toFindStr)) {
                            boolean blacklisted = false;
                            for (String s : dupe) {
                                //System.out.println("Blacklisted ore " + s + " compared to " + name );
                                if (s != null && s.equals(toFindStr)) {
                                    //System.out.println("Ignore, ore is blacklisted");
                                    // ore is blacklisted, do not report
                                    blacklisted = true;
                                }
                            }
                            if (!blacklisted ) {
                                found++;
                                //System.out.println("Exact match found");
                                if (found >= MAX_DAMAGE) { 
                                    keepLooking = false;
                                }
                                continue;
                            }
                        }
                    }
                }
            }
            itemstack.setItemDamage(MAX_DAMAGE - found);
        }

    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) 
    {
        if (itemStack.stackTagCompound != null)
        {

            if (ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0) != null){
                toFindStr2 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0).getDisplayName();
                toFindStr3 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0).getUnlocalizedName();
                String splitFind[] = toFindStr3.split("\\.");
                if (splitFind[1].equals("blockores")) {
                    short tMetaID = (short) (Integer.parseInt(splitFind[2]) % 1000);
                    toFindStr3 = splitFind[0] + "." + splitFind[1] + "." + tMetaID;
                }
            }else{
                toFindStr2 = "Empty";
            }

            list.add("I want to find: " + toFindStr2 );
            list.add(toFindStr3 );
            list.add("Put ore block you want to find in item inventory -");
            list.add("SHIFT+RIGHT CLICK on ground to open inventory");
            list.add("You can only use 1 finder at a time");
            list.add("Search radius X, Z: "+ cfg.xzAreaRadius +" Y: "+cfg.yAreaRadius);

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
            /*
            
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
*/
        }
        return true;
    }
}
