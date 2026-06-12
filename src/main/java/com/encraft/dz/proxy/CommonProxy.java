package com.encraft.dz.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.encraft.dz.IFU;
import com.encraft.dz.OreFinderPlayerData;
import com.encraft.dz.container.ContainerOreFinder;
import com.encraft.dz.gui.GuiOreFinder;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy implements IGuiHandler {

    public void registerRenderers() {}

    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }

    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {

        if (guiId == IFU.GUI_ORE_FINDER) {
            return new ContainerOreFinder(player, player.inventory, OreFinderPlayerData.get(player).filterInventory);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {

        if (guiId == IFU.GUI_ORE_FINDER) {
            return new GuiOreFinder(player, player.inventory, OreFinderPlayerData.get(player).filterInventory);
        }
        return null;
    }

}
