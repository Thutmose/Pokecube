package pokecube.core.client.gui.watch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.network.packets.PacketPokedex;

public class SpawnsPage extends ListPage
{
    int last = 0;

    public SpawnsPage(GuiPokeWatch watch)
    {
        super(watch);
        this.setTitle(I18n.format("pokewatch.title.spawns"));
    }

    @Override
    public void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 5;
        int offsetY = (watch.height - 160) / 2 + 25;
        int max = fontRenderer.FONT_HEIGHT;
        int height = max * 12;
        QName local = new QName("Local_Rate");
        List<PokedexEntry> names = Lists.newArrayList(PacketPokedex.selectedLoc.keySet());
        Map<PokedexEntry, Float> rates = Maps.newHashMap();
        for (PokedexEntry e : names)
        {
            Float value = Float.parseFloat(PacketPokedex.selectedLoc.get(e).spawnRule.values.get(local));
            rates.put(e, value);
        }
        Collections.sort(names, new Comparator<PokedexEntry>()
        {
            @Override
            public int compare(PokedexEntry o1, PokedexEntry o2)
            {
                float rate1 = rates.get(o1);
                float rate2 = rates.get(o2);
                return rate1 > rate2 ? -1 : rate1 < rate2 ? 1 : 0;
            }
        });
        final SpawnsPage thisObj = this;
        IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(ITextComponent component)
            {
                return thisObj.handleComponentClick(component);
            }

            @Override
            public void handleHovor(ITextComponent component, int x, int y)
            {
                thisObj.handleComponentHover(component, x, y);
            }
        };
        for (PokedexEntry pokeEntry : names)
        {
            SpawnListEntry entry = new SpawnListEntry(this, fontRenderer, PacketPokedex.selectedLoc.get(pokeEntry),
                    pokeEntry, height, height, offsetY);
            List<LineEntry> lines = entry.getLines(listener);
            int num = 4;
            ITextComponent water0 = new TextComponentString(I18n.format("pokewatch.spawns.water_only"));
            ITextComponent water1 = new TextComponentString(I18n.format("pokewatch.spawns.water_optional"));
            while (lines.size() > num)
            {
                ITextComponent comp = lines.get(1).line;
                if (comp.getUnformattedText().trim().equals(water0.getUnformattedText().trim()))
                {
                    num++;
                    continue;
                }
                if (comp.getUnformattedText().trim().equals(water1.getUnformattedText().trim()))
                {
                    num++;
                    continue;
                }
                lines.remove(1);
            }

            entries.addAll(lines);
        }
        list = new ScrollGui(mc, 151, height, max, offsetX, offsetY, entries);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (last != PacketPokedex.selectedLoc.size())
        {
            initList();
            last = PacketPokedex.selectedLoc.size();
        }
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 17;
        drawCenteredString(fontRenderer, I18n.format("pokewatch.spawns.info"), x, y, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        PokedexEntry e = Database.getEntry(component.getUnformattedComponentText());
        if (e != null)
        {
            PacketPokedex.updateWatchEntry(e);
            watch.pages.get(watch.index).onPageClosed();
            watch.index = 1;
            GuiPokeWatch.lastPage = 1;
            PokemobInfoPage page = (PokemobInfoPage) watch.pages.get(watch.index);
            page.initPages(null);
            watch.pages.get(watch.index).onPageOpened();
        }
        return super.handleComponentClick(component);
    }

    @Override
    protected void handleComponentHover(ITextComponent component, int x, int y)
    {
        super.handleComponentHover(component, x, y);
    }
}
