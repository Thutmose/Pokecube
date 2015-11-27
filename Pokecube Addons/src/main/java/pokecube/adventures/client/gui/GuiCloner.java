package pokecube.adventures.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.blocks.cloner.TileEntityCloner;

public class GuiCloner extends GuiContainer {
    private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("textures/gui/container/crafting_table.png");

    public GuiCloner(InventoryPlayer inventory, TileEntityCloner tile)
    {
        super(new ContainerCloner(inventory, tile));
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 28, 6, 4210752);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2, 4210752);
        ContainerCloner container = (ContainerCloner) inventorySlots;
        if(container.worldObj != null)
        {
            TileEntity te = container.worldObj.getTileEntity(container.pos);

            if (te == null) return;

            TileEntityCloner cloner = (TileEntityCloner) te;

            int energy = cloner.getField(0);
            this.fontRendererObj.drawString(""+energy, 128, 6, 4210752);
            
        }
    }

    @Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(craftingTableGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
    }
}
