package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;

public class Breeding extends ListPage
{
    final PokemobInfoPage parent;

    public Breeding(PokemobInfoPage parent, IPokemob pokemob)
    {
        super(parent.watch, pokemob, "breeding");
        this.parent = parent;
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }

    @Override
    void drawInfo(int mouseX, int mouseY, float partialTicks)
    {

    }

    @Override
    void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 20;
        int offsetY = (watch.height - 160) / 2 + 85;
        int height = fontRenderer.FONT_HEIGHT * 6;
        int width = 135;

        int y0 = offsetY;
        int y1 = offsetY + height;
        int colour = 0xFFFFFFFF;

        width = 111;
        int dx = 25;
        int dy = -57;
        y0 += dy;
        y1 += dy;
        offsetY += dy;
        offsetX += dx;

        final Breeding thisObj = this;
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
        ITextComponent main = new TextComponentTranslation(pokemob.getPokedexEntry().getUnlocalizedName());
        main.setStyle(new Style());
        main.getStyle().setColor(TextFormatting.GREEN);
        main.getStyle().setClickEvent(new ClickEvent(Action.CHANGE_PAGE, pokemob.getPokedexEntry().getName()));
        entries.add(new LineEntry(y0, y1, fontRenderer, main, colour).setClickListner(listener));
        for (PokedexEntry entry : pokemob.getPokedexEntry().getRelated())
        {
            main = new TextComponentTranslation(entry.getUnlocalizedName());
            main.setStyle(new Style());
            main.getStyle().setColor(TextFormatting.GREEN);
            main.getStyle().setClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getName()));
            entries.add(new LineEntry(y0, y1, fontRenderer, main, colour).setClickListner(listener));
        }
        list = new ScrollGui(mc, width, height, fontRenderer.FONT_HEIGHT, offsetX, offsetY, entries);
    }

    @Override
    public boolean handleComponentClick(ITextComponent component)
    {

        if (component != null)
        {
            ClickEvent clickevent = component.getStyle().getClickEvent();
            if (clickevent == null)
            {
                for (ITextComponent sib : component.getSiblings())
                {
                    if (sib != null && (clickevent = sib.getStyle().getClickEvent()) != null)
                    {
                        break;
                    }
                }
            }
            if (clickevent != null)
            {
                if (clickevent.getAction() == Action.CHANGE_PAGE)
                {
                    PokedexEntry entry = Database.getEntry(clickevent.getValue());
                    if (entry != null && entry != pokemob.getPokedexEntry())
                    {
                        parent.initPages(EventsHandlerClient.getRenderMob(entry, watch.player.getEntityWorld()));
                    }
                    return true;
                }
            }
        }
        return super.handleComponentClick(component);
    }
}