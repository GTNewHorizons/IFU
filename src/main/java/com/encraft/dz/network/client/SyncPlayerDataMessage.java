package com.encraft.dz.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.encraft.dz.OreFinderPlayerData;
import com.encraft.dz.network.AbstractMessage.AbstractClientMessage;

import cpw.mods.fml.relauncher.Side;

public class SyncPlayerDataMessage extends AbstractClientMessage<SyncPlayerDataMessage> {
    // Previously, we've been writing each field in our properties one at a time,
    // but that is really annoying, and we've already done it in the save and load
    // NBT methods anyway, so here's a slick way to efficiently send all of your
    // extended data, and no matter how much you add or remove, you'll never have
    // to change the packet / synchronization of your data.

    // this will store our OreFinderPlayerData data, allowing us to easily read and write
    private NBTTagCompound data;

    // The basic, no-argument constructor MUST be included to use the new automated handling
    public SyncPlayerDataMessage() {}

    // We need to initialize our data, so provide a suitable constructor:
    public SyncPlayerDataMessage(EntityPlayer player) {
        // create a new tag compound
        data = new NBTTagCompound();
        // and save our player's data into it
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
