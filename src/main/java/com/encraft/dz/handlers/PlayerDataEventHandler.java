package com.encraft.dz.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.encraft.dz.OreFinderPlayerData;
import com.encraft.dz.network.PacketDispatcher;
import com.encraft.dz.network.client.SyncPlayerDataMessage;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerDataEventHandler {

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            if (OreFinderPlayerData.get((EntityPlayer) event.entity) == null)
                OreFinderPlayerData.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
            PacketDispatcher
                    .sendTo(new SyncPlayerDataMessage((EntityPlayer) event.entity), (EntityPlayerMP) event.entity);
        }
    }

    @SubscribeEvent
    public void onClonePlayer(PlayerEvent.Clone event) {
        OreFinderPlayerData.get(event.entityPlayer).copy(OreFinderPlayerData.get(event.original));
    }
}
