package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;

public class WikiPage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class Page implements IGuiListEntry
    {
        final String   page;
        final WikiPage parent;

        public Page(String page, WikiPage parent)
        {
            this.page = page;
            this.parent = parent;
        }

        @Override
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
        {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected)
        {
            ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(page);
            int n = 0;
            for (ITextComponent comp : GuiUtilRenderComponents.splitText(itextcomponent, 120, parent.fontRendererObj,
                    true, true))
            {
                int py = y + parent.fontRendererObj.FONT_HEIGHT * n++;
                if (py < (parent.watch.height - 160) / 2 + 20) continue;
                if (py > (parent.watch.height - 160) / 2 + 120) continue;
                parent.fontRendererObj.drawString(comp.getUnformattedText(), x, py, 0);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }
    }

    private int index = 0;

    public WikiPage(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    protected String getTitle()
    {
        return I18n.format("pokewatch.title.wiki");
    }

    @Override
    void initList()
    {
        setList();
    }

    protected void onPageOpened()
    {
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        String next = I18n.format("tile.pc.next");
        String prev = I18n.format("tile.pc.previous");
        this.watch.getButtons().add(new Button(watch.getButtons().size(), x + 26, y - 69, 50, 12, next));
        this.watch.getButtons().add(new Button(watch.getButtons().size(), x - 76, y - 69, 50, 12, prev));
    }

    protected void onPageClosed()
    {
        this.watch.getButtons().removeIf(new Predicate<GuiButton>()
        {
            @Override
            public boolean test(GuiButton t)
            {
                return t instanceof Button;
            }
        });
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

        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 10;
        int offsetY = (watch.height - 160) / 2 + 20;
        int height = 120;
        ItemStack book = books.get(index).getInfoBook(FMLClientHandler.instance().getCurrentLanguage());
        NBTTagCompound tag = book.getTagCompound();
        NBTTagList bookPages = tag.getTagList("pages", 8);
        for (int i = 0; i < bookPages.tagCount(); i++)
        {
            entries.add(new Page(bookPages.getStringTagAt(i), this));
        }
        list = new ScrollGui(mc, 140, height, 128, offsetX, offsetY, entries);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
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
        drawRect(offsetX - 2, offsetY - 1, offsetX + 122, offsetY + 122, 0xFFFDF8EC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
