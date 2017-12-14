package pokecube.core.client.gui.watch.util;

import java.io.IOException;
import java.util.function.Predicate;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class WatchPage extends GuiScreen
{
    public final GuiPokeWatch watch;

    public WatchPage(GuiPokeWatch watch)
    {
        this.watch = watch;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.mc = watch.mc;
        this.fontRenderer = this.mc.fontRenderer;
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void onPageOpened()
    {
    }

    public void onPageClosed()
    {
        final WatchPage thisPage = this;
        this.watch.getButtons().removeIf(new Predicate<GuiButton>()
        {
            @Override
            public boolean test(GuiButton t)
            {
                return t instanceof PageButton && ((PageButton) t).page == thisPage;
            }
        });
    }

    public abstract String getTitle();
}
