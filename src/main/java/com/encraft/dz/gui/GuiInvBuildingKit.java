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
import com.encraft.dz.inventory.InventoryBuildingKit;
import com.encraft.dz.items.ItemOreFinderTool;

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
        ItemOreFinderTool.MatchTarget target = ItemOreFinderTool.resolveMatch(slotek);

        String title = StatCollector.translateToLocal(inventory.getInventoryName());
        int width = fontRendererObj.getStringWidth(title);
        fontRendererObj.drawString(title, 88 - width / 2, 12, 4210752);

        if (target.isBlocklisted()) {
            fontRendererObj.drawString(GTUtility.translate("IFU.BlocklistedBlock"), 27, ySize - 116, 4210752);
        }

        int maxTextWidth = xSize - 10 - 8;

        String name = slotek != null ? slotek.getDisplayName() : StatCollector.translateToLocal("IFU.Empty");
        fontRendererObj
                .drawString(fit(GTUtility.translate("IFU.NameTip", name), maxTextWidth), 10, ySize - 106, 4210752);

        if (slotek != null) {
            int color = target.canSearch() ? 4210752 : 0xAA0000;
            fontRendererObj.drawString(
                    fit(GTUtility.translate("IFU.SearchTip", target.describe()), maxTextWidth),
                    10,
                    ySize - 96,
                    color);
        }
    }

    private String fit(String text, int maxWidth) {
        if (fontRendererObj.getStringWidth(text) <= maxWidth) {
            return text;
        }
        return fontRendererObj.trimStringToWidth(text, maxWidth - fontRendererObj.getStringWidth("...")) + "...";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(iconLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
