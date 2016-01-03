package pokecube.adventures.client.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.network.PokecubePacketHandler;

public class GuiAFA extends GuiContainer
{
    private static final ResourceLocation guiTexture = new ResourceLocation(
            "pokecube_adventures:textures/gui/afaGui.png");

    long last = 0;

    public GuiAFA(InventoryPlayer inventory, TileEntityAFA tile)
    {
        super(new ContainerAFA(tile, inventory));
    }

    /** Adds the buttons (and other controls) to the screen in question. Called
     * when the GUI is displayed and when the window resizes, the buttonList is
     * cleared beforehand. */
    public void initGui()
    {
        super.initGui();
        int xOffset = 0;
        int yOffset = -11;
        // Range Control
        buttonList.add(new GuiButton(0, width / 2 - xOffset + 64, height / 2 - yOffset - 85, 20, 20, "\u25b2"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 64, height / 2 - yOffset - 65, 20, 20, "\u25bc"));
        if (mc.thePlayer.capabilities.isCreativeMode)
        {
            ContainerAFA container = (ContainerAFA) inventorySlots;
            int num = container.tile.getField(2);
            // Power need toggle
            buttonList.add(new GuiButton(2, width / 2 - xOffset + 64, height / 2 - yOffset - 45, 20, 20,
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
        //Reset Button
        buttonList.add(new GuiButton(11, width / 2 - xOffset - 72, height / 2 - yOffset - 45, 20, 20, "X"));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2,
                4210752);
        ContainerAFA container = (ContainerAFA) inventorySlots;
        if (container.worldObj != null)
        {
            TileEntity te = container.worldObj.getTileEntity(container.pos);

            if (te == null) return;

            TileEntityAFA cloner = (TileEntityAFA) te;

            if (last != cloner.getWorld().getTotalWorldTime())
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(1));
                buffer.writeByte(MessageServer.MESSAGEGUIAFA);
                last = cloner.getWorld().getTotalWorldTime();
                MessageServer message;
                message = new MessageServer(buffer);
                PokecubePacketHandler.sendToServer(message);
            }

            String mess;
            int energy = cloner.getField(0);
            mess = "e:" + energy;
            this.fontRendererObj.drawString(mess, 148 - fontRendererObj.getStringWidth(mess), 66, 4210752);

            int distance = cloner.getField(1);
            mess = "r:" + distance;
            this.fontRendererObj.drawString(mess, 148 - fontRendererObj.getStringWidth(mess), 26, 4210752);
            if (cloner.ability != null && cloner.getStackInSlot(0) != null)
                this.fontRendererObj.drawString("" + cloner.ability.getName(), 48, 6, 4210752);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        ContainerAFA container = (ContainerAFA) inventorySlots;
        TileEntity te = container.worldObj.getTileEntity(container.pos);
        if (te == null) return;

        TileEntityAFA tile = (TileEntityAFA) te;
        MessageServer message;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(21));
        buffer.writeByte(MessageServer.MESSAGEGUIAFA);
        int diff = isShiftKeyDown() ? 10 : 1;
        if (button.id == 0)
        {
            tile.setField(1, tile.getField(1) + diff);
            buffer.writeInt(1);
            buffer.writeInt(tile.getField(1));
        }
        else if (button.id == 1)
        {
            tile.setField(1, tile.getField(1) - diff);
            buffer.writeInt(1);
            buffer.writeInt(tile.getField(1));
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
            buffer.writeInt(2);
            buffer.writeInt(tile.getField(2));
        }
        if (button.id == 3)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(3, tile.getField(3) + diff);
            buffer.writeInt(3);
            buffer.writeInt(tile.getField(3));
        }
        else if (button.id == 4)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(3, tile.getField(3) - diff);
            buffer.writeInt(3);
            buffer.writeInt(tile.getField(3));
        }
        if (button.id == 5)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(4, tile.getField(4) + diff);
            buffer.writeInt(4);
            buffer.writeInt(tile.getField(4));
        }
        else if (button.id == 6)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(4, tile.getField(4) - diff);
            buffer.writeInt(4);
            buffer.writeInt(tile.getField(4));
        }
        if (button.id == 7)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(5, tile.getField(5) + diff);
            buffer.writeInt(5);
            buffer.writeInt(tile.getField(5));
        }
        else if (button.id == 8)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(5, tile.getField(5) - diff);
            buffer.writeInt(5);
            buffer.writeInt(tile.getField(5));
        }
        if (button.id == 9)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(6, tile.getField(6) + diff);
            buffer.writeInt(6);
            buffer.writeInt(tile.getField(6));
        }
        else if (button.id == 10)
        {
            diff = isCtrlKeyDown() ? diff * 10 : diff;
            tile.setField(6, tile.getField(6) - diff);
            buffer.writeInt(6);
            buffer.writeInt(tile.getField(6));
        }
        else if (button.id == 11)
        {
            tile.setField(7, 0);
            buffer.writeInt(7);
            buffer.writeInt(0);
        }
        message = new MessageServer(buffer);
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

}
