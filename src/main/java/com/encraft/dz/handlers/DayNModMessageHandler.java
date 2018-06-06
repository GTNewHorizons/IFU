package com.encraft.dz.handlers;

import net.minecraft.entity.player.EntityPlayer;


import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class DayNModMessageHandler implements IMessageHandler<DayNModMessage, IMessage> { 
	
	
	EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
	@Override
	public IMessage onMessage(DayNModMessage message, MessageContext ctx)
	{
		
    	return null;
	}
}