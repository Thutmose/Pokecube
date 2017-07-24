package pokecube.adventures.client.gui.cloner;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.cloner.container.ContainerGeneExtractor;

public class GuiExtractor extends GuiContainer
{
    private static final ResourceLocation guiTexture = new ResourceLocation(
            "pokecube_adventures:textures/gui/extractorgui.png");
    IInventory                            tile;

    public GuiExtractor(InventoryPlayer inv, IInventory tile)
    {
        super(new ContainerGeneExtractor(inv, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        int l1 = this.getCookProgressScaled(24);
        this.drawTexturedModalRect(k + 78, l + 34, 176, 0, l1 + 1, 16);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(I18n.format("container.extractor", new Object[0]), 28, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2,
                4210752);
    }

    private int getCookProgressScaled(int pixels)
    {
        int i = this.tile.getField(0);
        int j = this.tile.getField(1);
        // System.out.println(i + " " + j);
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

}
