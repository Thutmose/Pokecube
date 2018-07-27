package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketPokemobGui;

public class GuiPokemobAI extends GuiContainer
{
    final IInventory playerInventory;
    final IInventory pokeInventory;
    final IPokemob   pokemob;
    final Entity     entity;
    GuiListExtended  list;
    private float    yRenderAngle = 10;
    private float    xRenderAngle = 0;

    public GuiPokemobAI(IInventory playerInv, IPokemob pokemob)
    {
        super(new ContainerPokemob(playerInv, pokemob.getPokemobInventory(), pokemob, false));
        this.pokemob = pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = pokemob.getPokemobInventory();
        this.entity = pokemob.getEntity();
    }

    final List<GuiTextField> textInputs = Lists.newArrayList();

    @Override
    /** Draws the background (i is always 0 as of 1.2.2) */
    public void drawBackground(int tint)
    {
        super.drawBackground(tint);
    }

    @Override
    public void drawWorldBackground(int tint)
    {
        super.drawWorldBackground(tint);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = width / 2 - 10;
        int yOffset = height / 2 - 77;
        buttonList.add(new GuiButton(0, xOffset + 60, yOffset, 30, 10, "Inv"));
        buttonList.add(new GuiButton(1, xOffset + 30, yOffset, 30, 10, "ST"));
        buttonList.add(new GuiButton(2, xOffset + 00, yOffset, 30, 10, "RT"));
        yOffset += 13;
        xOffset += 2;
        final List<IGuiListEntry> entries = Lists.newArrayList();
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            String name = AIRoutine.values()[i].toString();
            if (name.length() > 6) name = name.substring(0, 6);
            entries.add(new Entry(new GuiButton(i, xOffset, yOffset + i * 10, 40, 10, name), yOffset, mc, pokemob));
        }
        list = new ScrollGui(mc, 88, 50, 10, xOffset, yOffset, entries);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
    }

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
        try
        {
            super.mouseClicked(x, y, mouseButton);
            this.list.mouseClicked(x, y, mouseButton);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Called when a mouse button is released. */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(this.pokeInventory.hasCustomName() ? this.pokeInventory.getName()
                : I18n.format(this.pokeInventory.getName(), new Object[0]), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
                : I18n.format(this.playerInventory.getName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 0)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, entity.getEntityId());
        }
        else if (guibutton.id == 1)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, entity.getEntityId());
        }
        else if (guibutton.id == 2)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, entity.getEntityId());
        }
        else
        {
            this.list.actionPerformed(guibutton);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        this.list.drawScreen(i, j, f);
        this.renderHoveredToolTip(i, j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        yRenderAngle = -entity.rotationYaw + 45;
        xRenderAngle = 0;
        GuiPokemob.renderMob(pokemob, k, l, xSize, ySize, xRenderAngle, yRenderAngle, 0, 1);
    }

    private static class Entry implements IGuiListEntry
    {
        final GuiButton wrapped;
        final int       offsetY;
        final Minecraft mc;
        final IPokemob  pokemob;

        public Entry(GuiButton guiButton, int offsetY, Minecraft mc, IPokemob pokemob)
        {
            this.wrapped = guiButton;
            this.offsetY = offsetY;
            this.mc = mc;
            this.pokemob = pokemob;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            boolean fits = true;
            wrapped.x = x - 2;
            wrapped.y = y - 4;
            fits = wrapped.y >= offsetY;
            fits = fits && wrapped.y + 10 <= offsetY + 50;
            if (fits)
            {
                wrapped.drawButton(mc, mouseX, mouseY, partialTicks);
                AIRoutine routine = AIRoutine.values()[slotIndex];
                boolean state = pokemob.isRoutineEnabled(routine);
                Gui.drawRect(wrapped.x + 41, wrapped.y + 1, wrapped.x + 80, wrapped.y + 10,
                        state ? 0xFF00FF00 : 0xFFFF0000);
                Gui.drawRect(wrapped.x, wrapped.y + 10, wrapped.x + 40, wrapped.y + 11, 0xFF000000);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            boolean fits = true;
            fits = wrapped.y >= offsetY;
            fits = fits && wrapped.y + 10 <= offsetY + 52;
            if (fits)
            {
                AIRoutine routine = AIRoutine.values()[slotIndex];
                boolean state = !pokemob.isRoutineEnabled(routine);
                pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(pokemob, routine, state);
            }
            return fits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {

        }

    }
}
