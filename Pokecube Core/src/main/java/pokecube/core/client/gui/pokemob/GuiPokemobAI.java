package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.MathHelper;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketAIRoutine;

public class GuiPokemobAI extends GuiContainer
{
    final GuiPokemob parentScreen;
    final IInventory playerInventory;
    final IInventory pokeInventory;
    final IPokemob   pokemob;
    final Entity     entity;
    GuiListExtended  list;
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
        yOffset += 13;
        xOffset += 2;

        final int offsetX = xOffset;
        final int offsetY = yOffset;

        class Entry implements IGuiListEntry
        {
            final GuiButton wrapped;

            public Entry(GuiButton guiButton)
            {
                this.wrapped = guiButton;
            }

            @Override
            public void setSelected(int i, int j, int k)
            {
            }

            @Override
            public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                    boolean isSelected)
            {
                boolean fits = true;
                wrapped.xPosition = x - 2;
                wrapped.yPosition = y - 4;
                fits = wrapped.yPosition >= offsetY;
                fits = fits && wrapped.yPosition + 10 <= offsetY + 50;
                if (fits)
                {
                    wrapped.drawButton(mc, mouseX, mouseY);
                    AIRoutine routine = AIRoutine.values()[slotIndex];
                    boolean state = pokemob.isRoutineEnabled(routine);
                    Gui.drawRect(wrapped.xPosition + 41, wrapped.yPosition + 1, wrapped.xPosition + 80,
                            wrapped.yPosition + 10, state ? 0xFF00FF00 : 0xFFFF0000);
                    Gui.drawRect(wrapped.xPosition, wrapped.yPosition + 10, wrapped.xPosition + 40,
                            wrapped.yPosition + 11, 0xFF000000);
                }
            }

