package com.encraft.dz.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.encraft.dz.ExtendedPlayer;
import com.encraft.network.PacketDispatcher;
import com.encraft.network.client.SyncPlayerPropsMessage;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DayNModEventHandler {

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            if (ExtendedPlayer.get((EntityPlayer) event.entity) == null)
                ExtendedPlayer.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
            PacketDispatcher
                    .sendTo(new SyncPlayerPropsMessage((EntityPlayer) event.entity), (EntityPlayerMP) event.entity);
        }
    }

    @SubscribeEvent
    public void onClonePlayer(PlayerEvent.Clone event) {
        ExtendedPlayer.get(event.entityPlayer).copy(ExtendedPlayer.get(event.original));
    }
}
