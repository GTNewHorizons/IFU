package com.encraft.dz.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.encraft.dz.DayNMod;
import com.encraft.dz.container.ContainerBuildingKit;
import com.encraft.dz.handlers.ConfigHandler;
import com.encraft.dz.inventory.InventoryBuildingKit;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GTUtility;

@SideOnly(Side.CLIENT)
public class GuiInvBuildingKit extends GuiContainer {

    private float xSize_lo;
    private float ySize_lo;
    private static final ResourceLocation iconLocation = new ResourceLocation(
            DayNMod.MOD_ID,
            "textures/gui/guiIngBuildingKit.png");
    private final InventoryBuildingKit inventory;
    private ConfigHandler cfg;

    public GuiInvBuildingKit(EntityPlayer player, InventoryPlayer inventoryPlayer,
            InventoryBuildingKit inventoryCustom) {
        super(new ContainerBuildingKit(player, inventoryPlayer, inventoryCustom));

        this.inventory = inventoryCustom;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        xSize_lo = mouseX;
        ySize_lo = mouseY;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        ItemStack slotek = inventory.getStackInSlot(0);
        String co_wnim;
        if (slotek != null) {
            co_wnim = slotek.getDisplayName();
        } else {
            co_wnim = StatCollector.translateToLocal("IFU.Empty");
        }

        String s = StatCollector.translateToLocal(inventory.getInventoryName());

        int width = fontRendererObj.getStringWidth(s);
        fontRendererObj.drawString(s, 88 - width / 2, 12, 4210752);

        String[] blacklisted = cfg.blacklist;
        for (String ss : blacklisted) {
            if (ss != null && slotek != null && ss.equals(slotek.getUnlocalizedName())) {
                fontRendererObj.drawString(GTUtility.translate("IFU.BlacklistedBlock"), 27, ySize - 116, 4210752);
                break;
            }
        }

        fontRendererObj.drawString(GTUtility.translate("IFU.NameTip", co_wnim), 10, ySize - 96, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(iconLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