            @Override
            public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
                    int relativeY)
            {
                boolean fits = true;
                fits = wrapped.yPosition >= offsetY;
                fits = fits && wrapped.yPosition + 10 <= offsetY + 52;
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

        }
        final List<Entry> entries = Lists.newArrayList();
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            String name = AIRoutine.values()[i].toString();
            if (name.length() > 6) name = name.substring(0, 6);
            entries.add(new Entry(new GuiButton(i, xOffset, yOffset + i * 10, 40, 10, name)));
        }

        class Scroll extends GuiListExtended
        {
            public Scroll(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
            {
                super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
                this.height = 50;
                this.width = 83;
                this.left = offsetX;
                this.right = this.left + this.width;
                this.top = offsetY;
                this.bottom = this.top + this.height;
            }

            @Override
            public IGuiListEntry getListEntry(int index)
            {
                return entries.get(index);
            }

            @Override
            protected int getSize()
            {
                return entries.size();
            }

            @Override
            protected int getScrollBarX()
            {
                return this.left + this.width;
            }

            @Override
            /** The element in the slot that was clicked, boolean for whether it
             * was double clicked or not */
            protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
            {
                super.elementClicked(slotIndex, isDoubleClick, mouseX, mouseY);
            }

            @Override
            /** Returns true if the element passed in is currently selected */
            protected boolean isSelected(int slotIndex)
            {
                return super.isSelected(slotIndex);
            }

            @Override
            protected void drawBackground()
            {
                super.drawBackground();
            }

            @Override
            protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn,
                    int mouseYIn)
            {
                super.drawSlot(entryID, insideLeft, yPos, insideSlotHeight, mouseXIn, mouseYIn);
            }

            @Override
            protected void updateItemPos(int entryID, int insideLeft, int yPos)
            {
                super.updateItemPos(entryID, insideLeft, yPos);
            }

            @Override
            public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
            {
                if (this.isMouseYWithinSlotBounds(mouseY))
                {
                    int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
                    if (i >= 0)
                    {
                        int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                        int k = this.top - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
                        int l = mouseX - j;
                        int i1 = mouseY - k;
                        if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, l, i1))
                        {
                            this.setEnabled(false);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean mouseReleased(int p_148181_1_, int p_148181_2_, int p_148181_3_)
            {
                for (int i = 0; i < this.getSize(); ++i)
                {
                    int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                    int k = this.top - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
                    int l = p_148181_1_ - j;
                    int i1 = p_148181_2_ - k;
                    this.getListEntry(i).mouseReleased(i, p_148181_1_, p_148181_2_, p_148181_3_, l, i1);
                }

                this.setEnabled(true);
                return false;
            }

            @Override
            /** Handles drawing a list's header row. */
            protected void drawListHeader(int insideLeft, int insideTop, Tessellator tessellatorIn)
            {
                super.drawListHeader(insideLeft, insideTop, tessellatorIn);
            }

            @Override
            protected void clickedHeader(int p_148132_1_, int p_148132_2_)
            {
                super.clickedHeader(p_148132_1_, p_148132_2_);
            }

            @Override
            protected void renderDecorations(int mouseXIn, int mouseYIn)
            {
                super.renderDecorations(mouseXIn, mouseYIn);
            }

            @Override
            public int getSlotIndexFromScreenCoords(int posX, int posY)
            {
                int i = this.left + this.width / 2 - this.getListWidth() / 2;
                int j = this.left + this.width / 2 + this.getListWidth() / 2;
                int k = posY - this.top - this.headerPadding + (int) this.amountScrolled;
                int l = k / this.slotHeight;
                return posX < this.getScrollBarX() && posX >= i && posX <= j && l >= 0 && k >= 0 && l < this.getSize()
                        ? l : -1;
            }

            @Override
            /** Stop the thing from scrolling out of bounds */
            protected void bindAmountScrolled()
            {
                this.amountScrolled = MathHelper.clamp_float(this.amountScrolled, 0.0F, (float) this.getMaxScroll());
            }

            @Override
            public int getMaxScroll()
            {
                return Math.max(0, this.getContentHeight() - (this.bottom - this.top));
            }

            @Override
            /** Returns the amountScrolled field as an integer. */
            public int getAmountScrolled()
            {
                return (int) this.amountScrolled;
            }

            @Override
            public boolean isMouseYWithinSlotBounds(int p_148141_1_)
            {
                return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= this.left
                        && this.mouseX <= this.right;
            }

            @Override
            /** Scrolls the slot by the given amount. A positive value scrolls
             * down, and a negative value scrolls up. */
            public void scrollBy(int amount)
            {
                super.scrollBy(amount);
            }

            @Override
            public void actionPerformed(GuiButton button)
            {
                super.actionPerformed(button);
            }

            @Override
            public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
            {
                if (this.visible)
                {
                    this.mouseX = mouseXIn;
                    this.mouseY = mouseYIn;
                    this.drawBackground();
                    int i = this.getScrollBarX();
                    int j = i + 6;
                    this.bindAmountScrolled();
                    GlStateManager.disableLighting();
                    GlStateManager.disableFog();
                    Tessellator tessellator = Tessellator.getInstance();
                    VertexBuffer vertexbuffer = tessellator.getBuffer();
                    // Forge: background rendering moved into separate method.
                    // this.drawContainerBackground(tessellator);
                    int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                    int l = this.top + 4 - (int) this.amountScrolled;
                    this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                            GlStateManager.DestFactor.ONE);
                    GlStateManager.disableAlpha();
                    GlStateManager.shadeModel(7425);
                    GlStateManager.disableTexture2D();
                    int j1 = this.getMaxScroll();
                    if (j1 > 0)
                    {
                        int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                        k1 = MathHelper.clamp_int(k1, 32, this.bottom - this.top - 8);
                        int l1 = (int) this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                        if (l1 < this.top)
                        {
                            l1 = this.top;
                        }
                        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        vertexbuffer.pos((double) i, (double) (l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255)
                                .endVertex();
                        vertexbuffer.pos((double) j, (double) (l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255)
                                .endVertex();
                        vertexbuffer.pos((double) j, (double) l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255)
                                .endVertex();
                        vertexbuffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255)
                                .endVertex();
                        tessellator.draw();
                        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        vertexbuffer.pos((double) i, (double) (l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D)
                                .color(192, 192, 192, 255).endVertex();
                        vertexbuffer.pos((double) (j - 1), (double) (l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D)
                                .color(192, 192, 192, 255).endVertex();
                        vertexbuffer.pos((double) (j - 1), (double) l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255)
                                .endVertex();
                        vertexbuffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255)
                                .endVertex();
                        tessellator.draw();
                    }
                    this.renderDecorations(mouseXIn, mouseYIn);
                    GlStateManager.enableTexture2D();
                    GlStateManager.shadeModel(7424);
                    GlStateManager.enableAlpha();
                    GlStateManager.disableBlend();
                }
            }

            @Override
            public void handleMouseInput()
            {
                if (this.isMouseYWithinSlotBounds(this.mouseY))
                {
                    if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top
                            && this.mouseY <= this.bottom)
                    {
                        int i = (this.width - this.getListWidth()) / 2;
                        int j = (this.width + this.getListWidth()) / 2;
                        int k = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                        int l = k / this.slotHeight;

                        if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0)
                        {
                            this.elementClicked(l, false, this.mouseX, this.mouseY);
                            this.selectedElement = l;
                        }
                        else if (this.mouseX >= i && this.mouseX <= j && k < 0)
                        {
                            this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int) this.amountScrolled - 4);
                        }
                    }

                    if (Mouse.isButtonDown(0) && this.getEnabled())
                    {
                        if (this.initialClickY == -1)
                        {
                            boolean flag1 = true;

                            if (this.mouseY >= this.top && this.mouseY <= this.bottom)
                            {
                                int j2 = (this.width - this.getListWidth()) / 2;
                                int k2 = (this.width + this.getListWidth()) / 2;
                                int l2 = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                                int i1 = l2 / this.slotHeight;

                                if (i1 < this.getSize() && this.mouseX >= j2 && this.mouseX <= k2 && i1 >= 0 && l2 >= 0)
                                {
                                    boolean flag = i1 == this.selectedElement
                                            && Minecraft.getSystemTime() - this.lastClicked < 250L;
                                    this.elementClicked(i1, flag, this.mouseX, this.mouseY);
                                    this.selectedElement = i1;
                                    this.lastClicked = Minecraft.getSystemTime();
                                }
                                else if (this.mouseX >= j2 && this.mouseX <= k2 && l2 < 0)
                                {
                                    this.clickedHeader(this.mouseX - j2,
                                            this.mouseY - this.top + (int) this.amountScrolled - 4);
                                    flag1 = false;
                                }

                                int i3 = this.getScrollBarX();
                                int j1 = i3 + 6;

                                if (this.mouseX >= i3 && this.mouseX <= j1)
                                {
                                    this.scrollMultiplier = -1.0F;
                                    int k1 = this.getMaxScroll();

                                    if (k1 < 1)
                                    {
                                        k1 = 1;
                                    }

                                    int l1 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
                                            / (float) this.getContentHeight());
                                    l1 = MathHelper.clamp_int(l1, 32, this.bottom - this.top - 8);
                                    this.scrollMultiplier /= (float) (this.bottom - this.top - l1) / (float) k1;
                                }
                                else
                                {
                                    this.scrollMultiplier = 1.0F;
                                }

                                if (flag1)
                                {
                                    this.initialClickY = this.mouseY;
                                }
                                else
                                {
                                    this.initialClickY = -2;
                                }
                            }
                            else
                            {
                                this.initialClickY = -2;
                            }
                        }
                        else if (this.initialClickY >= 0)
                        {
                            this.amountScrolled -= (float) (this.mouseY - this.initialClickY) * this.scrollMultiplier;
                            this.initialClickY = this.mouseY;
                        }
                    }
                    else
                    {
                        this.initialClickY = -1;
                    }

                    int i2 = Mouse.getEventDWheel();

                    if (i2 != 0)
                    {
                        if (i2 > 0)
                        {
                            i2 = -1;
                        }
                        else if (i2 < 0)
                        {
                            i2 = 1;
                        }

                        this.amountScrolled += (float) (i2 * (this.slotHeight));
                    }
                }
            }

            @Override
            /** Gets the width of the list */
            public int getListWidth()
            {
                return this.width;// super.getListWidth();
            }

            @Override
            /** Draws the selection box around the selected slot element. */
            protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn)
            {
                super.drawSelectionBox(insideLeft, insideTop, mouseXIn, mouseYIn);
            }

            @Override
            /** Overlays the background to hide scrolled items */
            protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
            {
                // super.overlayBackground(startY, endY, startAlpha, endAlpha);
            }

            @Override
            public int getSlotHeight()
            {
                return super.getSlotHeight();
            }

            @Override
            protected void drawContainerBackground(Tessellator tessellator)
            {
                // super.drawContainerBackground(tessellator);
            }

            /** Return the height of the content being scrolled */
            protected int getContentHeight()
            {
                return super.getContentHeight();
            }

        }

        list = new Scroll(mc, 128, 64, 64, 128, 10);
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
            Minecraft.getMinecraft().displayGuiScreen(parentScreen);
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
}
