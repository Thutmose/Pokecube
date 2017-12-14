package pokecube.core.client.gui.watch.util;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class ListPage extends WatchPage
{
    protected ScrollGui list;

    public ListPage(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        initList();
    }

    public abstract void initList();

    public void drawTitle(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
        drawCenteredString(fontRenderer, getTitle(), x, y, 0xFFFFFFFF);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        list.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        list.actionPerformed(button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        list.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawTitle(mouseX, mouseY, partialTicks);
        list.drawScreen(mouseX, mouseY, partialTicks);
    }
}
