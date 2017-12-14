package pokecube.core.utils;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum PokeType
{
    unknown(0, "???");

    public static float[][] typeTable;

    public static float getAttackEfficiency(PokeType type, PokeType defenseType1, PokeType defenseType2)
    {
        float multiplier = 1;
        if (type == null) return multiplier;
        if (defenseType1 != unknown && defenseType1 != null)
        {
            multiplier *= typeTable[type.ordinal()][defenseType1.ordinal()];
        }
        if (defenseType2 != unknown && defenseType2 != null)
        {
            multiplier *= typeTable[type.ordinal()][defenseType2.ordinal()];
        }
        return multiplier;
    }

    public static String getName(PokeType type)
    {
        return type.name;
    }

    @SideOnly(Side.CLIENT)
    public static String getTranslatedName(PokeType type)
    {
        String translated = I18n.format("type." + type.name);

        if (translated == null || translated.startsWith("type.")) { return type.name; }

        return translated;
    }

    public static PokeType getType(String name)
    {
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        for (PokeType type : values())
        {
            if (name.equalsIgnoreCase(type.name)) return type;
        }
        return unknown;
    }

    public final int    colour;

    public final String name;

    private PokeType(int colour, String name)
    {
        this.colour = colour;
        this.name = name;
    }
}
