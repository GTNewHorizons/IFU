package com.encraft.dz.items;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.handlers.ConfigHandler;
import com.sinthoras.visualprospecting.VisualProspecting_API;
import com.sinthoras.visualprospecting.database.OreVeinPosition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IOreMaterial;
import gregtech.api.util.GTUtility;
import gregtech.common.ores.OreInfo;
import gregtech.common.ores.OreManager;

public class ItemOreFinderTool extends Item {

    private static final int MAX_FOUND = 10;

    private static IIcon[] iconIndexes;

    public ItemOreFinderTool() {
        setUnlocalizedName(DayNMod.MOD_ID + "_" + "buildingKitItem");
        setMaxStackSize(1);
        setTextureName(DayNMod.MOD_ID + ":meter0");
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister p_94581_1_) {
        super.registerIcons(p_94581_1_);
        iconIndexes = new IIcon[5];
        for (int i = 0; i <= 4; i++) {
            iconIndexes[i] = p_94581_1_.registerIcon(DayNMod.MOD_ID + ":meter" + i);
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return switch (meta) {
            case 0 -> iconIndexes[0];
            case 1, 2, 3 -> iconIndexes[1];
            case 4, 5, 6 -> iconIndexes[2];
            case 7, 8, 9 -> iconIndexes[3];
            // 10 or more found
            default -> iconIndexes[4];
        };
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return GTUtility.linearCurve(stack.getItemDamage(), 0, 1d, MAX_FOUND, 0d);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return stack.getItemDamage() > 0;
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

        // Only search for ore blocks once a second
        // Compare against the entity hashCode so that each player updates on different ticks
        if (world.getTotalWorldTime() % 20 != entity.hashCode() % 20) {
            return;
        }

        if (world.isRemote) {
            int foundOres = itemstack.getItemDamage();

            if (foundOres >= MAX_FOUND / 2 && ConfigHandler.wandSound) {
                world.playSound(entity.posX, entity.posY, entity.posZ, "ic2:tools.Treetap", 0.6F, 0.8F, true);
            }
        } else {
            if (!ConfigHandler.aEnableEverywhere && world.provider.dimensionId != 0
                    && world.provider.dimensionId != -1
                    && !world.provider.getDimensionName().equals("Twilight Forest"))
                return;

            if (!(entity instanceof EntityPlayerMP player)) {
                return;
            }

            // The ore stack that's in the wand
            ItemStack searchItem = ExtendedPlayer.get(player).inventorybk.getStackInSlot(0);

            if (searchItem == null) {
                itemstack.setItemDamage(0);
                return;
            }

            if (inventoryContainsAAD(player.inventory) == null) {
                itemstack.setItemDamage(0);
                return;
            }

            for (String ss : ConfigHandler.blacklist) {
                if (ss != null && ss.equals(searchItem.getUnlocalizedName())) {
                    itemstack.setItemDamage(0);
                    return;
                }
            }

            HashSet<String> materials = new HashSet<>();

            for (var oredict : OrePrefixes.detectPrefix(searchItem)) {
                materials.add(oredict.material);
            }

            int cur_x = MathHelper.floor_double(entity.posX);
            int cur_y = MathHelper.floor_double(entity.posY);
            int cur_z = MathHelper.floor_double(entity.posZ);

            int min_x = cur_x - ConfigHandler.xzAreaRadius - 1;
            int min_y = cur_y - ConfigHandler.yAreaRadius;
            int min_z = cur_z - ConfigHandler.xzAreaRadius;

            int max_x = cur_x + ConfigHandler.xzAreaRadius;
            int max_y = cur_y + ConfigHandler.yAreaRadius;
            int max_z = cur_z + ConfigHandler.xzAreaRadius + 1;

            int found = 0;

            IOreMaterial oreMaterial = null;

            outer: for (int z1 = min_z; z1 < max_z; z1++) {
                for (int x1 = min_x; x1 < max_x; x1++) {
                    for (int y1 = min_y; y1 < max_y; y1++) {
                        try (OreInfo<IOreMaterial> info = OreManager.getOreInfo(world, x1, y1, z1)) {
                            if (info != null && info.isNatural && !info.isSmall) {
                                if (materials.contains(info.material.getInternalName())) {
                                    found++;

                                    oreMaterial = info.material;

                                    if (found >= MAX_FOUND) {
                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (oreMaterial != null) {
                prospectForVeins(world, player, cur_x, cur_z, oreMaterial);
            }

            itemstack.setItemDamage(found);
        }
    }

    private void prospectForVeins(World world, EntityPlayerMP player, int x1, int z1, IOreMaterial ore) {
        List<OreVeinPosition> veins = VisualProspecting_API.LogicalServer
                .prospectOreVeinsWithinRadius(world.provider.dimensionId, x1, z1, 48).stream()
                .filter(it -> it.veinType.containsOre(ore)).collect(Collectors.toList());

        VisualProspecting_API.LogicalServer.sendProspectionResultsToClient(player, veins, Collections.emptyList());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean par4) {
        ItemStack filterStack = ExtendedPlayer.get(player).inventorybk.getStackInSlot(0);

        if (filterStack != null) {
            list.add(GTUtility.translate("IFU.FindTarget", filterStack.getDisplayName()));
        }

        list.add(GTUtility.translate("IFU.description1"));
        list.add(GTUtility.translate("IFU.description2"));
        list.add(GTUtility.translate("IFU.description3"));
        list.add(GTUtility.translate("IFU.SearchRadiusText", ConfigHandler.xzAreaRadius, ConfigHandler.yAreaRadius));

        if (!ConfigHandler.aEnableEverywhere) {
            list.add(GTUtility.translate("IFU.disableAtSomewhereWarning1"));
            list.add(GTUtility.translate("IFU.disableAtSomewhereWarning2"));
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
