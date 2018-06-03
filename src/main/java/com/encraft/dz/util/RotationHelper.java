package com.encraft.dz.util;

import java.util.ArrayList;
import java.util.List;

public class RotationHelper {

	public static final String[] OldLogBlock = {"minecraft:log"};
	public static final int[] OakLog = {4, 8};
	public static final int[] SpruceLog = {5, 9};
	public static final int[] BirchLog = {6, 10};
	public static final int[] JungleLog = {7, 11};
	public static final String[] NewLogBlock = {"minecraft:log2"};
	public static final int[] AcaciaLog = {4, 8};
	public static final int[] DarkOakLog = {5, 9};
	
	public static final String[] StairsBlock = {"minecraft:oak_stairs", "minecraft:stone_stairs", "minecraft:brick_stairs", "minecraft:stone_brick_stairs", "minecraft:nether_brick_stairs", "minecraft:sandstone_stairs", "minecraft:spruce_stairs", "minecraft:birch_stairs", "minecraft:jungle_stairs", "minecraft:quartz_stairs", "minecraft:acacia_stairs", "minecraft:dark_oak_stairs"};
	public static final int[] StarisNormal = {0, 2, 1, 3};
	public static final int[] StarisUpSideDown = {4, 6, 5, 7};
	
	//Chests, Furnaces, Trapped Chests, Jack o'Laterns, Dispenser, Droppers, Pistons, Sticky Pistons, Ladders, Signs, Hoppers
	public static final String[] GenericBlock = {"minecraft:chest", "minecraft:furnace", "minecraft:trapped_chest", "minecraft:lit_pumpkin", "minecraft:dispenser", "minecraft:sticky_piston", "minecraft:piston", "minecraft:dropper", "minecraft:hopper", "minecraft:wall_sign", "minecraft:ladder"};
	public static final int[] GenericBlocks = {4, 2, 5, 3}; 
	
	public static final String[] AnvilBlock = {"minecraft:anvil"};
	public static final int[] AnvilNormal = {1, 2};
	public static final int[] AnvilDamaged = {5, 6};
	public static final int[] AnvilVeryDamaged = {9, 10};
	
	public static final String[] BedBlock = {"minecraft:bed"};
	public static final int[] Bed = {1, 2, 3, 0};
	public static final int[] BedTop = {9, 10, 11, 8};
	
	public static final String[] LeverOnTheBlock = {"minecraft:lever"};
	public static final int[] LeverOnTheGround = {5, 6};
	public static final int[] LeverOnTheCelling = {0, 7};
	
	//Levers, Red Torches, Buttons 
	public static final String[] RedStoneDevicesOnTheWallBlock = {"minecraft:lever", "minecraft:redstone_torch", "minecraft:stone_button", "minecraft:wooden_button", "minecraft:torch"};
	public static final int[] RedStoneDevicesOnTheWall = {2, 4, 1, 3};
	
	public static final String[] TrapdoorBlock = {"minecraft:trapdoor"};
	public static final int[] TrapdoorClosed = {2, 0, 3, 1};
	public static final int[] TrapdoorOpened = {6, 4, 7, 5};
	
	public static final String[] TripwireHookBlock = {"minecraft:tripwire_hook"};
	public static final int[] TripwireHook = {1, 2, 3, 0};
	
	public static final String[] DoorBlock = {"minecraft:iron_door", "minecraft:wooden_door"};
	public static final int[] DoorBottomClosed = {0, 1, 2, 3};
	public static final int[] DoorBottomOpened = {4, 5, 6, 7};
	
	//Repeaters, Comparators
	public static final String[] RedStoneDevicesBlock = {"minecraft:unpowered_repeater", "minecraft:unpowered_comparator", "minecraft:powered_repeater"};
	public static final int[] RedStoneDevices = {3, 0, 1, 2};
	public static final int[] ComparatorSecondState = {7, 4, 5, 6};
	
