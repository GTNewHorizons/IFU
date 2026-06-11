package com.encraft.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.encraft.dz.DayNMod;
import com.encraft.network.client.SyncPlayerPropsMessage;
import com.encraft.network.server.OpenGuiMessage;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketDispatcher {

    // a simple counter will allow us to get rid of 'magic' numbers used during packet registration
    private static byte packetId = 0;

    /**
     * The SimpleNetworkWrapper instance is used both to register and send packets. Since I will be adding wrapper
     * methods, this field is private, but you should make it public if you plan on using it directly.
     */
    private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(DayNMod.MOD_ID);

    /**
     * Call this during pre-init or loading and register all of your packets (messages) here
     */
    public static final void registerPackets() {
        // Packets handled on CLIENT
        registerMessage(SyncPlayerPropsMessage.class);

        // Packets handled on SERVER
        registerMessage(OpenGuiMessage.class);
    }

    /**
     * Registers an {@link AbstractMessage} to the appropriate side(s)
     */
    private static final <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(
            Class<T> clazz) {
        // We can tell by the message class which side to register it on by using #isAssignableFrom (google it)

        // Also, one can see the convenience of using a static counter 'packetId' to keep
        // track of the current index, rather than hard-coding them all, plus it's one less
        // parameter to pass.
        if (AbstractMessage.AbstractClientMessage.class.isAssignableFrom(clazz)) {
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.CLIENT);
        } else if (AbstractMessage.AbstractServerMessage.class.isAssignableFrom(clazz)) {
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
        } else {
            // hopefully you didn't forget to extend the right class, or you will get registered on both sides
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId, Side.CLIENT);
            PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
        }
    }

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleNetworkWrapper#sendTo(IMessage, EntityPlayerMP)}
     */
    public static final void sendTo(IMessage message, EntityPlayerMP player) {
        PacketDispatcher.dispatcher.sendTo(message, player);
    }
}
