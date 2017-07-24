package pokecube.adventures.client.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.network.packets.PacketAFA;
import pokecube.core.network.PokecubePacketHandler;

public class GuiAFA extends GuiContainer
{
    private static final ResourceLocation guiTexture = new ResourceLocation(
            "pokecube_adventures:textures/gui/afagui.png");

    long                                  last       = 0;

    public GuiAFA(InventoryPlayer inventory, TileEntityAFA tile)
    {
        super(new ContainerAFA(tile, inventory));
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
    protected void actionPerformed(GuiButton button) throws IOException
    {
        ContainerAFA container = (ContainerAFA) inventorySlots;
        TileEntity te = container.world.getTileEntity(container.pos);
        if (te == null) return;

        TileEntityAFA tile = (TileEntityAFA) te;
        PacketAFA message;
        int id = -1;
        int val = -1;
        int diff = isShiftKeyDown() ? 10 : 1;
        if (button.id == 0)
        {
            tile.setField(1, tile.getField(1) + diff);
            id = 1;
            val = tile.getField(id);
        }
        else if (button.id == 1)
        {
            tile.setField(1, tile.getField(1) - diff);
            id = 1;
            val = tile.getField(id);
        }
        else if (button.id == 2)
        {
            int num = tile.getField(2);
            if (num == 0)
            {
                tile.setField(2, 1);
                button.displayString = "O";
            }
            else
            {
                tile.setField(2, 0);
                button.displayString = "X";
            }
            id = 2;
            val = tile.getField(id);
        }
        if (button.id == 3)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(3, tile.getField(3) + diff);
            id = 3;
            val = tile.getField(id);
        }
        else if (button.id == 4)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(3, tile.getField(3) - diff);
            id = 3;
            val = tile.getField(id);
        }
        if (button.id == 5)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(4, tile.getField(4) + diff);
            id = 4;
            val = tile.getField(id);
        }
        else if (button.id == 6)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(4, tile.getField(4) - diff);
            id = 4;
            val = tile.getField(id);
        }
        if (button.id == 7)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(5, tile.getField(5) + diff);
            id = 5;
            val = tile.getField(id);
        }
        else if (button.id == 8)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(5, tile.getField(5) - diff);
            id = 5;
            val = tile.getField(id);
        }
        if (button.id == 9)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(6, tile.getField(6) + diff);
            id = 6;
            val = tile.getField(id);
        }
        else if (button.id == 10)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(6, tile.getField(6) - diff);
            id = 6;
            val = tile.getField(id);
        }
        else if (button.id == 11)
        {
            tile.setField(7, 0);
            id = 7;
            val = tile.getField(id);
        }

        message = new PacketAFA();
        if (id != -1)
        {
            message.data.setInteger("I", id);
            message.data.setInteger("V", val);
        }
        PokecubePacketHandler.sendToServer(message);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2,
                4210752);
        ContainerAFA container = (ContainerAFA) inventorySlots;
        if (container.world != null)
        {
            TileEntity te = container.world.getTileEntity(container.pos);
            if (te == null) return;
            TileEntityAFA cloner = (TileEntityAFA) te;
            String mess;
            int energy = cloner.getField(0);
            mess = "e:" + energy;
            this.fontRenderer.drawString(mess, 148 - fontRenderer.getStringWidth(mess), 66, 4210752);
            int distance = cloner.getField(1);
            mess = "r:" + distance;
            this.fontRenderer.drawString(mess, 148 - fontRenderer.getStringWidth(mess), 26, 4210752);
            if (cloner.ability != null && cloner.getStackInSlot(0) != null)
                this.fontRenderer.drawString("" + I18n.format(cloner.ability.getName()), 48, 6, 4210752);
        }
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
        if (mc.player.capabilities.isCreativeMode)
        {
            ContainerAFA container = (ContainerAFA) inventorySlots;
            int num = container.tile.getField(2);
            // Power need toggle
            buttonList.add(new GuiButton(2, width / 2 - xOffset + 69, height / 2 - yOffset - 45, 20, 20,
                    num == 1 ? "O" : "X"));
        }
        // Scale Buttons
        buttonList.add(new GuiButton(3, width / 2 - xOffset + 10, height / 2 - yOffset - 65, 20, 20, "+"));
        buttonList.add(new GuiButton(4, width / 2 - xOffset + 10, height / 2 - yOffset - 45, 20, 20, "-"));
        // Position Buttons
        buttonList.add(new GuiButton(5, width / 2 - xOffset - 12, height / 2 - yOffset - 65, 20, 20, "\u25c0"));
        buttonList.add(new GuiButton(6, width / 2 - xOffset - 12, height / 2 - yOffset - 45, 20, 20, "\u25b6"));
        // Position Buttons
        buttonList.add(new GuiButton(7, width / 2 - xOffset - 32, height / 2 - yOffset - 65, 20, 20, "\u25b2"));
        buttonList.add(new GuiButton(8, width / 2 - xOffset - 32, height / 2 - yOffset - 45, 20, 20, "\u25bc"));
        // Position Buttons
        buttonList.add(new GuiButton(9, width / 2 - xOffset - 52, height / 2 - yOffset - 65, 20, 20, "\u25c0"));
        buttonList.add(new GuiButton(10, width / 2 - xOffset - 52, height / 2 - yOffset - 45, 20, 20, "\u25b6"));
        // Reset Button
        buttonList.add(new GuiButton(11, width / 2 - xOffset - 72, height / 2 - yOffset - 45, 20, 20, "X"));
    }

}
