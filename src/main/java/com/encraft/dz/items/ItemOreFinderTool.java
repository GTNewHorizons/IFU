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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.handlers.ConfigHandler;
import com.github.bsideup.jabel.Desugar;
import com.sinthoras.visualprospecting.VisualProspecting_API;
import com.sinthoras.visualprospecting.database.OreVeinPosition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IOreMaterial;
import gregtech.api.util.GTUtility;
import gregtech.common.ores.OreInfo;
import gregtech.common.ores.OreManager;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class ItemOreFinderTool extends Item {

    private static final int MAX_FOUND = 10;
    private static final int ANY_META = -1;
    private static final int NO_MATCH = -2;

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

    // Return the first Ore Finder wand in the player inventory, or null if there are none.
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

            // We only scan for Ore once if the Ore Finder is in player inventory
            ItemStack primaryWand = inventoryContainsAAD(player.inventory);
            if (itemstack != primaryWand) {
                itemstack.setItemDamage(primaryWand == null ? 0 : primaryWand.getItemDamage());
                return;
            }

            // The ore stack that's in the wand
            ItemStack searchItem = ExtendedPlayer.get(player).inventorybk.getStackInSlot(0);

            if (searchItem == null) {
                itemstack.setItemDamage(0);
                return;
            }

            for (String ss : ConfigHandler.blacklist) {
                if (ss != null && ss.equals(searchItem.getUnlocalizedName())) {
                    itemstack.setItemDamage(0);
                    return;
                }
            }

            // If the filter item is in allow-listed entry, the wand looks for that exact block.
            Block allowBlock = null;
            int allowMeta = NO_MATCH;

            if (searchItem.getItem() instanceof ItemBlock filterItemBlock) {
                Block filterBlock = Block.getBlockFromItem(filterItemBlock);
                int rule = allowlistMatch(filterBlock, filterItemBlock.getMetadata(searchItem.getItemDamage()));
                if (rule != NO_MATCH) {
                    allowBlock = filterBlock;
                    allowMeta = rule;
                }
            }

            ReferenceOpenHashSet<IOreMaterial> materials = new ReferenceOpenHashSet<>();

            if (allowBlock == null) {
                IOreMaterial searchMaterial = OreManager.getMaterial(searchItem);
                if (searchMaterial != null) {
                    materials.add(searchMaterial);
                } else {
                    for (var oredict : OrePrefixes.detectPrefix(searchItem)) {
                        IOreMaterial mat = IOreMaterial.findMaterial(oredict.material);
                        if (mat != null) {
                            materials.add(mat);
                        }
                    }
                }
            }

            int cur_x = MathHelper.floor_double(entity.posX);
            int cur_y = MathHelper.floor_double(entity.posY);
            int cur_z = MathHelper.floor_double(entity.posZ);

            AreaScan scan = scanArea(world, cur_x, cur_y, cur_z, allowBlock, allowMeta, materials);

            if (scan.oreMaterial != null) {
                prospectForVeins(world, player, cur_x, cur_z, scan.oreMaterial);
            }

            itemstack.setItemDamage(scan.found);
        }
    }

    /**
     * Scans the area and reports how many matching blocks were found (capped at {@link #MAX_FOUND}). In ore mode the
     * matched material is reported back so the caller can run vein prospecting; allow-listed block searches leave it
     * {@code null}.
     */
    private static AreaScan scanArea(World world, int centerX, int centerY, int centerZ, Block allowBlock,
            int allowMeta, ReferenceOpenHashSet<IOreMaterial> materials) {

        int minX = centerX - ConfigHandler.xzAreaRadius - 1;
        int maxX = centerX + ConfigHandler.xzAreaRadius;
        int minY = centerY - ConfigHandler.yAreaRadius;
        int maxY = centerY + ConfigHandler.yAreaRadius;
        int minZ = centerZ - ConfigHandler.xzAreaRadius;
        int maxZ = centerZ + ConfigHandler.xzAreaRadius + 1;

        int found = 0;
        IOreMaterial oreMaterial = null;

        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    if (allowBlock != null) {
                        // Allow-listed block search: match the exact block, and the metadata unless wildcard.
                        if (world.getBlock(x, y, z) == allowBlock
                                && (allowMeta == ANY_META || world.getBlockMetadata(x, y, z) == allowMeta)) {
                            found++;
                        }
                    } else {
                        IOreMaterial mat = matchedOre(world, x, y, z, materials);
                        if (mat != null) {
                            found++;
                            oreMaterial = mat;
                        }
                    }

                    if (found >= MAX_FOUND) {
                        return new AreaScan(found, oreMaterial);
                    }
                }
            }
        }

        return new AreaScan(found, oreMaterial);
    }

    private static IOreMaterial matchedOre(World world, int x, int y, int z,
            ReferenceOpenHashSet<IOreMaterial> materials) {
        try (OreInfo<IOreMaterial> info = OreManager.getOreInfo(world, x, y, z)) {
            if (info != null && info.isNatural && !info.isSmall && materials.contains(info.material)) {
                return info.material;
            }
        }
        return null;
    }

    @Desugar
    private record AreaScan(int found, IOreMaterial oreMaterial) {

    }

    /**
     * Checks whether the given block is permitted by {@link ConfigHandler#allowlist}. Entries use the block registry
     * name with an optional metadata suffix: {@code "modid:block"} matches any metadata, {@code "modid:block:2"}
     * matches only metadata 2.
     *
     * @return the metadata the wand should look for ({@link #ANY_META} for wildcard entries), or {@link #NO_MATCH} if
     *         the block isn't allow-listed.
     */
    private static int allowlistMatch(Block block, int meta) {
        if (block == null) {
            return NO_MATCH;
        }

        for (String entry : ConfigHandler.allowlist) {
            if (entry == null) {
                continue;
            }

            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            String name = entry;
            int entryMeta = ANY_META;

            // A registry name already contains one ':' (modid:block); a second ':' introduces a metadata suffix.
            int lastColon = entry.lastIndexOf(':');
            if (lastColon > 0 && lastColon != entry.indexOf(':')) {
                try {
                    entryMeta = Integer.parseInt(entry.substring(lastColon + 1));
                    name = entry.substring(0, lastColon);
                } catch (NumberFormatException e) {
                    // Trailing token wasn't a number, so treat the whole entry as the registry name.
                    entryMeta = ANY_META;
                    name = entry;
                }
            }

            if (Block.getBlockFromName(name) != block) {
                continue;
            }

            if (entryMeta == ANY_META || entryMeta == meta) {
                return entryMeta;
            }
        }

        return NO_MATCH;
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
