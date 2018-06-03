package com.encraft.dz;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;





import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.handlers.DayNModEventHandler;
import com.encraft.dz.items.DayNModItems;
import com.encraft.dz.lib.Reference;
import com.encraft.dz.network.MessageCheckKeypadCode;
import com.encraft.dz.network.MessageSetKeypadCode;
import com.encraft.dz.proxy.CommonProxy;
import com.encraft.dz.util.FindIDFromHandCommand;
import com.encraft.network.PacketDispatcher;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VER)
public class DayNMod {
    
    @Mod.Instance(Reference.MOD_ID)
    public static DayNMod instance;
    
    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
	public static CommonProxy proxy;

    public static SimpleNetworkWrapper snw; 

    private static int modGuiIndex = 10;
	public static final int GUI_CUSTOM_INV1 = modGuiIndex++;
	
	private static int modEntityIndex = 0;
    
  
    public static CreativeTabs dnCVt = new CreativeTabs("DayNModTools") //tools tab
    {
	    public Item getTabIconItem()
	    {
	    	return DayNModItems.buildingKit; 
	    }
    };
 
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	
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
    public void load(FMLInitializationEvent event)
    {
    	proxy.registerRenderers();
    	MinecraftForge.EVENT_BUS.register(new DayNModEventHandler());
    	NetworkRegistry.INSTANCE.registerGuiHandler(this, new CommonProxy());
    	
    }
        
    @EventHandler
    public void postInit(FMLPostInitializationEvent event){}
    

    
}
