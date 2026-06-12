package com.encraft.dz;

import net.minecraft.creativetab.*;
import net.minecraft.item.*;
import net.minecraftforge.common.*;

import com.encraft.dz.handlers.*;
import com.encraft.dz.items.*;
import com.encraft.dz.lib.*;
import com.encraft.dz.network.*;
import com.encraft.dz.proxy.*;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.*;

@Mod(modid = IFU.MOD_ID, name = "I Will Find You", version = Tags.MOD_VER)
public class IFU {

    public static final String MOD_ID = "ifu";

    @Mod.Instance(MOD_ID)
    public static IFU instance;

    @SidedProxy(clientSide = "com.encraft.dz.proxy.ClientProxy", serverSide = "com.encraft.dz.proxy.CommonProxy")
    public static CommonProxy proxy;

    private static int modGuiIndex = 10;
    public static final int GUI_ORE_FINDER = modGuiIndex++;

    public static CreativeTabs creativeTab = new CreativeTabs("IFU") // tools tab
    {

        public Item getTabIconItem() {
            return IFUItems.oreFinder;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        String configDir = event.getModConfigurationDirectory().toString();
        ConfigHandler.init(configDir);
        FMLCommonHandler.instance().bus().register(new ConfigHandler());

        IFUItems.load();

        PacketDispatcher.registerPackets();

    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        proxy.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new PlayerDataEventHandler());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new CommonProxy());

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        new Recipes().run();
    }

}
