package com.encraft.dz.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.lib.Tags;
import com.sinthoras.visualprospecting.VisualProspecting_API;
import com.sinthoras.visualprospecting.database.OreVeinPosition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_OreDictUnificator;

public class ItemOreFinderTool extends Item {

    private static Long lastUpdate = Long.valueOf(0L);
    private static Long millisPerUpdate = Long.valueOf(250L);
    private static int MAX_DAMAGE = 10;
    private static IIcon[] iconIndexes;
    private static int found = 0;

    public ItemOreFinderTool() {
        setUnlocalizedName(Tags.MOD_ID + "_" + "buildingKitItem");
        setMaxStackSize(1);
        setTextureName(Tags.MOD_ID + ":meter0");
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister p_94581_1_) {
        super.registerIcons(p_94581_1_);
        iconIndexes = new IIcon[5];
        for (int i = 0; i <= 4; i++) {
            iconIndexes[i] = p_94581_1_.registerIcon(Tags.MOD_ID + ":meter" + i);
        }
    }

    public boolean canItemEditBlocks() {
        return false;
    }

    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
        itemStack.stackTagCompound = new NBTTagCompound();
    }

    // TODO -----------------------------------------------------------------------------------------------
    public static ItemStack inventoryContainsAAD(InventoryPlayer inventory) {
        ItemStack itemstack = null;
        for (ItemStack s : inventory.mainInventory) {
            if (s != null && s.getItem() instanceof ItemOreFinderTool) {
                itemstack = s;
                break;
            }
        }
        return itemstack;
    }

    public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
        if ((entity == null) || (world == null)) {
            return;
        }
        if (new Date().getTime() <= lastUpdate.longValue()) { // Only search for ore blocks occasionally
            return;
        }
        lastUpdate = Long.valueOf(new Date().getTime()) + millisPerUpdate.longValue();

        if (world.isRemote) { // Client side stuff
            found = MAX_DAMAGE - itemstack.getItemDamage();
            if (found < 0) {
                found = 0;
            }

            switch (found) {
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
                default: // 10 or more found
                    this.itemIcon = iconIndexes[4];
                    break;
            }
            if (found >= (MAX_DAMAGE / 2) && ConfigHandler.wandSound) {
                world.playSound(entity.posX, entity.posY, entity.posZ, "ic2:tools.Treetap", 0.6F, 0.8F, true); // (Minecraft.getMinecraft().thePlayer,
                                                                                                               // "ic2:tools.Treetap",
                                                                                                               // 1.0F,
                                                                                                               // 1.0F);
            }
        } else { // Server side stuff
            if (!ConfigHandler.aEnableEverywhere && world.provider.dimensionId != 0
                    && world.provider.dimensionId != -1
                    && !world.provider.getDimensionName().equals("Twilight Forest"))
                return;

            if (!(entity instanceof EntityPlayer)) {
                return;
            }

            ItemStack searchItem = ExtendedPlayer.get((EntityPlayer) entity).inventorybk.getStackInSlot(0);

            if (searchItem == null) {
                itemstack.setItemDamage(MAX_DAMAGE);
                return;
            }

            if (inventoryContainsAAD(((EntityPlayer) entity).inventory) == null) {
                itemstack.setItemDamage(MAX_DAMAGE);
                return;
            }

            for (String ss : ConfigHandler.blacklist) {
                if (ss != null && ss.equals(searchItem.getUnlocalizedName())) {
                    itemstack.setItemDamage(MAX_DAMAGE);
                    return;
                }
            }

            ItemData data = GT_OreDictUnificator.getAssociation(searchItem);

            boolean vanilla = (data == null || data.mMaterial == null || data.mMaterial.mMaterial == null);

            int id = vanilla ? Item.getIdFromItem(searchItem.getItem()) : 0;

            double cur_x = entity.posX;
            double cur_y = entity.posY;
            double cur_z = entity.posZ;

            int min_x = (int) cur_x - ConfigHandler.xzAreaRadius - 1;
            int min_y = (int) cur_y - ConfigHandler.yAreaRadius;
            int min_z = (int) cur_z - ConfigHandler.xzAreaRadius;

            int max_x = (int) cur_x + ConfigHandler.xzAreaRadius;
            int max_y = (int) cur_y + ConfigHandler.yAreaRadius;
            int max_z = (int) cur_z + ConfigHandler.xzAreaRadius + 1;
            boolean keepLooking = true;
            found = 0;

            for (int z1 = min_z; (z1 < max_z) && (keepLooking); z1++) {
                for (int x1 = min_x; (x1 < max_x) && (keepLooking); x1++) {
                    for (int y1 = min_y; (y1 < max_y) && (keepLooking); y1++) {

                        Block tBlock = world.getBlock(x1, y1, z1);
                        int meta = tBlock.getDamageValue(world, x1, y1, z1);
                        ItemStack inWorld = new ItemStack(tBlock, 1, meta);
                        if (!vanilla) {
                            ItemData dataInWorld = GT_OreDictUnificator.getAssociation(inWorld);
                            if (dataInWorld == null || dataInWorld.mPrefix == null
                                    || dataInWorld.mMaterial == null
                                    || dataInWorld.mMaterial.mMaterial == null)
                                continue;

                            List<OrePrefixes> oreTypes = Arrays.asList(
                                    OrePrefixes.ore,
                                    OrePrefixes.oreBasalt,
                                    OrePrefixes.oreBlackgranite,
                                    OrePrefixes.oreEnd,
                                    OrePrefixes.oreEndstone,
                                    OrePrefixes.oreMarble,
                                    OrePrefixes.oreNether,
                                    OrePrefixes.oreNetherrack,
                                    OrePrefixes.oreRedgranite,
                                    OrePrefixes.oreRich,
                                    OrePrefixes.oreDense,
                                    // uncomment this for small ores and/or blocks
                                    // OrePrefixes.oreSmall,
                                    // OrePrefixes.block
                                    // OrePrefixes.block_
                                    OrePrefixes.oreGem,
                                    OrePrefixes.denseore);

                            if (dataInWorld.mMaterial.mMaterial == data.mMaterial.mMaterial
                                    && oreTypes.contains(dataInWorld.mPrefix)) {
                                found++;
                                keepLooking = shouldKeepLooking();
                                checkGtOreFound(world, entity, vanilla, keepLooking, z1, x1, dataInWorld);
                            }
                        } else {
                            if (Item.getIdFromItem(inWorld.getItem()) == id
                                    && inWorld.getItemDamage() == searchItem.getItemDamage()) {
                                found++;
                                keepLooking = shouldKeepLooking();
                            }
                        }

                    }
                }
            }
            itemstack.setItemDamage(MAX_DAMAGE - found);
        }
    }

    private List<OreVeinPosition> listVeinsInProximityContaining(short foundMaterialMetaId, int blocX, int blockZ,
            World world) {
        return VisualProspecting_API.LogicalServer
                .prospectOreVeinsWithinRadius(world.provider.dimensionId, blocX, blockZ, 48).stream()
                .filter(it -> it.veinType.containsOre(foundMaterialMetaId)).collect(Collectors.toList());
    }

    private void checkGtOreFound(World world, Entity entity, boolean vanilla, boolean keepLooking, int z1, int x1,
            ItemData dataInWorld) {
        if (!vanilla && !world.isRemote && !keepLooking && entity instanceof EntityPlayer) {
            short foundMaterialMetaId = (short) dataInWorld.mMaterial.mMaterial.mMetaItemSubID;
            List<OreVeinPosition> discoveredOreVeins = this
                    .listVeinsInProximityContaining(foundMaterialMetaId, x1, z1, world);
            VisualProspecting_API.LogicalServer.sendProspectionResultsToClient(
                    (EntityPlayerMP) entity,
                    discoveredOreVeins,
                    Collections.emptyList());
        }
    }

    private boolean shouldKeepLooking() {
        return found < MAX_DAMAGE;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        if (itemStack.stackTagCompound != null) {
            String toFindStr2 = "", toFindStr3 = "";

            if (ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0) != null) {
                toFindStr2 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0)
                        .getDisplayName();
                toFindStr3 = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).inventorybk.getStackInSlot(0)
                        .getUnlocalizedName();
                String splitFind[] = toFindStr3.split("\\.");
                if (splitFind[1].equals("blockores")) {
                    short tMetaID = (short) (Integer.parseInt(splitFind[2]) % 1000);
                    toFindStr3 = splitFind[0] + "." + splitFind[1] + "." + tMetaID;
                }
            }
            if (!toFindStr2.isEmpty()) list.add("I want to find: " + toFindStr2);
            if (!toFindStr3.isEmpty()) list.add(toFindStr3);
            list.add("Put ore block you want to find in item inventory -");
            list.add("SHIFT+RIGHT CLICK on ground to open inventory");
            list.add("You can only use 1 finder at a time");
            list.add("Search radius X, Z: " + ConfigHandler.xzAreaRadius + " Y: " + ConfigHandler.yAreaRadius);

            if (!ConfigHandler.aEnableEverywhere) {
                list.add("** DOES NOT WORK IN SPACE! **");
                list.add("Overworld, Nether, Twilight Forest only");
            }
        }
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityPlayer, World world, int x, int y, int z,
            int sciankaKliknieta, float objetosc_x, float pbjetosc_y, float objetosc_z) {

        if (entityPlayer.isSneaking()) {
            if (!world.isRemote) {

                entityPlayer.openGui(
                        DayNMod.instance,
                        DayNMod.GUI_CUSTOM_INV1,
                        entityPlayer.worldObj,
                        (int) entityPlayer.posX,
                        (int) entityPlayer.posY,
                        (int) entityPlayer.posZ);
            }
        } else {
            /*
             * // Only for tests! world.playSoundAtEntity(entityPlayer, "ic2:tools.Treetap", 1.0F, 1.0F); Block tBlock =
             * world.getBlock(x, y, z); short tMetaID = (short)world.getBlockMetadata(x, y, z); TileEntity tTileEntity =
             * world.getTileEntity(x, y, z); if(tTileEntity !=null){ tMetaID = (short)((GT_TileEntity_Ores)
             * tTileEntity).getMetaData(); ArrayList<ItemStack> drops = tBlock.getDrops(world, x, y, z, tMetaID, 0); //
             * tBlock.getDrops(world, x, y, z, metadata, fortune) String name = tBlock.getDrops(world, x, y, z,tMetaID ,
             * 0) + "." + tMetaID; //System.out.println(drops.get(0).getUnlocalizedName()); for (int i = 0;
             * i<drops.size();i++) System.out.println("drops :" + (i+1) + " " + drops.get(i).getDisplayName());
             * System.out.println(" -----------------   " ); }
             */
        }
        return true;
    }
}
