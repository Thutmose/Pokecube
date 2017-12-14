package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.client.gui.watch.pokemob.Breeding;
import pokecube.core.client.gui.watch.pokemob.Moves;
import pokecube.core.client.gui.watch.pokemob.PokeInfoPage;
import pokecube.core.client.gui.watch.pokemob.Spawns;
import pokecube.core.client.gui.watch.pokemob.StatsInfo;
import pokecube.core.client.gui.watch.util.PageButton;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;

public class PokemobInfoPage extends WatchPage
{

    IPokemob           pokemob;
    int                index = 0;
    GuiTextField       search;
    List<PokeInfoPage> pages = Lists.newArrayList();

    public PokemobInfoPage(GuiPokeWatch watch)
    {
        super(watch);
        this.pokemob = watch.pokemob;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = watch.width / 2 - 70;
        int y = watch.height / 2 + 53;
        search = new GuiTextField(0, fontRenderer, x, y, 140, 10);
    }

    public void initPages(IPokemob pokemob)
    {
        boolean update = false;
        if (pokemob == null)
        {
            String name = PokecubePlayerDataHandler.getCustomDataTag(watch.player).getString("WEntry");
            PokedexEntry entry = Database.getEntry(name);
            if (entry == null)
            {
                update = true;
                entry = Pokedex.getInstance().getFirstEntry();
            }
            pokemob = EventsHandlerClient.getRenderMob(entry, watch.player.getEntityWorld());
        }
        this.pokemob = pokemob;
        search.setVisible(!watch.canEdit(pokemob));
        search.setText(pokemob.getPokedexEntry().getName());
        PacketPokedex.sendSpecificSpawnsRequest(pokemob.getPokedexEntry());
        if (update) PacketPokedex.updateWatchEntry(pokemob.getPokedexEntry());
        pages.clear();
        pages.add(new StatsInfo(watch, pokemob));
        pages.add(new Moves(watch, pokemob));
        pages.add(new Spawns(watch, pokemob));
        pages.add(new Breeding(this, pokemob));
        // pages.add(new Search(this, pokemob));
        for (PokeInfoPage page : pages)
        {
            page.initGui();
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        initPages(pokemob);
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        String next = ">";
        String prev = "<";
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x + 64, y - 70, 12, 12, next, this));
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x - 76, y - 70, 12, 12, prev, this));
        if (!watch.canEdit(pokemob))
        {
            PageButton buttonNext = new PageButton(watch.getButtons().size(), x - 46, y + 4, 12, 12, next, this);
            PageButton buttonPrev = new PageButton(watch.getButtons().size() + 1, x - 76, y + 4, 12, 12, prev, this);
            PageButton formCycle = new PageButton(watch.getButtons().size() + 2, x - 65, y + 4, 20, 12, "-", this);
            this.watch.getButtons().add(buttonNext);
            this.watch.getButtons().add(buttonPrev);
            this.watch.getButtons().add(formCycle);
        }
        pages.get(index).onPageOpened();
    }

    @Override
    public void onPageClosed()
    {
        super.onPageClosed();
        pages.get(index).onPageClosed();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 3)// Next
        {
            pages.get(index).onPageClosed();
            index++;
            if (index >= pages.size()) index = 0;
            pages.get(index).onPageOpened();
        }
        else if (button.id == 4)// Previous
        {
            pages.get(index).onPageClosed();
            index--;
            if (index < 0) index = pages.size() - 1;
            pages.get(index).onPageOpened();
        }
        else if (button.id == 5)
        {
            PokedexEntry entry = pokemob.getPokedexEntry();
            int i = isShiftKeyDown() ? isCtrlKeyDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            pokemob = EventsHandlerClient.getRenderMob(entry, watch.player.getEntityWorld());
            initPages(pokemob);
        }
        else if (button.id == 6)
        {
            PokedexEntry entry = pokemob.getPokedexEntry();
            int i = isShiftKeyDown() ? isCtrlKeyDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            pokemob = EventsHandlerClient.getRenderMob(entry, watch.player.getEntityWorld());
            initPages(pokemob);
        }
        else if (button.id == 7)
        {
            // Cycle Form.
            PokedexEntry entry = pokemob.getPokedexEntry();
            PokedexEntry base = entry.base ? entry : entry.getBaseForme();
            List<PokedexEntry> forms = Lists.newArrayList(base.forms.values());
            if (!forms.contains(entry))
            {
                forms.add(0, entry);
            }
            if (!forms.contains(base))
            {
                forms.add(0, base);
            }
            int index = forms.indexOf(pokemob.getPokedexEntry());
            if (index != -1)
            {
                entry = forms.get((index + 1) % forms.size());
                initPages(pokemob.megaEvolve(entry));
            }
        }
        super.actionPerformed(button);
        pages.get(index).actionPerformed(button);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        pages.get(index).mouseClicked(mouseX, mouseY, mouseButton);
        if (search.getVisible() && !new Exception().getStackTrace()[2].getClassName()
                .equals("pokecube.core.client.gui.watch.PokemobInfoPage"))
            search.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        pages.get(index).handleMouseInput();
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        pages.get(index).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        pages.get(index).mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        pages.get(index).keyTyped(typedChar, keyCode);
        if (search.getVisible())
        {
            search.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_TAB)
            {
                // TODO autocomplete the name.
            }
            else if (keyCode == Keyboard.KEY_RETURN)
            {
                PokedexEntry entry = pokemob.getPokedexEntry();
                PokedexEntry newEntry = Database.getEntry(search.getText());
                if (newEntry != null)
                {
                    search.setText(newEntry.getName());
                    pokemob = EventsHandlerClient.getRenderMob(newEntry, watch.player.getEntityWorld());
                    initPages(pokemob);
                }
                else
                {
                    search.setText(entry.getName());
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
        drawCenteredString(fontRenderer, getTitle(), x, y, 0xFF78C850);
        drawCenteredString(fontRenderer, pages.get(index).getTitle(), x, y + 10, 0xFF78C850);
        int dx = -76;
        int dy = 10;
        int dr = 40;
        int colour = 0xFF78C850;

        // Draw a box around where pokemob displays
        drawVerticalLine(x + dx, y + dy, y + dy + dr, colour);
        drawVerticalLine(x + dx + dr, y + dy, y + dy + dr, colour);
        drawHorizontalLine(x + dx, x + dx + dr, y + dy, colour);
        drawHorizontalLine(x + dx, x + dx + dr, y + dy + dr, colour);

        // Draw Pokemob
        if (pokemob != null)
        {
            if (!pokemob.getEntity().addedToChunk)
            {
                EntityLivingBase player = watch.player;
                EntityTools.copyEntityTransforms(pokemob.getEntity(), player);
            }

            dx = -110;
            dy = -20;
            // Draw the actual pokemob
            GuiPokemob.renderMob(pokemob, x + dx, y + dy, watch.width, watch.height, 0, 0, 0, 1);
            // Draw gender, types and lvl
            int genderColor = 0xBBBBBB;
            String gender = "";
            if (pokemob.getSexe() == IPokemob.MALE)
            {
                genderColor = 0x0011CC;
                gender = "\u2642";
            }
            else if (pokemob.getSexe() == IPokemob.FEMALE)
            {
                genderColor = 0xCC5555;
                gender = "\u2640";
            }
            String level = "L. " + pokemob.getLevel();
            dx = -74;
            dy = 42;
            drawString(fontRenderer, level, x + dx, y + dy, 0xffffff);
            dx = -40;
            drawCenteredString(fontRenderer, gender, x + dx, y + dy, genderColor);
            pokemob.getType1();
            String type1 = PokeType.getTranslatedName(pokemob.getType1());
            String type2 = PokeType.getTranslatedName(pokemob.getType2());
            dx = -74;
            dy = 52;
            colour = pokemob.getType1().colour;
            drawString(fontRenderer, type1, x + dx, y + dy, colour);
            colour = pokemob.getType2().colour;
            dy = 62;
            if (pokemob.getType2() != PokeType.unknown) drawString(fontRenderer, type2, x + dx, y + dy, colour);

            // Draw box around where type displays
            dx = -76;
            dy = 10;
            dy = 50;
            dr = 20;
            colour = 0xFF78C850;
            drawVerticalLine(x + dx, y + dy, y + dy + dr, colour);
            drawVerticalLine(x + dx + 2 * dr, y + dy, y + dy + dr, colour);
            dr = 40;
            drawHorizontalLine(x + dx, x + dx + dr, y + dy + dr / 2, colour);
        }
        pages.get(index).drawScreen(mouseX, mouseY, partialTicks);

        if (pokemob != null)
        {
            // Draw hovored tooltip with pokemob's name
            int mx = mouseX - x;
            int my = mouseY - y;
            if (mx > -75 && mx < -75 + 40) if (my > 10 && my < 10 + 40)
            {
                List<String> text = Lists.newArrayList();
                text.add(pokemob.getPokedexEntry().getTranslatedName());
                if (!pokemob.getPokemonNickname().isEmpty())
                {
                    text.add("\"" + pokemob.getPokemonNickname() + "\"");
                }
                GlStateManager.disableDepth();
                mx = -35;
                my = 20;
                dy = fontRenderer.FONT_HEIGHT;
                int box = 0;
                for (String s : text)
                {
                    box = Math.max(box, fontRenderer.getStringWidth(s) + 2);
                }

                drawRect(x + mx - 1, y + my - 1, x + mx + box + 1, y + my + dy * text.size() + 1, 0xFF78C850);
                for (String s : text)
                {
                    drawRect(x + mx, y + my, x + mx + box, y + my + dy, 0xFF000000);
                    fontRenderer.drawString(s, x + mx + 1, y + my, 0xFFFFFFFF, true);
                    my += dy;
                }
                GlStateManager.enableDepth();
            }
        }
        search.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public String getTitle()
    {
        return I18n.format("pokewatch.title.pokeinfo");
    }

}
