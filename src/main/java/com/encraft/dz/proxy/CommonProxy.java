package com.encraft.dz.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.encraft.dz.DayNMod;
import com.encraft.dz.ExtendedPlayer;
import com.encraft.dz.container.ContainerBuildingKit;
import com.encraft.dz.gui.GuiInvBuildingKit;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy implements IGuiHandler {

    public void registerRenderers() {}

    public void registerTileEntitySpecialRenderer() {}

    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }

    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {

        if (guiId == DayNMod.GUI_CUSTOM_INV1) {
            return new ContainerBuildingKit(player, player.inventory, ExtendedPlayer.get(player).inventorybk);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {

        if (guiId == DayNMod.GUI_CUSTOM_INV1) {
            return new GuiInvBuildingKit(player, player.inventory, ExtendedPlayer.get(player).inventorybk);
        }
        return null;
    }

    public void registerTileEntities() {}

    public void init(FMLInitializationEvent event) {}

    public int addArmor(String armor) {
        return 0;
    }

}
