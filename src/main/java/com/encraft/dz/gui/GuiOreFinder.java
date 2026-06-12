package com.encraft.dz.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.encraft.dz.IFU;
import com.encraft.dz.container.ContainerOreFinder;
import com.encraft.dz.inventory.InventoryOreFinder;
import com.encraft.dz.items.OreFinderSearch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GTUtility;

@SideOnly(Side.CLIENT)
public class GuiOreFinder extends GuiContainer {

    private static final ResourceLocation iconLocation = new ResourceLocation(
            IFU.MOD_ID,
            "textures/gui/gui_ore_finder.png");
    private final InventoryOreFinder inventory;

    public GuiOreFinder(EntityPlayer player, InventoryPlayer inventoryPlayer, InventoryOreFinder inventoryCustom) {
        super(new ContainerOreFinder(player, inventoryPlayer, inventoryCustom));

        this.inventory = inventoryCustom;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        ItemStack filterStack = inventory.getStackInSlot(0);
        OreFinderSearch.MatchTarget target = OreFinderSearch.resolveMatch(filterStack);

        String title = StatCollector.translateToLocal(inventory.getInventoryName());
        int width = fontRendererObj.getStringWidth(title);
        fontRendererObj.drawString(title, 88 - width / 2, 12, 4210752);

        if (target.isBlocklisted()) {
            fontRendererObj.drawString(GTUtility.translate("IFU.BlocklistedBlock"), 27, ySize - 116, 4210752);
        }

        int maxTextWidth = xSize - 10 - 8;

        String name = filterStack != null ? filterStack.getDisplayName() : StatCollector.translateToLocal("IFU.Empty");
        fontRendererObj
                .drawString(fit(GTUtility.translate("IFU.NameTip", name), maxTextWidth), 10, ySize - 106, 4210752);

        if (filterStack != null) {
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
