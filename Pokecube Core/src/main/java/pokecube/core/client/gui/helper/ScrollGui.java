package pokecube.core.client.gui.helper;

import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public class ScrollGui extends GuiListExtended
{
    final List<IGuiListEntry> entries;
    /** Whether the gui will scroll smoothly, if false, there is no scroll bar
     * and it will scroll in steps. */
    public boolean            scrollBar = true;

    public ScrollGui(Minecraft mcIn, int widthIn, int heightIn, int slotHeightIn, int offsetX, int offsetY,
            List<IGuiListEntry> entries)
    {
        super(mcIn, widthIn, heightIn, 64, 128, slotHeightIn);
        this.left = offsetX;
        this.right = this.left + this.width;
        this.top = offsetY;
        this.bottom = this.top + this.height;
        this.entries = entries;
    }

    @Override
    public IGuiListEntry getListEntry(int index)
    {
        return entries.get(index);
    }

    @Override
    public int getSize()
    {
        return entries.size();
    }

    @Override
    protected int getScrollBarX()
    {
        return this.left + this.width - 6;
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
    public int getSlotIndexFromScreenCoords(int posX, int posY)
    {
        int i = this.left + this.width / 2 - this.getListWidth() / 2;
        int j = this.left + this.width / 2 + this.getListWidth() / 2;
        int k = posY - this.top - this.headerPadding + (int) this.amountScrolled;
        int l = k / this.slotHeight;
        return posX < this.getScrollBarX() && posX >= i && posX <= j && l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
    }

    @Override
    public int getMaxScroll()
    {
        return Math.max(0, this.getContentHeight() - (this.bottom - this.top));
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
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            // Forge: background rendering moved into separate method.
            // this.drawContainerBackground(tessellator);
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int) this.amountScrolled;
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            int j1 = this.getMaxScroll();
            if (scrollBar && j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
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
                vertexbuffer.pos((double) j, (double) l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                vertexbuffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexbuffer.pos((double) i, (double) (l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255)
                        .endVertex();
                vertexbuffer.pos((double) (j - 1), (double) (l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D)
                        .color(192, 192, 192, 255).endVertex();
                vertexbuffer.pos((double) (j - 1), (double) l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255)
                        .endVertex();
                vertexbuffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
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
                            this.scrollMultiplier = -1.0F * (scrollBar ? 1 : 0);
                            int k1 = this.getMaxScroll();

                            if (k1 < 1)
                            {
                                k1 = 1;
                            }

                            int l1 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
                                    / (float) this.getContentHeight());
                            l1 = MathHelper.clamp(l1, 32, this.bottom - this.top - 8);
                            this.scrollMultiplier /= (float) (this.bottom - this.top - l1) / (float) k1;
                        }
                        else
                        {
                            this.scrollMultiplier = 1.0F * (scrollBar ? 1 : 0);
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
                    i2 = scrollBar ? -1 : -slotHeight;
                }
                else if (i2 < 0)
                {
                    i2 = scrollBar ? 1 : slotHeight;
                }

                this.amountScrolled += (float) (i2 * (this.slotHeight));
            }
        }
    }

    @Override
    /** Gets the width of the list */
    public int getListWidth()
    {
        return this.width;
    }

    @Override
    /** Overlays the background to hide scrolled items */
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        // No background needed here
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator)
    {
        // No background needed here
    }

}