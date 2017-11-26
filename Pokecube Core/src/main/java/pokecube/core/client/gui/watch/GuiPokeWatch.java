package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class GuiPokeWatch extends GuiScreen
{
    public static abstract class WatchPage extends GuiScreen
    {
        protected final GuiPokeWatch watch;

        public WatchPage(GuiPokeWatch watch)
        {
            this.watch = watch;
        }

        @Override
        public void initGui()
        {
            super.initGui();
            this.mc = watch.mc;
            this.fontRendererObj = watch.fontRendererObj;
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            super.actionPerformed(button);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
        {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state)
        {
            super.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            super.keyTyped(typedChar, keyCode);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        protected void onPageOpened()
        {

        }

        protected void onPageClosed()
        {

        }

        protected abstract String getTitle();
    }

    private static class MissingPage extends WatchPage
    {

        public MissingPage(GuiPokeWatch watch)
        {
            super(watch);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            int x = (watch.width - 160) / 2 + 80;
            int y = (watch.height - 160) / 2 + 70;
            drawCenteredString(fontRendererObj, I18n.format("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected String getTitle()
        {
            return I18n.format("pokewatch.title.blank");
        }

    }

    public static final ResourceLocation           TEXTURE  = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui.png");
    public static List<Class<? extends WatchPage>> PAGELIST = Lists.newArrayList();

    static
    {
        PAGELIST.add(StartPage.class);
        PAGELIST.add(TeleportsPage.class);
        PAGELIST.add(SecretBaseRadarPage.class);
        PAGELIST.add(WikiPage.class);
    }

    final List<WatchPage>     pages = Lists.newArrayList();
    public final EntityPlayer player;
    int                       index = 0;

    public GuiPokeWatch(EntityPlayer player)
    {
        this.player = player;
        for (Class<? extends WatchPage> pageClass : PAGELIST)
        {
            WatchPage page;
            try
            {
                page = pageClass.getConstructor(GuiPokeWatch.class).newInstance(this);
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                page = new MissingPage(this);
            }
            pages.add(page);
        }
    }

    public List<GuiButton> getButtons()
    {
        return buttonList;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        for (WatchPage page : pages)
            page.initGui();
        pages.get(index).onPageOpened();
        int x = width / 2;
        int y = height / 2 - 5;
        String next = I18n.format("tile.pc.next");
        String prev = I18n.format("tile.pc.previous");
        String home = I18n.format("pokewatch.button.home");
        this.buttonList.add(new GuiButton(0, x + 26, y + 69, 50, 12, next));
        this.buttonList.add(new GuiButton(1, x - 76, y + 69, 50, 12, prev));
        this.buttonList.add(new GuiButton(2, x - 25, y + 69, 50, 12, home));
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        minecraft.renderEngine.bindTexture(TEXTURE);
        int j2 = (width - 160) / 2;
        int k2 = (height - 160) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, 160, 160);
        super.drawScreen(mouseX, mouseY, partialTicks);
        pages.get(index).drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleInput() throws IOException
    {
        super.handleInput();
    }

    @Override
    public void handleKeyboardInput() throws IOException
    {
        super.handleKeyboardInput();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        pages.get(index).handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        int old = index;
        if (button.id == 0)
        {
            if (index < pages.size() - 1)
            {
                index++;
            }
            else
            {
                index = 0;
            }
        }
        if (button.id == 1)
        {
            if (index > 0)
            {
                index--;
            }
            else
            {
                index = pages.size() - 1;
            }
        }
        if (button.id == 2)
        {
            index = 0;
        }
        if (old != index)
        {
            pages.get(old).onPageClosed();
            pages.get(index).onPageOpened();
        }
        pages.get(index).actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        pages.get(index).mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        pages.get(index).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        pages.get(index).mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        pages.get(index).keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }
}
