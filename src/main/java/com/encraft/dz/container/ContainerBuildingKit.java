package com.encraft.dz.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.encraft.dz.inventory.InventoryBuildingKit;

import gregtech.api.util.GTUtility;

public class ContainerBuildingKit extends Container {

    public ContainerBuildingKit(EntityPlayer player, InventoryPlayer inventoryPlayer,
            InventoryBuildingKit inventoryCustom) {
        int i;

        addSlotToContainer(new SlotItemInv(inventoryCustom, 0, 80, 26));

        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            // Lock the slot in which the player is currently holding the wand.
            if (i == player.inventory.currentItem) {
                this.addSlotToContainer(new SlotBlockedItemInv(inventoryPlayer, i, 8 + i * 18, 142));
            } else {
                this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
            }
        }
    }

    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        if (par2 != 0) {
            ItemStack toMove = ((Slot) this.inventorySlots.get(par2)).getStack();
            if (toMove == null || ((SlotItemInv) this.inventorySlots.get(0)).getHasStack()) return null;
            ItemStack toPlace = toMove.copy();
            toPlace.stackSize = 1;
            toMove.stackSize -= 1;
            if (toMove.stackSize < 1) toMove = null;
            ((SlotItemInv) this.inventorySlots.get(0)).putStack(toPlace);
            ((Slot) this.inventorySlots.get(par2)).putStack(toMove);
        } else {
            boolean canMerge = false;
            SlotItemInv fromSlot = (SlotItemInv) this.inventorySlots.get(0);
            ItemStack toMove = fromSlot.getStack();
            if (toMove == null) return null;
            for (int i = 1; i < this.inventorySlots.size(); i++) {
                Slot toSlot = (Slot) this.inventorySlots.get(i);
                ItemStack toPlace = toSlot.getStack();

                if (GTUtility.areStacksEqual(toMove, toPlace) && toPlace.stackSize < toPlace.getMaxStackSize()) {
                    toPlace.stackSize++;
                    fromSlot.putStack(null);
                    canMerge = true;
                    break;
                }
            }
            for (int i = 1; i < this.inventorySlots.size(); i++) {
                Slot toSlot = ((Slot) this.inventorySlots.get(i));
                if (!canMerge && toSlot.getStack() == null) {
                    fromSlot.putStack(null);
                    toSlot.putStack(toMove);
                    break;
                }
            }
        }
        detectAndSendChanges();
        return null;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (slotId >= 0 && this.getSlot(slotId) != null && (this.getSlot(slotId).getStack() == player.getHeldItem())) {
            return null;
        }

        // keybind for moving from hotbar slot to hovered slot
        if (mode == 2 && clickedButton >= 0 && clickedButton < 9) {
            int hotbarIndex = 1 + (9 * 3) + clickedButton;
            Slot hotbarSlot = getSlot(hotbarIndex);
            if (hotbarSlot instanceof SlotBlockedItemInv) {
                return null;
            }
        }

        return super.slotClick(slotId, clickedButton, mode, player);
    }

}