	public static int getIndex(int[] inputList, int value)
	{
		int searchedIndex = -1;
		for (int i = 0; i < inputList.length; i++)
		{
			if (inputList[i] == value)
			{
				searchedIndex = i;
				break;
			}
		}
		return searchedIndex;
	}
	public static boolean checkIfContains(String[] inputList, String value)
	{
		for (int i = 0; i < inputList.length; i++)
		{
			if (inputList[i].equalsIgnoreCase(value))
			{
				return true;
			}
		}
		return false;
	}
	public static boolean checkIfContains(int[] inputList, int value)
	{
		for (int i = 0; i < inputList.length; i++)
		{
			if (inputList[i] == value)
			{
				return true;
			}
		}
		return false;
	}
	public static int getValue(int[] inputList, int currentStatus, int change)
	{
		int returnValue = -1;
		currentStatus = getIndex(inputList, currentStatus);
		for (int i = 0; i<change; i++) 
		{
			currentStatus = currentStatus + 1;
			if (currentStatus > (inputList.length - 1)) { currentStatus = 0;}
		}
		returnValue = inputList[currentStatus];
		return returnValue;
	}
	public static int ifContainsChange(int[] inputList, int currentStatus, int change)
	{
		if (checkIfContains(inputList, currentStatus)) 
		{
			return getValue(inputList, currentStatus, change);
		}
		return currentStatus;
	}
	public static String[] Rotate(String[] blockProperities, int Rotation)
	{
		String block = blockProperities[0];
		String pmeta = blockProperities[1];
		String x = blockProperities[2];
		String y = blockProperities[3];
		String z = blockProperities[4];
		int meta = Integer.parseInt(pmeta);
		int psx = Integer.parseInt(x);
		int sy = Integer.parseInt(y);
		int psz = Integer.parseInt(z); 
		
		
		
		if (checkIfContains(OldLogBlock, block))
		{
			meta = ifContainsChange(OakLog, meta, Rotation);
			meta = ifContainsChange(SpruceLog, meta, Rotation);
			meta = ifContainsChange(BirchLog, meta, Rotation);
			meta = ifContainsChange(JungleLog, meta, Rotation);
		}
		if (checkIfContains(NewLogBlock, block))
		{
			meta = ifContainsChange(AcaciaLog, meta, Rotation);
			meta = ifContainsChange(DarkOakLog, meta, Rotation);
		}
		if (checkIfContains(StairsBlock, block))
		{
			meta = ifContainsChange(StarisNormal, meta, Rotation);
			meta = ifContainsChange(StarisUpSideDown, meta, Rotation);
		}
		if (checkIfContains(GenericBlock, block))
		{
			meta = ifContainsChange(GenericBlocks, meta, Rotation);
		}
		if (checkIfContains(AnvilBlock, block))
		{
			meta = ifContainsChange(AnvilNormal, meta, Rotation);
			meta = ifContainsChange(AnvilDamaged, meta, Rotation);
			meta = ifContainsChange(AnvilVeryDamaged, meta, Rotation);
		}
		if (checkIfContains(BedBlock, block))
		{
			meta = ifContainsChange(Bed, meta, Rotation);
			meta = ifContainsChange(BedTop, meta, Rotation);
		}
		if (checkIfContains(LeverOnTheBlock, block))
		{
			meta = ifContainsChange(LeverOnTheGround, meta, Rotation);
			meta = ifContainsChange(LeverOnTheCelling, meta, Rotation);
		}
		if (checkIfContains(RedStoneDevicesOnTheWallBlock, block))
		{
			meta = ifContainsChange(RedStoneDevicesOnTheWall, meta, Rotation);
		}
		if (checkIfContains(TrapdoorBlock, block))
		{
			meta = ifContainsChange(TrapdoorClosed, meta, Rotation);
			meta = ifContainsChange(TrapdoorOpened, meta, Rotation);
		}
		if (checkIfContains(TripwireHookBlock, block))
		{
			meta = ifContainsChange(TripwireHook, meta, Rotation);
		}
		if (checkIfContains(DoorBlock, block))
		{
			meta = ifContainsChange(DoorBottomClosed, meta, Rotation);
			meta = ifContainsChange(DoorBottomOpened, meta, Rotation);
		}
		if (checkIfContains(RedStoneDevicesBlock, block))
		{
			meta = ifContainsChange(RedStoneDevices, meta, Rotation);
			meta = ifContainsChange(ComparatorSecondState, meta, Rotation);
		}
		
		
		int sx, sz;	
		sx = psx;
		sz = psz;
		if (Rotation == 1)
		{
			sx = psz * (-1);
			sz = psx;
		}
		else if (Rotation == 2)
		{
			sx = psx * (-1);
			sz = psz * (-1);
		}
		else if (Rotation == 3)
		{
			sx = psz;
			sz = psx * (-1);
		}
		String[] output = {block, String.valueOf(meta), String.valueOf(sx), String.valueOf(sy), String.valueOf(sz)};
		return output;
	}	
}
