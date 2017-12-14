package pokecube.core.client.gui.watch.util;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.interfaces.PokecubeMod;

public abstract class PageWithSubPages extends WatchPage
{
    protected List<WatchPage> pages = Lists.newArrayList();
    protected int             index = 0;

    public PageWithSubPages(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    public void onPageClosed()
    {
        super.onPageClosed();
        preSubClosed();
        try
        {
            pages.get(index).onPageClosed();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with page " + pages.get(index).getTitle(), e);
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        preSubOpened();
        try
        {
            pages.get(index).initGui();
            pages.get(index).onPageOpened();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with page " + pages.get(index).getTitle(), e);
        }
    }

    public void changePage(int newIndex)
    {
        pages.get(index).onPageClosed();
        index = newIndex;
        if (index < 0) index = pages.size() - 1;
        if (index > pages.size() - 1) index = 0;
        pages.get(index).onPageOpened();
    }

    public void preSubClosed()
    {

    }

    public void preSubOpened()
    {

    }

    public void prePageDraw(int mouseX, int mouseY, float partialTicks)
    {

    }

    public void postPageDraw(int mouseX, int mouseY, float partialTicks)
    {

    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        pages.get(index).handleMouseInput();
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        pages.get(index).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        pages.get(index).mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        pages.get(index).keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        pages.get(index).mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        pages.get(index).actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        prePageDraw(mouseX, mouseY, partialTicks);
        pages.get(index).drawScreen(mouseX, mouseY, partialTicks);
        postPageDraw(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
