package pokecube.core.client.gui.watch;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.progress.GlobalProgress;
import pokecube.core.client.gui.watch.progress.PerMobProgress;
import pokecube.core.client.gui.watch.progress.PerTypeProgress;
import pokecube.core.client.gui.watch.util.PageButton;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.client.gui.watch.util.WatchPage;

public class ProgressPage extends PageWithSubPages
{

    public ProgressPage(GuiPokeWatch watch)
    {
        super(watch);
        setTitle(I18n.format("pokewatch.progress.main.title"));
    }

    @Override
    public void preSubOpened()
    {
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        String next = ">";
        String prev = "<";
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x + 64, y - 70, 12, 12, next, this));
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x - 76, y - 70, 12, 12, prev, this));
        pages.clear();
        this.pages.add(new GlobalProgress(watch));
        this.pages.add(new PerTypeProgress(watch));
        this.pages.add(new PerMobProgress(watch));
        for (WatchPage page : pages)
        {
            page.initGui();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 3)// Next
        {
            changePage(index + 1);
        }
        else if (button.id == 4)// Previous
        {
            changePage(index - 1);
        }
        super.actionPerformed(button);
    }

    @Override
    public void prePageDraw(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
        drawCenteredString(fontRenderer, getTitle(), x, y, 0xFF78C850);
        drawCenteredString(fontRenderer, pages.get(index).getTitle(), x, y + 10, 0xFF78C850);
    }
}
