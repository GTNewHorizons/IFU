package com.encraft.dz.container;

import com.encraft.dz.items.ItemOreFinderTool;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotItemInv extends Slot
{
	public SlotItemInv(IInventory inv, int index, int xPos, int yPos) {
		super(inv, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !(stack.getItem() instanceof ItemOreFinderTool);
	}
}