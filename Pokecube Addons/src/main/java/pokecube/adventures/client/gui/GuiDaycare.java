package pokecube.adventures.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.afa.ContainerDaycare;
import pokecube.adventures.blocks.afa.TileEntityDaycare;

public class GuiDaycare extends GuiContainer
{
    private static final ResourceLocation guiTexture = new ResourceLocation(
            "pokecube_adventures:textures/gui/afagui.png");

    long                                  last       = 0;

    public GuiDaycare(InventoryPlayer inventory, TileEntityDaycare tile)
    {
        super(new ContainerDaycare(tile, inventory));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(k + 14, l + 31, 14, 11, 18, 18);
        this.drawTexturedModalRect(k + 14, l + 51, 14, 11, 18, 18);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2,
                4210752);
    }

    /** Adds the buttons (and other controls) to the screen in question. Called
     * when the GUI is displayed and when the window resizes, the buttonList is
     * cleared beforehand. */
    @Override
    public void initGui()
    {
        super.initGui();
        int xOffset = 5;
        int yOffset = -11;
        // Range Control
        buttonList.add(new GuiButton(0, width / 2 - xOffset + 69, height / 2 - yOffset - 85, 20, 20, "\u25b2"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 69, height / 2 - yOffset - 65, 20, 20, "\u25bc"));
    }

}
