package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.interfaces.IPokemob;

public class GuiPokemobAI extends GuiContainer
{
    final GuiPokemob parentScreen;
    final IInventory playerInventory;
    final IInventory pokeInventory;
    final IPokemob   pokemob;
    final Entity     entity;
    private float    yRenderAngle = 10;
    private float    xRenderAngle = 0;

    public GuiPokemobAI(IInventory playerInv, IPokemob pokemob, GuiPokemob parentScreen)
    {
        super(new ContainerPokemob(playerInv, pokemob.getPokemobInventory(), pokemob, false));
        this.pokemob = pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = pokemob.getPokemobInventory();
        this.entity = pokemob.getEntity();
        this.parentScreen = parentScreen;
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
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = width / 2 - 10;
        int yOffset = height / 2 - 77;
        buttonList.add(new GuiButton(0, xOffset + 60, yOffset, 30, 10, "Inv"));
        yOffset += 12;
        xOffset += 2;
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            buttonList.add(new GuiButton(i + 1, xOffset, yOffset + 10 * i, 40, 10, AIRoutine.values()[i] + ""));
        }

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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(this.pokeInventory.hasCustomName() ? this.pokeInventory.getName()
                : I18n.format(this.pokeInventory.getName(), new Object[0]), 8, 6, 4210752);
        this.fontRendererObj.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
                : I18n.format(this.playerInventory.getName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
        int xOffset = xSize / 2 + 32;
        int yOffset = ySize / 2 - 65;
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            boolean enabled = pokemob.isRoutineEnabled(AIRoutine.values()[i]);
            int colour = enabled ? 0xFF00FF00 : 0xFFFF0000;
            this.drawGradientRect(xOffset, yOffset + 10 * i, xOffset + 48, yOffset + 10 + 10 * i, colour, colour);
            this.drawHorizontalLine(xOffset, xOffset + 47, yOffset + 10 * i, 0xFF000000);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 0)
        {
            Minecraft.getMinecraft().displayGuiScreen(parentScreen);
        }
        else
        {
            int index = guibutton.id - 1;
            AIRoutine routine = AIRoutine.values()[index];
            boolean state = !pokemob.isRoutineEnabled(routine);
            pokemob.setRoutineState(routine, state);
            PacketAIRoutine.sentCommand(pokemob, routine, state);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        yRenderAngle = -entity.rotationYaw + 45;
        xRenderAngle = 0;
        GuiPokemob.renderMob(pokemob, k, l, xSize, ySize, xRenderAngle, yRenderAngle, 0, 1);
    }
}
