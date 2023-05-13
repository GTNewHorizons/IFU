package com.encraft.dz.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotBlockedItemInv extends Slot {

    public SlotBlockedItemInv(IInventory inv, int index, int xPos, int yPos) {
        super(inv, index, xPos, yPos);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public void putStack(ItemStack itemStack) {}

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {}

    @Override
    public boolean getHasStack() {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int i) {
        return null;
    }
}
