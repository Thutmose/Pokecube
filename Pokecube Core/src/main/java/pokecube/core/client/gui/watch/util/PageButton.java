package pokecube.core.client.gui.watch.util;

import net.minecraft.client.gui.GuiButton;

public class PageButton extends GuiButton
{
    public final WatchPage page;

    public PageButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, WatchPage page)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.page = page;
    }
}
