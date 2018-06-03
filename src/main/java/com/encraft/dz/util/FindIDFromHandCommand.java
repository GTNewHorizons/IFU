package com.encraft.dz.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class FindIDFromHandCommand implements ICommand
{
	private List<String> aliases;
	
	public FindIDFromHandCommand()
	{
		this.aliases = new ArrayList<String>();
		this.aliases.add("hid");
		this.aliases.add("handid");
		this.aliases.add("holdingid");
	}
	
	@Override
	public int compareTo(Object o)
	{
		return 0;
	}
	
	@Override
	public String getCommandName()
	{
		return "handID";
	}
	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_)
	{
		return "/handid";
	}
	
	@Override
	public List<?> getCommandAliases()
	{
		return this.aliases;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] input)
	{
		if(sender instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)sender;
			ItemStack holding = player.getHeldItem();
			
			if(holding != null)
			{
				String name = Item.itemRegistry.getNameForObject(Item.itemRegistry.getObjectById(Item.getIdFromItem(holding.getItem())));
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Item textual ID: " + EnumChatFormatting.AQUA + name));
			}
			else
			{
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_RED + "You must be holding an item!"));
			}
		}
		else
		{
			sender.addChatMessage(new ChatComponentText("You must be a player to use this command"));
		}
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
	{
		return true;
	}
	
	@Override
	public List<?> addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
	{
		return null;
	}
	
	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
	{
		return false;
	}
	
}
