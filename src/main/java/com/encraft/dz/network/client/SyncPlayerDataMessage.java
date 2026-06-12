package com.encraft.dz.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.encraft.dz.OreFinderPlayerData;
import com.encraft.dz.network.AbstractMessage.AbstractClientMessage;

import cpw.mods.fml.relauncher.Side;

public class SyncPlayerDataMessage extends AbstractClientMessage<SyncPlayerDataMessage> {

    private NBTTagCompound data;

    @SuppressWarnings("unused")
    public SyncPlayerDataMessage() {}

    public SyncPlayerDataMessage(EntityPlayer player) {
        data = new NBTTagCompound();
        OreFinderPlayerData.get(player).saveNBTData(data);
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        data = buffer.readNBTTagCompoundFromBuffer();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        OreFinderPlayerData.get(player).loadNBTData(data);
    }

}
