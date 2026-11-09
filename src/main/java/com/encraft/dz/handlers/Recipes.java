package com.encraft.dz.handlers;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.materials.Materials;
import gregtech.api.util.GTModHandler;

public class Recipes implements Runnable {

    public void run() {
        GTModHandler.addCraftingRecipe(
                GTModHandler.getModItem("ifu", "ifu_buildingKit", 1, GTValues.W),
                GTModHandler.RecipeBits.BUFFERED | GTModHandler.RecipeBits.NOT_REMOVABLE
                        | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS,
                new Object[] { "XrX", "SGS", "RIR", 'X', OrePrefixes.stick.ingredient(Materials.IronMagnetic), 'S',
                        OrePrefixes.stick.ingredient(Materials.Wood), 'G', OrePrefixes.ring.ingredient(Materials.Gold),
                        'I', OrePrefixes.stick.ingredient(Materials.Iron), 'R',
                        OrePrefixes.dust.ingredient(Materials.Redstone) });

    }
}
