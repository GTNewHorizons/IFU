package com.encraft.dz.handlers;

import com.ruling_0.materiallib.api.MaterialLibAPI;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.materials.Materials;
import gregtech.api.enums.materials.Shapes;
import gregtech.api.util.GTModHandler;

public class Recipes implements Runnable {

    public void run() {
        GTModHandler.addCraftingRecipe(
                GTModHandler.getModItem("ifu", "ifu_buildingKit", 1, GTValues.W),
                GTModHandler.RecipeBits.BUFFERED | GTModHandler.RecipeBits.NOT_REMOVABLE
                        | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS,
                new Object[] { "XrX", "SGS", "RIR", 'X',
                        MaterialLibAPI.getStack(Materials.IronMagnetic, Shapes.stick, 1), 'S',
                        MaterialLibAPI.getStack(Materials.Wood, Shapes.stick, 1), 'G',
                        MaterialLibAPI.getStack(Materials.Gold, Shapes.ring, 1), 'I',
                        MaterialLibAPI.getStack(Materials.Iron, Shapes.stick, 1), 'R',
                        MaterialLibAPI.getStack(Materials.Redstone, Shapes.dust, 1) });

    }
}
