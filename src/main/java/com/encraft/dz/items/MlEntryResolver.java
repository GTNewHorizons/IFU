package com.encraft.dz.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.bsideup.jabel.Desugar;
import com.ruling_0.materiallib.api.Material;
import com.ruling_0.materiallib.api.ShapeBlock;
import com.ruling_0.materiallib.api.StackResolver;

/// Expands the `ml:<material>:<shapeToken>` entries of the Ore Finder allow-list and block-list into the blocks and
/// metadata they name. A MaterialLib block carries the material's global index as its metadata, and that index is
/// assigned per session, so no fixed `modid:block:meta` entry can name one.
///
/// Expansions are cached per entry until [#invalidate].
public final class MlEntryResolver {

    /// Marks a config entry as a MaterialLib material and shape pair rather than a block name.
    public static final String PREFIX = "ml:";

    private static final Logger LOG = LogManager.getLogger("IFU");

    private static final Map<String, List<BlockMeta>> EXPANSIONS = new ConcurrentHashMap<>();

    private MlEntryResolver() {}

    /// One block of a MaterialLib shape together with the metadata a material takes on it.
    @Desugar
    public record BlockMeta(Block block, int meta) {

    }

    /// The blocks and metadata `entry` names. A shape token naming one block shape yields a single pair. A family
    /// token such as `ore` yields one pair per `ore_*` variant. Empty when the entry names no block shape that the
    /// material generates.
    public static List<BlockMeta> expand(String entry) {
        return EXPANSIONS.computeIfAbsent(entry, MlEntryResolver::resolve);
    }

    /// Drops the cached expansions.
    public static void invalidate() {
        EXPANSIONS.clear();
    }

    /// Expands every `ml:` entry of the given lists. Call once MaterialLib has resolved its shapes.
    public static void warmUp(String[]... lists) {
        int resolved = 0;
        int invalid = 0;

        for (String[] list : lists) {
            for (String entry : list) {
                if (entry == null) {
                    continue;
                }

                String trimmed = entry.trim();
                if (!trimmed.startsWith(PREFIX)) {
                    continue;
                }

                if (expand(trimmed).isEmpty()) {
                    invalid++;
                } else {
                    resolved++;
                }
            }
        }

        if (resolved + invalid > 0) {
            LOG.info("IFU: resolved {} MaterialLib list entries ({} invalid)", resolved, invalid);
        }
    }

    private static List<BlockMeta> resolve(String entry) {
        String[] parts = entry.split(":");
        if (parts.length != 3) {
            LOG.error("Malformed MaterialLib list entry \"{}\", expected ml:<material>:<shape>", entry);
            return Collections.emptyList();
        }

        Material material = StackResolver.getMaterial(parts[1]);
        if (material == null) {
            return Collections.emptyList();
        }

        List<ShapeBlock> shapes = StackResolver.getBlockShapes(parts[2]);
        if (shapes.isEmpty()) {
            if (StackResolver.getShape(parts[2]) != null) {
                LOG.error("MaterialLib list entry \"{}\" names an item shape, not a block shape", entry);
            }
            return Collections.emptyList();
        }

        List<BlockMeta> pairs = new ArrayList<>();
        for (ShapeBlock shape : shapes) {
            for (Material served : shape.getServedMaterials()) {
                if (served == material) {
                    pairs.add(new BlockMeta(shape, material.getIndex()));
                    break;
                }
            }
        }

        if (pairs.isEmpty()) {
            LOG.error("MaterialLib list entry \"{}\" names a shape that material does not generate", entry);
        }
        return pairs;
    }
}
