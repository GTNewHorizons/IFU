package com.encraft.dz.handlers;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_ModHandler;

public class Recipes implements Runnable {

    public void run() {
        GT_ModHandler.addCraftingRecipe(
                GT_ModHandler.getModItem("ifu", "ifu_buildingKit", 1, GT_Values.W),
                GT_ModHandler.RecipeBits.BUFFERED | GT_ModHandler.RecipeBits.NOT_REMOVABLE,
                new Object[] { "XrX", "SGS", "RIR", 'X', OrePrefixes.stick.get(Materials.IronMagnetic), 'S',
                        OrePrefixes.stick.get(Materials.Wood), 'G', OrePrefixes.ring.get(Materials.Gold), 'I',
                        OrePrefixes.stick.get(Materials.Iron), 'R', OrePrefixes.dust.get(Materials.Redstone) });

    }
}
