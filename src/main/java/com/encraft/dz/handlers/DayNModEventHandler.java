package com.encraft.dz.handlers;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.encraft.dz.ExtendedPlayer;
import com.encraft.network.PacketDispatcher;
import com.encraft.network.client.SyncPlayerPropsMessage;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DayNModEventHandler {

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.entity instanceof EntityItem) {

        }

        if (event.entity instanceof EntityPlayer) {
            if (ExtendedPlayer.get((EntityPlayer) event.entity) == null)
                ExtendedPlayer.register((EntityPlayer) event.entity);
            // ExtendedPlayer props = ExtendedPlayer.get((EntityPlayer) event.entity);

        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // System.out.println("witamt " + event);

        if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
            PacketDispatcher
                    .sendTo(new SyncPlayerPropsMessage((EntityPlayer) event.entity), (EntityPlayerMP) event.entity);
            ExtendedPlayer props = ExtendedPlayer.get((EntityPlayer) event.entity);
            EntityPlayer player = (EntityPlayer) event.entity;

        }
    }

    @SubscribeEvent
    public void onClonePlayer(PlayerEvent.Clone event) {
        ExtendedPlayer.get(event.entityPlayer).copy(ExtendedPlayer.get(event.original));
    }

    @SubscribeEvent
    public void onLivingFallEvent(LivingFallEvent event) {
        if (event.entity instanceof EntityPlayer) {
            ExtendedPlayer props = ExtendedPlayer.get((EntityPlayer) event.entity);

        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {

        if (event.entity instanceof EntityPlayer) {
            // ExtendedPlayer props = ExtendedPlayer.get((EntityPlayer) event.entity);
            EntityPlayer player = (EntityPlayer) event.entity;

        }
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
