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
        buttonList.add(new GuiButton(0, width / 2 - xOffset - 137, height
                / 2 - yOffset - 85, 50, 20, "up"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset - 137, height
                / 2 - yOffset - 65, 50, 20, "down"));
        if (mc.thePlayer.capabilities.isCreativeMode)
        {
            buttonList.add(new GuiButton(2, width / 2 - xOffset - 137, height
                    / 2 - yOffset - 45, 50, 20, "energy"));
        }
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

            int energy = cloner.getField(0);
            this.fontRendererObj.drawString("e:" + energy, 128, 6, 4210752);

            int distance = cloner.getField(1);
            this.fontRendererObj.drawString("r:" + distance, 128, 26, 4210752);
            if(cloner.ability!=null && cloner.getStackInSlot(0) !=null)
                this.fontRendererObj.drawString("" + cloner.ability.getName(), 48, 6, 4210752);

            if (mc.thePlayer.capabilities.isCreativeMode)
            {
                int needspower = cloner.getField(2);
                this.fontRendererObj.drawString("" + needspower, 128, 46, 4210752);
            }
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
        buffer.writeBlockPos(tile.getPos());
        int diff = isShiftKeyDown()?10:1;
        if (button.id == 0)
        {
            tile.setField(1, tile.getField(1)+diff);
            buffer.writeInt(1);
            buffer.writeInt(tile.getField(1));
        }
        else if (button.id == 1)
        {
            tile.setField(1, tile.getField(1)-diff);
            buffer.writeInt(1);
            buffer.writeInt(tile.getField(1));
        }
        else if (button.id == 2)
        {
            int num = tile.getField(2);
            if(num==0)
            {
                tile.setField(2, 1);
                button.displayString = "noEnergy";
            }
            else
            {
                tile.setField(2, 0);
                button.displayString = "energy";
            }
            buffer.writeInt(2);
            buffer.writeInt(tile.getField(2));
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
