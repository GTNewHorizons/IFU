package com.encraft.dz.items;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.encraft.dz.IFU;
import com.encraft.dz.OreFinderPlayerData;
import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.items.OreFinderSearch.MatchTarget;
import com.sinthoras.visualprospecting.VisualProspecting_API;
import com.sinthoras.visualprospecting.database.OreVeinPosition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.IOreMaterial;
import gregtech.api.util.GTUtility;
import gregtech.common.ores.OreInfo;
import gregtech.common.ores.OreManager;

public class ItemOreFinderTool extends Item {

    private static IIcon[] iconIndexes;

    public ItemOreFinderTool() {
        setUnlocalizedName(IFU.MOD_ID + "_" + "buildingKitItem");
        setMaxStackSize(1);
        setTextureName(IFU.MOD_ID + ":meter0");
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister p_94581_1_) {
        super.registerIcons(p_94581_1_);
        iconIndexes = new IIcon[5];
        for (int i = 0; i <= 4; i++) {
            iconIndexes[i] = p_94581_1_.registerIcon(IFU.MOD_ID + ":meter" + i);
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
        return GTUtility.linearCurve(stack.getItemDamage(), 0, 1d, OreFinderSearch.MAX_FOUND, 0d);
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

    // Return the first Ore Finder wand in the player inventory, or null if there are none.
    public static ItemStack findFirstInInventory(InventoryPlayer inventory) {
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

            if (foundOres >= OreFinderSearch.MAX_FOUND / 2 && ConfigHandler.wandSound) {
                world.playSound(entity.posX, entity.posY, entity.posZ, "ic2:tools.Treetap", 0.6F, 0.8F, true);
            }
        } else {
            if (!ConfigHandler.enableEverywhere && world.provider.dimensionId != 0
                    && world.provider.dimensionId != -1
                    && !world.provider.getDimensionName().equals("Twilight Forest"))
                return;

            if (!(entity instanceof EntityPlayerMP player)) {
                return;
            }

            // Only scan for Ore if the Ore Finder is in player inventory, and only once
            ItemStack primaryWand = findFirstInInventory(player.inventory);
            if (itemstack != primaryWand) {
                itemstack.setItemDamage(primaryWand == null ? 0 : primaryWand.getItemDamage());
                return;
            }

            OreFinderPlayerData props = OreFinderPlayerData.get(player);
            ItemStack searchItem = props.filterInventory.getStackInSlot(0);
            MatchTarget target = OreFinderSearch.resolveMatch(searchItem);

            if (!target.canSearch()) {
                itemstack.setItemDamage(0);
                props.clearLastScan();
                return;
            }

            int cur_x = MathHelper.floor_double(entity.posX);
            int cur_y = MathHelper.floor_double(entity.posY);
            int cur_z = MathHelper.floor_double(entity.posZ);

            if (props.isSameScan(cur_x, cur_y, cur_z, searchItem)) {
                return;
            }

            var scan = OreFinderSearch.scanArea(world, cur_x, cur_y, cur_z, target);

            if (scan.oreMaterial() != null) {
                prospectForVeins(world, player, cur_x, cur_z, scan.oreMaterial());
            }

            itemstack.setItemDamage(scan.found());
            props.rememberScan(cur_x, cur_y, cur_z, searchItem);
        }
    }

    private void prospectForVeins(World world, EntityPlayerMP player, int x1, int z1, IOreMaterial ore) {
        List<OreVeinPosition> veins = VisualProspecting_API.LogicalServer
                .prospectOreVeinsWithinRadius(world.provider.dimensionId, x1, z1, ConfigHandler.xzAreaRadius + 1)
                .stream().filter(it -> it.veinType.containsOre(ore)).collect(Collectors.toList());

        VisualProspecting_API.LogicalServer.sendProspectionResultsToClient(player, veins, Collections.emptyList());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean par4) {
        ItemStack filterStack = OreFinderPlayerData.get(player).filterInventory.getStackInSlot(0);

        if (filterStack != null) {
            list.add(GTUtility.translate("IFU.FindTarget", filterStack.getDisplayName()));
        }

        list.add(GTUtility.translate("IFU.description1"));
        list.add(GTUtility.translate("IFU.description2"));
        list.add(GTUtility.translate("IFU.description3"));
        list.add(GTUtility.translate("IFU.SearchRadiusText", ConfigHandler.xzAreaRadius, ConfigHandler.yAreaRadius));

        if (!ConfigHandler.enableEverywhere) {
            list.add(GTUtility.translate("IFU.disableAtSomewhereWarning1"));
            list.add(GTUtility.translate("IFU.disableAtSomewhereWarning2"));
        }
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {

        if (entityPlayer.isSneaking()) {
            if (!world.isRemote) {
                entityPlayer.openGui(
                        IFU.instance,
                        IFU.GUI_ORE_FINDER,
                        entityPlayer.worldObj,
                        (int) entityPlayer.posX,
                        (int) entityPlayer.posY,
                        (int) entityPlayer.posZ);
            }
        } else if (ConfigHandler.debugBlockInfo && !world.isRemote) {
            printBlockDebug(world, entityPlayer, x, y, z);
            printFilterDebug(entityPlayer);
        }
        return true;
    }

    private static void printBlockDebug(World world, EntityPlayer player, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        GTUtility.sendChatComp(
                player,
                new ChatComponentText(
                        String.format(
                                "[OreFinder] Block: %s meta=%d",
                                Block.blockRegistry.getNameForObject(block),
                                meta)));

        try (OreInfo<IOreMaterial> info = OreManager.getOreInfo(world, x, y, z)) {
            if (info != null) {
                GTUtility.sendChatComp(
                        player,
                        new ChatComponentText(
                                String.format(
                                        "[OreFinder] Ore material: %s (stone=%s, natural=%s, small=%s)",
                                        info.material.getInternalName(),
                                        info.stoneType,
                                        info.isNatural,
                                        info.isSmall)));
            } else {
                GTUtility.sendChatComp(player, new ChatComponentText("[OreFinder] Not a recognised ore block"));
            }
        }
    }

    private static void printFilterDebug(EntityPlayer player) {
        ItemStack filter = OreFinderPlayerData.get(player).filterInventory.getStackInSlot(0);
        if (filter == null) {
            GTUtility.sendChatComp(player, new ChatComponentText("[OreFinder] Wand slot is empty"));
            return;
        }
        MatchTarget target = OreFinderSearch.resolveMatch(filter);
        GTUtility.sendChatComp(
                player,
                new ChatComponentText(
                        String.format(
                                "[OreFinder] Wand slot '%s' resolves to: %s",
                                filter.getDisplayName(),
                                target.getInternalName())));
    }
}
