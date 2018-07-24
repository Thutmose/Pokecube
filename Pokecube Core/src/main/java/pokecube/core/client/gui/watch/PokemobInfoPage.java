package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.watch.pokemob.Breeding;
import pokecube.core.client.gui.watch.pokemob.Moves;
import pokecube.core.client.gui.watch.pokemob.Spawns;
import pokecube.core.client.gui.watch.pokemob.StatsInfo;
import pokecube.core.client.gui.watch.util.PageButton;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
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

public class PokemobInfoPage extends PageWithSubPages
{
    public static int savedIndex = 0;

    IPokemob          pokemob;
    IPokemob          renderMob;
    GuiTextField      search;

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
        index = savedIndex;
    }

    public void initPages(IPokemob pokemob)
    {
        if (pokemob == null)
        {
            String name = PokecubePlayerDataHandler.getCustomDataTag(watch.player).getString("WEntry");
            PokedexEntry entry = Database.getEntry(name);
            if (entry == null)
            {
                entry = Pokedex.getInstance().getFirstEntry();
            }
            pokemob = EventsHandlerClient.getRenderMob(entry, watch.player.getEntityWorld());
        }
        this.pokemob = pokemob;
        this.renderMob = pokemob;
        search.setVisible(!watch.canEdit(pokemob));
        search.setText(pokemob.getPokedexEntry().getName());
        PacketPokedex.sendSpecificSpawnsRequest(pokemob.getPokedexEntry());
        PacketPokedex.updateWatchEntry(pokemob.getPokedexEntry());
        pages.clear();
        pages.add(new StatsInfo(watch, pokemob));
        pages.add(new Moves(watch, pokemob));
        pages.add(new Spawns(watch, pokemob));
        pages.add(new Breeding(this, pokemob));
        for (WatchPage page : pages)
        {
            page.initGui();
        }
    }

    @Override
    public void preSubOpened()
    {
        initPages(pokemob);
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        String next = ">";
        String prev = "<";
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x + 64, y - 70, 12, 12, next, this));
        this.watch.getButtons().add(new PageButton(watch.getButtons().size(), x - 76, y - 70, 12, 12, prev, this));
        PageButton buttonNext = new PageButton(watch.getButtons().size(), x - 46, y + 4, 12, 20, next, this);
        PageButton buttonPrev = new PageButton(watch.getButtons().size() + 1, x - 76, y + 4, 12, 20, prev, this);
        PageButton formCycle = new PageButton(watch.getButtons().size() + 2, x - 65, y + 4, 20, 9, "\u2500", this);
        PageButton playSound = new PageButton(watch.getButtons().size() + 3, x - 65, y + 13, 20, 10, "\u266B", this);
        this.watch.getButtons().add(buttonNext);
        this.watch.getButtons().add(buttonPrev);
        this.watch.getButtons().add(formCycle);
        this.watch.getButtons().add(playSound);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 3)// Next
        {
            changePage(index + 1);
            savedIndex = index;
        }
        else if (button.id == 4)// Previous
        {
            changePage(index - 1);
            savedIndex = index;
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
        else if (button.id == 7 && !watch.canEdit(pokemob))
        {
            // Cycle Form.
            PokedexEntry entry = pokemob.getPokedexEntry();
            List<PokedexEntry> forms = Lists.newArrayList(Database.getFormes(entry));
            int index = forms.indexOf(pokemob.getPokedexEntry());
            if (index != -1)
            {
                entry = forms.get((index + 1) % forms.size());
                initPages(pokemob.megaEvolve(entry));
            }
        }
        else if (button.id == 8)
        {
            watch.player.playSound(pokemob.getSound(), 0.5f, 1.0F);
        }
        super.actionPerformed(button);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!new Exception().getStackTrace()[2].getClassName()
                .equals("pokecube.core.client.gui.watch.util.PageWithSubPages"))
        {
            // change gender if clicking on the gender, and shininess otherwise
            if (!watch.canEdit(pokemob))
            {
                int x = (watch.width - 160) / 2 + 80;
                int y = (watch.height - 160) / 2 + 8;
                int mx = mouseX - x;
                int my = mouseY - y;
                if (mx > -43 && mx < -43 + 76 && my > 42 && my < 42 + 7)
                {
                    switch (pokemob.getSexe())
                    {
                    case IPokemob.MALE:
                        pokemob.setSexe(IPokemob.FEMALE);
                        break;
                    case IPokemob.FEMALE:
                        pokemob.setSexe(IPokemob.MALE);
                        break;
                    }
                }
                else if (mx > -75 && mx < -75 + 40 && my > 10 && my < 10 + 40)
                {
                    if (pokemob.getPokedexEntry().hasShiny)
                    {
                        pokemob.setShiny(!pokemob.isShiny());
                    }
                }
            }
            if (search.getVisible()) search.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (search.getVisible())
        {
            search.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_TAB)
            {
                String text = search.getText();
                text = Database.trim(text);
                List<String> ret = new ArrayList<String>();
                for (PokedexEntry entry : Database.getSortedFormes())
                {
                    String check = entry.getTrimmedName();
                    if (check.startsWith(text))
                    {
                        String name = entry.getName();
                        if (name.contains(" "))
                        {
                            name = "\'" + name + "\'";
                        }
                        ret.add(name);
                    }
                }
                Collections.sort(ret, new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        if (o1.startsWith("'") && !o2.startsWith("'")) return 1;
                        else if (o2.startsWith("'") && !o1.startsWith("'")) return -1;
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                ret.replaceAll(new UnaryOperator<String>()
                {

                    @Override
                    public String apply(String t)
                    {
                        if (t.startsWith("'") && t.endsWith("'"))
                        {
                            t = t.substring(1, t.length() - 1);
                        }
                        return t;
                    }
                });
                String[] args = { text };
                ret = CommandBase.getListOfStringsMatchingLastWord(args, ret);
                if (!ret.isEmpty()) search.setText(ret.get(0));
            }
            else if (keyCode == Keyboard.KEY_RETURN)
            {
                PokedexEntry entry = pokemob.getPokedexEntry();
                PokedexEntry newEntry = Database.getEntry(search.getText());
                // Search to see if maybe it was a translated name put into the
                // search.
                if (newEntry == null)
                {
                    for (PokedexEntry e : Database.getSortedFormes())
                    {
                        String translated = I18n.format(e.getUnlocalizedName());
                        if (translated.equalsIgnoreCase(search.getText()))
                        {
                            Database.data2.put(translated, e);
                            entry = e;
                            break;
                        }
                    }
                    // If the pokedex entry is not actually registered, use
                    // old
                    // entry.
                    if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
                }

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
    public void prePageDraw(int mouseX, int mouseY, float partialTicks)
    {
        if (!watch.canEdit(pokemob))
        {
            String name = PokecubePlayerDataHandler.getCustomDataTag(watch.player).getString("WEntry");
            if (!name.equals(pokemob.getPokedexEntry().getName()))
            {
                search.setText(name);
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
            IPokemob pokemob = this.renderMob;
            // Copy the stuff to the render mob if this mob is in world
            if (pokemob.getEntity().addedToChunk)
            {
                IPokemob newMob = EventsHandlerClient.getRenderMob(pokemob.getPokedexEntry(),
                        pokemob.getEntity().getEntityWorld());
                newMob.readPokemobData(pokemob.writePokemobData());
                pokemob = this.renderMob = newMob;
            }

            if (!pokemob.getEntity().addedToChunk)
            {
                EntityLivingBase player = watch.player;
                EntityTools.copyEntityTransforms(pokemob.getEntity(), player);
            }

            dx = -35;
            dy = -55;
            // Draw the actual pokemob
            GuiPokedex.renderMob(pokemob.getEntity(), mc, dx, dy, 0.75f, watch.height, watch.width, 160, 160, 0, 45,
                    -45);
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
    }

    @Override
    public void postPageDraw(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
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
                int dy = fontRenderer.FONT_HEIGHT;
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
    }

    @Override
    public String getTitle()
    {
        return I18n.format("pokewatch.title.pokeinfo");
    }

}
