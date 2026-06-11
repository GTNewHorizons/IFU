package com.encraft.network.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import com.encraft.dz.DayNMod;
import com.encraft.network.AbstractMessage.AbstractServerMessage;

import cpw.mods.fml.relauncher.Side;

public class OpenGuiMessage extends AbstractServerMessage<OpenGuiMessage> {

    // this will store the id of the gui to open
    private int id;

    // The basic, no-argument constructor MUST be included to use the new automated handling
    public OpenGuiMessage() {}

    // if there are any class fields, be sure to provide a constructor that allows
    // for them to be initialized, and use that constructor when sending the packet
    public OpenGuiMessage(int id) {
        this.id = id;
    }

    @Override
    protected void read(PacketBuffer buffer) {
        // basic Input/Output operations, very much like DataInputStream
        id = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer) {
        // basic Input/Output operations, very much like DataOutputStream
        buffer.writeInt(id);
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        // using the message instance gives access to 'this.id'

        player.openGui(
                DayNMod.instance,
                this.id,
                player.worldObj,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);
    }
}
