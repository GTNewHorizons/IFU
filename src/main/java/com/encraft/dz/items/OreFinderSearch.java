package com.encraft.dz.items;

import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.encraft.dz.handlers.ConfigHandler;
import com.github.bsideup.jabel.Desugar;

import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IOreMaterial;
import gregtech.api.util.GTUtility;
import gregtech.common.ores.OreInfo;
import gregtech.common.ores.OreManager;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public final class OreFinderSearch {

    private OreFinderSearch() {}

    /** Blocks counted before the scan stops early; also the scale for the wand's damage bar. */
    public static final int MAX_FOUND = 10;

    private static final int ANY_META = -1;
    private static final int NO_MATCH = -2;

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
    static AreaScan scanArea(World world, int centerX, int centerY, int centerZ, MatchTarget target) {
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
    record AreaScan(int found, IOreMaterial oreMaterial) {

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
}
