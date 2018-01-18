package pokecube.core.client.gui.watch.progress;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.FontRenderer;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.WatchPage;

public abstract class Progress extends WatchPage
{
    protected int          caught0;
    protected int          caught1;
    protected int          hatched0;
    protected int          hatched1;
    protected int          killed0;
    protected int          killed1;

    protected List<String> lines = Lists.newArrayList();

    protected FontRenderer fontRender;

    public Progress(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.fontRender = fontRenderer;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 30;
        int dy = 0;
        int colour = 0xFFFFFFFF;
        for (String s : lines)
        {
            this.drawCenteredString(fontRender, s, x, y + dy, colour);
            dy += fontRender.FONT_HEIGHT;
            if (s.isEmpty()) dy -= fontRender.FONT_HEIGHT / 1.5f;
        }
    }

}
