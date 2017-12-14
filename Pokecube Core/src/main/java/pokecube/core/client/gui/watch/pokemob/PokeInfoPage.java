package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.interfaces.IPokemob;

public abstract class PokeInfoPage extends WatchPage
{
    final IPokemob pokemob;

    public PokeInfoPage(GuiPokeWatch watch, IPokemob pokemob, String title)
    {
        super(watch);
        this.pokemob = pokemob;
        setTitle(I18n.format("pokewatch.title.pokeinfo." + title));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawInfo(mouseX, mouseY, partialTicks);
    }

    abstract void drawInfo(int mouseX, int mouseY, float partialTicks);

}
