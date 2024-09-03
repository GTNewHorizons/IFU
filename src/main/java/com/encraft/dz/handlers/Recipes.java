package com.encraft.dz.handlers;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;

public class Recipes implements Runnable {

    public void run() {
        GTModHandler.addCraftingRecipe(
                GTModHandler.getModItem("ifu", "ifu_buildingKit", 1, GTValues.W),
                GTModHandler.RecipeBits.BUFFERED | GTModHandler.RecipeBits.NOT_REMOVABLE,
                new Object[] { "XrX", "SGS", "RIR", 'X', OrePrefixes.stick.get(Materials.IronMagnetic), 'S',
                        OrePrefixes.stick.get(Materials.Wood), 'G', OrePrefixes.ring.get(Materials.Gold), 'I',
                        OrePrefixes.stick.get(Materials.Iron), 'R', OrePrefixes.dust.get(Materials.Redstone) });

    }
}
