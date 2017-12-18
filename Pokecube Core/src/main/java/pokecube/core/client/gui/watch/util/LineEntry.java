package pokecube.core.client.gui.watch.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.util.text.ITextComponent;

public class LineEntry implements IGuiListEntry
{
    public static interface IClickListener
    {
        default boolean handleClick(ITextComponent component)
        {
            return false;
        }

        default void handleHovor(ITextComponent component, int x, int y)
        {

        }

    }

    final int                   y0;
    final int                   y1;
    final FontRenderer          fontRender;
    final int                   colour;
    public final ITextComponent line;
    private IClickListener      listener = new IClickListener()
                                         {
                                         };

    public LineEntry(int y0, int y1, FontRenderer fontRender, ITextComponent line, int default_colour)
    {
        this.y0 = y0;
        this.y1 = y1;
        this.fontRender = fontRender;
        this.line = line;
        this.colour = default_colour;
    }

    public LineEntry setClickListner(IClickListener listener)
    {
        if (listener == null) listener = new IClickListener()
        {
        };
        this.listener = listener;
        return this;
    }

    @Override
    public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
    {

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
            boolean isSelected, float partialTicks)
    {
        if (y < y0) return;
        if (y + fontRender.FONT_HEIGHT > y1) return;
        y -= fontRender.FONT_HEIGHT / 2;
        fontRender.drawString(line.getFormattedText(), x, y, colour);
        int dx = fontRender.getStringWidth(line.getFormattedText());
        int relativeX = mouseX - x;
        int relativeY = mouseY - y;
        if (relativeY <= fontRender.FONT_HEIGHT && relativeX >= 0 && relativeX <= dx && (relativeY) > 0)
        {
            listener.handleHovor(line, x, y);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        int dx = fontRender.getStringWidth(line.getFormattedText());
        if (relativeY <= (y1 - y0) && relativeX >= 0 && relativeX <= dx && (mouseY - y1) < -1)
        {
            listener.handleClick(line);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

}
