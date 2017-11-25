package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.helper.ScrollGui;

public class StatsPage extends ListPage
{

    public StatsPage(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    protected String getTitle()
    {
        return I18n.format("pokewatch.title.stats");
    }

    @Override
    void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 10;
        int offsetY = (watch.height - 160) / 2 + 20;
        int height = 120;
        list = new ScrollGui(mc, 140, height, 10, offsetX, offsetY, entries);
    }

}
