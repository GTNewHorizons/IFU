package com.encraft.dz;

import net.minecraft.creativetab.*;
import net.minecraft.item.*;
import net.minecraftforge.common.*;

import com.encraft.dz.handlers.*;
import com.encraft.dz.items.*;
import com.encraft.dz.lib.*;
import com.encraft.dz.network.*;
import com.encraft.dz.proxy.*;
import com.encraft.network.*;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.*;

@Mod(modid = DayNMod.MOD_ID, name = "I Will Find You", version = Tags.MOD_VER)
public class DayNMod {

    public static final String MOD_ID = "ifu";

    @Mod.Instance(MOD_ID)
    public static DayNMod instance;

    @SidedProxy(clientSide = "com.encraft.dz.proxy.ClientProxy", serverSide = "com.encraft.dz.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper snw;

    private static int modGuiIndex = 10;
    public static final int GUI_CUSTOM_INV1 = modGuiIndex++;

    private static int modEntityIndex = 0;

    public static CreativeTabs dnCVt = new CreativeTabs("DayNModTools") // tools tab
    {

        public Item getTabIconItem() {
            return DayNModItems.buildingKit;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        String configDir = event.getModConfigurationDirectory().toString();
        ConfigHandler.init(configDir);
        FMLCommonHandler.instance().bus().register(new ConfigHandler());

        DayNModItems.load();

        PacketDispatcher.registerPackets();

        snw = NetworkRegistry.INSTANCE.newSimpleChannel("DNMNetwork");
        snw.registerMessage(MessageSetKeypadCode.MessageHandler.class, MessageSetKeypadCode.class, 2, Side.SERVER);
        snw.registerMessage(MessageCheckKeypadCode.MessageHandler.class, MessageCheckKeypadCode.class, 3, Side.SERVER);

    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        proxy.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new DayNModEventHandler());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new CommonProxy());

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        new Recipes().run();
    }

}
