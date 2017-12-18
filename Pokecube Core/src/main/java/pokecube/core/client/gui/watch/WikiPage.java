package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.PageButton;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;

public class WikiPage extends ListPage
{
    public static class WikiLine extends LineEntry
    {
        final int page;

        public WikiLine(int y0, int y1, FontRenderer fontRender, ITextComponent line, int page)
        {
            super(y0, y1, fontRender, line, 0);
            this.page = page;
        }
    }

    private int index = 0;

    public WikiPage(GuiPokeWatch watch)
    {
        super(watch);
        this.setTitle(I18n.format("pokewatch.title.wiki"));
    }

    @Override
    public void initList()
    {
        setList();
    }

    @Override
    public void onPageOpened()
    {
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        String next = I18n.format("tile.pc.next");
        String prev = I18n.format("tile.pc.previous");
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x + 26, y - 69, 50, 12, next, this));
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x - 76, y - 69, 50, 12, prev, this));
    }

    private void setList()
    {
        List<FreeTranslatedReward> books = Lists.newArrayList();
        for (IInspectReward reward : PokedexInspector.rewards)
        {
            if (reward instanceof FreeTranslatedReward)
            {
                books.add((FreeTranslatedReward) reward);
            }
        }
        books.sort(new Comparator<FreeTranslatedReward>()
        {
            @Override
            public int compare(FreeTranslatedReward o1, FreeTranslatedReward o2)
            {
                return o1.key.compareTo(o2.key);
            }
        });
        if (books.isEmpty()) return;
        if (index < 0) index = books.size() - 1;
        if (index >= books.size()) index = 0;

        final WikiPage thisObj = this;
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
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 20;
        int offsetY = (watch.height - 160) / 2 + 20;
        int height = 120;
        ItemStack book = books.get(index).getInfoBook(FMLClientHandler.instance().getCurrentLanguage());
        NBTTagCompound tag = book.getTagCompound();
        NBTTagList bookPages = tag.getTagList("pages", 8);
        ITextComponent line;
        for (int i = 0; i < bookPages.tagCount(); i++)
        {
            ITextComponent page = ITextComponent.Serializer.jsonToComponent(bookPages.getStringTagAt(i));
            List<ITextComponent> list = GuiUtilRenderComponents.splitText(page, 120, fontRenderer, true, true);
            for (int j = 0; j < list.size(); j++)
            {
                line = list.get(j);
                if (j < list.size() - 1 && line.getUnformattedText().trim().isEmpty())
                {
                    for (int l = j; l < list.size(); l++)
                    {
                        if (!list.get(l).getUnformattedText().trim().isEmpty() || (l == list.size() - 1))
                        {
                            if (j < l - 1) j = l - 1;
                            break;
                        }
                    }
                }
                entries.add(new WikiLine(offsetY + 4, offsetY + height + 4, fontRenderer, line, i).setClickListner(listener));
            }
        }
        list = new ScrollGui(mc, 135, height, fontRenderer.FONT_HEIGHT + 2, offsetX, offsetY, entries);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        if (button.id == 3)// Next
        {
            index++;
            setList();
        }
        else if (button.id == 4)// Previous
        {
            index--;
            setList();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int offsetX = (watch.width - 160) / 2 + 10;
        int offsetY = (watch.height - 160) / 2 + 20;
        drawRect(offsetX - 2, offsetY - 1, offsetX + 132, offsetY + 122, 0xFFFDF8EC);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
                    int page = Integer.parseInt(clickevent.getValue());
                    int max = list.getMaxScroll();
                    for (int i = 0; i < list.getSize(); i++)
                    {
                        WikiLine line = (WikiLine) list.getListEntry(i);
                        if (line.page == page)
                        {
                            int scrollTo = Math.min(max, list.slotHeight * (i - list.height / list.slotHeight));
                            list.scrollBy(scrollTo - list.getAmountScrolled());
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return super.handleComponentClick(component);
    }

    @Override
    protected void handleComponentHover(ITextComponent component, int x, int y)
    {
        super.handleComponentHover(component, x, y);
    }
}
