package com.encraft.dz.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy {

    @SideOnly(Side.CLIENT)
    @Override
    public void registerRenderers() {}

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {

        return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntity(ctx));
    }
}
