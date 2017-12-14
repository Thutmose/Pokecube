package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketPokedex;

public class Spawns extends ListPage
{

    int last = 0;

    public Spawns(GuiPokeWatch watch, IPokemob pokemob)
    {
        super(watch, pokemob, "spawns");
    }

    @Override
    void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 45;
        int offsetY = (watch.height - 160) / 2 + 27;
        int height = 110;
        int max = fontRenderer.FONT_HEIGHT;
        for (SpawnBiomeMatcher matcher : PacketPokedex.selectedMob)
        {
            SpawnListEntry entry = new SpawnListEntry(this, fontRenderer, matcher, null, 100, height, offsetY);
            entries.addAll(entry.getLines(null));
        }
        list = new ScrollGui(mc, 110, 10 * max, max, offsetX, offsetY, entries);
    }

    @Override
    void drawInfo(int mouseX, int mouseY, float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (last != PacketPokedex.selectedMob.size())
        {
            initList();
            last = PacketPokedex.selectedMob.size();
        }
    }

}
