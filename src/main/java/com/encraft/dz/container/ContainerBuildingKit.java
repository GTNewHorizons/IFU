package com.encraft.dz.container;

import com.encraft.dz.inventory.InventoryBuildingKit;

import com.encraft.dz.items.ItemOreFinderTool;

import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class ContainerBuildingKit extends Container {

	public ContainerBuildingKit(EntityPlayer player, InventoryPlayer inventoryPlayer, InventoryBuildingKit inventoryCustom) {
		int i;

		addSlotToContainer(new SlotItemInv(inventoryCustom, 0, 80, 26));

		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
		if (par2 != 0){
			ItemStack toMove = ((Slot)this.inventorySlots.get(par2)).getStack();
			if (toMove == null || ((SlotItemInv)this.inventorySlots.get(0)).getHasStack())
				return null;
			ItemStack toPlace = toMove.copy();
			toPlace.stackSize = 1;
			toMove.stackSize -= 1;
			if (toMove.stackSize < 1)
			    toMove=null;
            ((SlotItemInv)this.inventorySlots.get(0)).putStack(toPlace);
			((Slot)this.inventorySlots.get(par2)).putStack(toMove);
		} else {
		    boolean canMerge = false;
			ItemStack toMove = ((SlotItemInv)this.inventorySlots.get(0)).getStack();
			if (toMove == null)
				return null;
            for (int i = 1; i < this.inventorySlots.size(); i++) {
                if ((GT_Utility.areStacksEqual(((SlotItemInv) this.inventorySlots.get(0)).getStack(),((Slot) this.inventorySlots.get(i)).getStack()) && ((Slot) this.inventorySlots.get(i)).getStack().stackSize < ((Slot) this.inventorySlots.get(i)).getStack().getMaxStackSize())){
                    ((Slot) this.inventorySlots.get(i)).getStack().stackSize++;
                    ((SlotItemInv) this.inventorySlots.get(0)).putStack(null);
                    canMerge=true;
                    break;
                }
            }
            for (int i = 1; i < this.inventorySlots.size(); i++) {
                if (!canMerge && (!((Slot) this.inventorySlots.get(i)).getHasStack())) {
                    ((SlotItemInv) this.inventorySlots.get(0)).putStack(null);
                    ((Slot) this.inventorySlots.get(i)).putStack(toMove);
                    break;
                }
            }
		}
		detectAndSendChanges();
		return null;
	}
	
}
