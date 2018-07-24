package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.WatchPage;

public class StartPage extends ListPage
{
    private static class PageEntry implements IGuiListEntry
    {
        final WatchPage page;
        final GuiButton button;
        final int       offsetY;
        final int       guiHeight;

        public PageEntry(WatchPage page, int offsetY, int guiHeight)
        {
            this.page = page;
            button = new GuiButton(0, 0, 0, 140, 20, this.page.getTitle());
            this.offsetY = offsetY;
            this.guiHeight = guiHeight;
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            boolean fits = true;
            button.y = y - 0;
            button.x = x - 2;
            fits = button.y >= offsetY;
            fits = fits && button.y + button.height <= offsetY + guiHeight;
            if (fits)
            {
                button.drawButton(page.mc, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            boolean fits = true;
            fits = button.y >= offsetY;
            fits = fits && button.y + button.height <= offsetY + guiHeight;
            if (fits)
            {
                button.playPressSound(page.mc.getSoundHandler());
                // Index plus 1 as 0 is the start page, and no button for it.
                page.watch.pages.get(page.watch.index).onPageClosed();
                page.watch.index = slotIndex + 1;
                GuiPokeWatch.lastPage = page.watch.index;
                page.watch.pages.get(page.watch.index).onPageOpened();
            }
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }

    }

    public StartPage(GuiPokeWatch watch)
    {
        super(watch);
        this.setTitle(I18n.format("pokewatch.title.start"));
    }

    @Override
    public void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 10;
        int offsetY = (watch.height - 160) / 2 + 20;
        int height = 100;
        for (WatchPage page : watch.pages)
        {
            if (!(page instanceof StartPage)) entries.add(new PageEntry(page, offsetY, height + 5));
        }
        list = new ScrollGui(mc, 146, height, 20, offsetX, offsetY, entries);
    }

}
