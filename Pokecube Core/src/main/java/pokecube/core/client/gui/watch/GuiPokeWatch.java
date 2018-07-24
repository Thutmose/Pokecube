package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.Tools;

public class GuiPokeWatch extends GuiScreen
{
    private static class MissingPage extends WatchPage
    {

        public MissingPage(GuiPokeWatch watch)
        {
            super(watch);
            setTitle(I18n.format("pokewatch.title.blank"));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            int x = (watch.width - 160) / 2 + 80;
            int y = (watch.height - 160) / 2 + 70;
            drawCenteredString(fontRenderer, I18n.format("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

    }

    public static final ResourceLocation           TEXTURE  = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui.png");
    public static List<Class<? extends WatchPage>> PAGELIST = Lists.newArrayList();

    static
    {
        PAGELIST.add(StartPage.class);
        PAGELIST.add(PokemobInfoPage.class);
        PAGELIST.add(WikiPage.class);
        PAGELIST.add(SpawnsPage.class);
        PAGELIST.add(ProgressPage.class);
        PAGELIST.add(TeleportsPage.class);
        PAGELIST.add(SecretBaseRadarPage.class);
    }

    public static int         lastPage = -1;

    final List<WatchPage>     pages    = Lists.newArrayList();
    public final IPokemob     pokemob;
    public final EntityPlayer player;
    int                       index    = 0;

    public GuiPokeWatch(EntityPlayer player, int startPage)
    {
        this(player);
        if (startPage >= 0 && startPage < pages.size()) index = startPage;
        else if (lastPage >= 0 && lastPage < pages.size()) index = lastPage;
        PacketPokedex.sendLocationSpawnsRequest();
    }

    public GuiPokeWatch(EntityPlayer player)
    {
        Entity entityHit = Tools.getPointedEntity(player, 16);
        pokemob = CapabilityPokemob.getPokemobFor(entityHit);
        if (pokemob != null)
        {
            PacketPokedex.sendInspectPacket(pokemob);
            PacketPokedex.updateWatchEntry(pokemob.getPokedexEntry());
        }
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
        int x = width / 2;
        int y = height / 2 - 5;
        String next = I18n.format("tile.pc.next");
        String prev = I18n.format("tile.pc.previous");
        String home = I18n.format("pokewatch.button.home");
        this.buttonList.add(new GuiButton(0, x + 26, y + 69, 50, 12, next));
        this.buttonList.add(new GuiButton(1, x - 76, y + 69, 50, 12, prev));
        this.buttonList.add(new GuiButton(2, x - 25, y + 69, 50, 12, home));

        pages.get(index).onPageOpened();
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
        try
        {
            pages.get(index).drawScreen(mouseX, mouseY, partialTicks);
        }
        catch (Exception e)
        {
            handleError(e);
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        try
        {
            pages.get(index).handleMouseInput();
        }
        catch (Exception e)
        {
            handleError(e);
        }
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
            lastPage = index;
        }
        try
        {
            pages.get(index).actionPerformed(button);
        }
        catch (Exception e)
        {
            handleError(e);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        try
        {
            pages.get(index).mouseClicked(mouseX, mouseY, mouseButton);
        }
        catch (Exception e)
        {
            handleError(e);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        try
        {
            pages.get(index).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        catch (Exception e)
        {
            handleError(e);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        try
        {
            pages.get(index).mouseReleased(mouseX, mouseY, state);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with page " + pages.get(index).getTitle(), e);
            pages.set(index, new MissingPage(this));
            pages.get(index).initGui();
            pages.get(index).onPageOpened();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        try
        {
            pages.get(index).keyTyped(typedChar, keyCode);
        }
        catch (Exception e)
        {
            handleError(e);
        }
    }

    public boolean canEdit(IPokemob pokemob)
    {
        return pokemob.getEntity().addedToChunk && (pokemob.getOwner() == player || player.capabilities.isCreativeMode);
    }

    private void handleError(Exception e)
    {
        PokecubeMod.log(Level.WARNING, "Error with page " + pages.get(index).getTitle(), e);
        try
        {
            pages.get(index).onPageClosed();
        }
        catch (Exception e1)
        {
        }
        pages.set(index, new MissingPage(this));
        pages.get(index).initGui();
        pages.get(index).onPageOpened();
    }
}
