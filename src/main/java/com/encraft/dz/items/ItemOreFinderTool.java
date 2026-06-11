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
import net.minecraft.util.ChatComponentText;
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

            // Only scan for Ore if the Ore Finder is in player inventory, and only once
            ItemStack primaryWand = inventoryContainsAAD(player.inventory);
            if (itemstack != primaryWand) {
                itemstack.setItemDamage(primaryWand == null ? 0 : primaryWand.getItemDamage());
                return;
            }

            ItemStack searchItem = ExtendedPlayer.get(player).inventorybk.getStackInSlot(0);
            MatchTarget target = resolveMatch(searchItem);

            if (!target.canSearch()) {
                itemstack.setItemDamage(0);
                return;
            }

            int cur_x = MathHelper.floor_double(entity.posX);
            int cur_y = MathHelper.floor_double(entity.posY);
            int cur_z = MathHelper.floor_double(entity.posZ);

            AreaScan scan = scanArea(world, cur_x, cur_y, cur_z, target);

            if (scan.oreMaterial != null) {
                prospectForVeins(world, player, cur_x, cur_z, scan.oreMaterial);
            }

            itemstack.setItemDamage(scan.found);
        }
    }

    // Resolves what the wand will actually look for
    public static MatchTarget resolveMatch(ItemStack searchItem) {
        if (searchItem == null) {
            return MatchTarget.NONE;
        }

        if (searchItem.getItem() instanceof ItemBlock filterItemBlock) {
            Block filterBlock = Block.getBlockFromItem(filterItemBlock);
            int meta = filterItemBlock.getMetadata(searchItem.getItemDamage());

            if (listMatch(ConfigHandler.blocklist, filterBlock, meta) != NO_MATCH) {
                return MatchTarget.BLOCKLISTED;
            }

            int rule = listMatch(ConfigHandler.allowlist, filterBlock, meta);
            if (rule != NO_MATCH) {
                return MatchTarget.block(filterBlock, rule);
            }
        }

        // Resolve the ore material(s) the filter stands for, dropping any the material block-list forbids.
        ReferenceOpenHashSet<IOreMaterial> materials = new ReferenceOpenHashSet<>();
        boolean resolved = false;
        IOreMaterial searchMaterial = OreManager.getMaterial(searchItem);
        if (searchMaterial != null) {
            resolved = true;
            if (!isMaterialBlocked(searchMaterial)) {
                materials.add(searchMaterial);
            }
        } else {
            for (var oredict : OrePrefixes.detectPrefix(searchItem)) {
                IOreMaterial mat = IOreMaterial.findMaterial(oredict.material);
                if (mat != null) {
                    resolved = true;
                    if (!isMaterialBlocked(mat)) {
                        materials.add(mat);
                    }
                }
            }
        }

        if (!materials.isEmpty()) {
            return MatchTarget.materials(materials);
        }
        return resolved ? MatchTarget.BLOCKLISTED : MatchTarget.NONE;
    }

    private static boolean isMaterialBlocked(IOreMaterial material) {
        String internalName = material.getInternalName();

        for (String entry : ConfigHandler.materialBlocklist) {
            if (entry != null && entry.trim().equalsIgnoreCase(internalName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Scans the area and reports how many matching blocks were found (capped at {@link #MAX_FOUND}).
     */
    private static AreaScan scanArea(World world, int centerX, int centerY, int centerZ, MatchTarget target) {
        Block allowBlock = target.block;
        int allowMeta = target.meta;
        ReferenceOpenHashSet<IOreMaterial> materials = target.materials;

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
     * The outcome of {@link #resolveMatch}: either an allow-listed {@link #block} (optionally with metadata), a set of
     * ore {@link #materials}, or neither.
     */
    public static final class MatchTarget {

        private enum Kind {
            NONE,
            BLOCKLISTED,
            BLOCK,
            MATERIALS
        }

        private static final MatchTarget NONE = new MatchTarget(Kind.NONE, null, ANY_META, null);
        private static final MatchTarget BLOCKLISTED = new MatchTarget(Kind.BLOCKLISTED, null, ANY_META, null);

        private final Kind kind;
        private final Block block;
        private final int meta;
        private final ReferenceOpenHashSet<IOreMaterial> materials;

        private MatchTarget(Kind kind, Block block, int meta, ReferenceOpenHashSet<IOreMaterial> materials) {
            this.kind = kind;
            this.block = block;
            this.meta = meta;
            this.materials = materials;
        }

        private static MatchTarget block(Block block, int meta) {
            return new MatchTarget(Kind.BLOCK, block, meta, null);
        }

        private static MatchTarget materials(ReferenceOpenHashSet<IOreMaterial> materials) {
            return new MatchTarget(Kind.MATERIALS, null, ANY_META, materials);
        }

        public boolean canSearch() {
            return kind == Kind.BLOCK || kind == Kind.MATERIALS;
        }

        public boolean isBlocklisted() {
            return kind == Kind.BLOCKLISTED;
        }

        public String describe() {
            return switch (kind) {
                case BLOCK -> new ItemStack(block, 1, meta == ANY_META ? 0 : meta).getDisplayName();
                case MATERIALS -> materials.stream().map(IOreMaterial::getLocalizedName)
                        .collect(Collectors.joining(", "));
                default -> GTUtility.translate("IFU.SearchNoMatch");
            };
        }
    }

    /**
     * Checks whether {@code block}/{@code meta} appears in the given list of config, shared by the allow-list and
     * block-list.
     *
     * @return the matched metadata ({@link #ANY_META} for a wildcard entry), or {@link #NO_MATCH} if no entry matches.
     */
    private static int listMatch(String[] entries, Block block, int meta) {
        if (block == null) {
            return NO_MATCH;
        }

        for (String entry : entries) {
            if (entry == null) {
                continue;
            }

            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            String name = entry;
            int entryMeta = ANY_META;

            // An item already contains one ':' (modid:block); a second ':' introduces a metadata suffix.
            int lastColon = entry.lastIndexOf(':');
            if (lastColon > 0 && lastColon != entry.indexOf(':')) {
                try {
                    entryMeta = Integer.parseInt(entry.substring(lastColon + 1));
                    name = entry.substring(0, lastColon);
                } catch (NumberFormatException e) {
                    // Trailing token wasn't a number, so treat the whole entry as the name.
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
        } else if (ConfigHandler.debugBlockInfo && !world.isRemote) {
            printBlockDebug(world, entityPlayer, x, y, z);
        }
        return true;
    }

    /**
     * Diagnostic helper (enabled by {@link ConfigHandler#debugBlockInfo}): right-clicking a block reports to player its
     * name and metadata. Handy for understanding why an ore does or doesn't match and for finding what to put in
     * Allow/Block lists.
     */
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
}
