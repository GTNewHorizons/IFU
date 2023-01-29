package com.encraft.dz.proxy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import com.encraft.dz.util.KeyBindings;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy {

    public static final Map<Item, ModelBiped> armorModels = new HashMap<Item, ModelBiped>();

    @SideOnly(Side.CLIENT)
    @Override
    public void registerRenderers() {
        KeyBindings.init();

    }

    public void registerTileEntitySpecialRenderer() {

    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {

        return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntity(ctx));
    }

    public int addArmor(String armor) {
        return RenderingRegistry.addNewArmourRendererPrefix(armor);
    }
}
