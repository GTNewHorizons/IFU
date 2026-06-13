package com.encraft.dz.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.encraft.dz.IFU;
import com.encraft.dz.network.client.SyncPlayerDataMessage;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketDispatcher {

    private static byte packetId = 0;

    private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(IFU.MOD_ID);

    /** Register all packets here; call during pre-init. */
    public static void registerPackets() {
        registerMessage(SyncPlayerDataMessage.class);
    }

    /** Registers an {@link AbstractMessage} on the side(s) implied by which subclass it extends. */
    private static <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz) {
        if (AbstractMessage.AbstractClientMessage.class.isAssignableFrom(clazz)) {
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.CLIENT);
        } else if (AbstractMessage.AbstractServerMessage.class.isAssignableFrom(clazz)) {
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
        } else {
            // Extends neither side-specific subclass: register on both sides.
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId, Side.CLIENT);
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
        }
    }

    /** Send a message to the given player's client. */
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        PacketDispatcher.dispatcher.sendTo(message, player);
    }
}
