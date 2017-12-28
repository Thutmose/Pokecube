package pokecube.core.client.gui.watch.util;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import thut.api.terrain.BiomeType;

public class SpawnListEntry
{
    final int                 width;
    final int                 height;
    final int                 yMin;
    final SpawnBiomeMatcher   value;
    final WatchPage           parent;
    final FontRenderer        fontRender;
    public final List<String> output = Lists.newArrayList();

    public SpawnListEntry(WatchPage parent, FontRenderer fontRender, SpawnBiomeMatcher value, PokedexEntry entry,
            int width, int height, int yMin)
    {
        this.width = width;
        this.height = height;
        this.yMin = yMin;
        this.value = value;
        this.parent = parent;
        this.fontRender = fontRender;
        value.additionalConditions = Sets.newHashSet();
        value.blackListBiomes = Sets.newHashSet();
        value.blackListSubBiomes = Sets.newHashSet();
        value.validBiomes = Sets.newHashSet();
        value.validSubBiomes = Sets.newHashSet();
        value.reset();
        value.parse();

        List<String> biomes = Lists.newArrayList();
        if (value.validBiomes != null) for (Biome b : value.validBiomes)
        {
            biomes.add(b.getBiomeName());
        }
        if (entry != null) output.add(entry.getName() + ":");
        String ind = entry != null ? "  " : "";
        if (!biomes.isEmpty())
        {
            String biomeString = I18n.format("pokewatch.spawns.biomes") + "\n";
            for (String s : biomes)
            {
                biomeString = biomeString + s + ",\n";
            }
            biomeString = biomeString.substring(0, biomeString.length() - 2) + ".";
            List<String> split = fontRender.listFormattedStringToWidth(biomeString,
                    width - fontRender.getStringWidth(ind));
            split.replaceAll(new UnaryOperator<String>()
            {
                @Override
                public String apply(String t)
                {
                    return ind + t;
                }
            });
            output.addAll(split);
        }

        List<String> types = Lists.newArrayList();
        if (value.validSubBiomes != null) for (BiomeType t : value.validSubBiomes)
        {
            types.add(t.readableName);
        }
        if (!types.isEmpty())
        {
            String typeString = I18n.format("pokewatch.spawns.types") + " ";
            for (String s : types)
            {
                typeString = typeString + s + ", ";
            }
            List<String> split = fontRender.listFormattedStringToWidth(typeString,
                    width - fontRender.getStringWidth(ind));
            split.replaceAll(new UnaryOperator<String>()
            {
                @Override
                public String apply(String t)
                {
                    return ind + t;
                }
            });
            output.addAll(split);
        }
        boolean day = value.day;
        boolean night = value.night;
        boolean dusk = value.dusk;
        boolean dawn = value.dawn;
        boolean water = value.water;
        boolean air = value.air;
        if (water)
        {
            if (air) output.addAll(
                    fontRender.listFormattedStringToWidth(ind + I18n.format("pokewatch.spawns.water_optional"), width));
            else output.addAll(
                    fontRender.listFormattedStringToWidth(ind + I18n.format("pokewatch.spawns.water_only"), width));
        }
        String times = ind + I18n.format("pokewatch.spawns.times");
        if (day)
        {
            times = times + " " + I18n.format("pokewatch.spawns.day");
        }
        if (night)
        {
            if (day) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.night");
        }
        if (dusk)
        {
            if (day || night) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.dusk");
        }
        if (dawn)
        {
            if (day || night || dawn) times = times + ", ";
            times = times + I18n.format("pokewatch.spawns.dawn");
        }
        output.addAll(fontRender.listFormattedStringToWidth(times, width));
        String rate = "";
        if (value.spawnRule.values.containsKey(new QName("Local_Rate")))
        {
            float val = 0;
            try
            {
                val = Float.parseFloat(value.spawnRule.values.get(new QName("Local_Rate")));
            }
            catch (Exception e)
            {

            }

            if (val > 10e-4)
            {
                val = ((int) (val * 1000)) / 10f;
            }
            else
            {
                if (val != 0)
                {
                    float denom = 1000f;
                    float numer = 100000f;
                    float val2 = ((int) (val * numer)) / denom;
                    while (val2 == 0)
                    {
                        numer *= 100;
                        denom *= 100;
                        val2 = ((int) (val * numer)) / denom;
                    }
                    val = val2;
                }
            }
            rate = ind + I18n.format("pokewatch.spawns.rate_local", val + "%");
        }
        else
        {
            float val = 0;
            try
            {
                val = Float.parseFloat(value.spawnRule.values.get(new QName("rate")));
            }
            catch (Exception e)
            {

            }
            if (val > 10e-4)
            {
                val = ((int) (val * 1000)) / 10f;
            }
            else
            {
                val = ((int) (val * 10000)) / 100f;
            }

            String var = val + "%";
            rate = ind + I18n.format("pokewatch.spawns.rate_single", var);
        }
        if (!rate.isEmpty())
        {
            output.addAll(fontRender.listFormattedStringToWidth(rate, width));
        }
        output.add("");
    }

    public List<LineEntry> getLines(IClickListener listener)
    {
        int y0 = yMin;
        int y1 = yMin + height;
        List<LineEntry> lines = Lists.newArrayList();
        int n = 0;
        for (String s : output)
        {
            ITextComponent comp = new TextComponentString(s);
            if (n++ == 0)
            {
                PokedexEntry e = Database.getEntry(comp.getUnformattedComponentText());
                if (e != null)
                {
                    comp.setStyle(new Style());
                    comp.getStyle().setColor(TextFormatting.GREEN);
                }
            }
            lines.add(new LineEntry(y0, y1, fontRender, comp, 0xFFFFFFFF).setClickListner(listener));
        }
        return lines;
    }
}
