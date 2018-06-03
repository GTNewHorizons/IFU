package com.encraft.dz.handlers;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class DayNModClientMessage implements IMessage{

	public String text;

    public DayNModClientMessage() {}
    
	public DayNModClientMessage(String text) 
    {
    	this.text = text;
	}

	@Override
    public void fromBytes(ByteBuf buf) 
	{
		this.text = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		ByteBufUtils.writeUTF8String(buf, text);
	} 

}