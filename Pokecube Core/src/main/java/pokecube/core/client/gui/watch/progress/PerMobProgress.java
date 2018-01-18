package pokecube.core.client.gui.watch.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Predicate;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.AxisAlignedBB;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketPokedex;

public class PerMobProgress extends Progress
{
    GuiTextField text;
    PokedexEntry entry = null;

    public PerMobProgress(GuiPokeWatch watch)
    {
        super(watch);
        setTitle(I18n.format("pokewatch.progress.mob.title"));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = watch.width / 2 - 70;
        int y = watch.height / 2 + 53;
        text = new GuiTextField(0, fontRender, x, y, 140, 10);
    }

    @Override
    public void onPageOpened()
    {
        lines.clear();
        if (entry == null)
        {
            String name = PokecubePlayerDataHandler.getCustomDataTag(watch.player).getString("WEntry");
            entry = Database.getEntry(name);
            if (entry == null) entry = Pokedex.getInstance().getFirstEntry();
        }
        text.setText(entry.getName());
        caught0 = CaptureStats.getTotalNumberOfPokemobCaughtBy(watch.player.getUniqueID(), entry);

        hatched0 = EggStats.getTotalNumberOfPokemobHatchedBy(watch.player.getUniqueID(), entry);

        killed0 = KillStats.getTotalNumberOfPokemobKilledBy(watch.player.getUniqueID(), entry);

        String captureLine = I18n.format("pokewatch.progress.mob.caught", caught0, entry);
        String killLine = I18n.format("pokewatch.progress.mob.killed", killed0, entry);
        String hatchLine = I18n.format("pokewatch.progress.mob.hatched", hatched0, entry);

        AxisAlignedBB centre = watch.player.getEntityBoundingBox();
        AxisAlignedBB bb = centre.grow(PokecubeMod.core.getConfig().maxSpawnRadius, 5,
                PokecubeMod.core.getConfig().maxSpawnRadius);
        List<Entity> otherMobs = watch.player.getEntityWorld().getEntitiesInAABBexcluding(watch.player, bb,
                new Predicate<Entity>()
                {
                    @Override
                    public boolean apply(Entity input)
                    {
                        IPokemob pokemob;
                        if (!(input instanceof EntityAnimal
                                && (pokemob = CapabilityPokemob.getPokemobFor(input)) != null))
                            return false;
                        return pokemob.getPokedexEntry() == entry;
                    }
                });
        String nearbyLine = I18n.format("pokewatch.progress.global.nearby", otherMobs.size());

        for (String line : fontRender.listFormattedStringToWidth(captureLine, 120))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(killLine, 120))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(hatchLine, 120))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(nearbyLine, 120))
        {
            lines.add(line);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        text.drawTextBox();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (new Exception().getStackTrace()[2].getClassName().equals("pokecube.core.client.gui.watch.GuiPokeWatch"))
            text.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        text.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB)
        {
            String text = this.text.getText();
            List<String> ret = new ArrayList<String>();
            for (PokedexEntry entry : Database.getSortedFormes())
            {
                String check = entry.getName().toLowerCase(java.util.Locale.ENGLISH);
                if (check.startsWith(text.toLowerCase(java.util.Locale.ENGLISH)))
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
            if (!ret.isEmpty()) this.text.setText(ret.get(0));
        }
        else if (keyCode == Keyboard.KEY_RETURN)
        {
            PokedexEntry newEntry = Database.getEntry(text.getText());
            if (newEntry != null)
            {
                text.setText(newEntry.getName());
                PacketPokedex.updateWatchEntry(newEntry);
                this.entry = newEntry;
                this.onPageOpened();
            }
            else
            {
                text.setText(entry.getName());
            }
        }
    }

}
