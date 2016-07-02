/**
 *
 */
package pokecube.core;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import pokecube.core.database.Database;

/** @author Manchou */
public class AchievementCatch extends Achievement
{
    int pokedexNb;

    public AchievementCatch(int par1, String par2Str, int par3, int par4, Item item, Achievement par6Achievement)
    {
        super(par2Str, par2Str, par3, par4, item, par6Achievement);
        pokedexNb = par1;
    }

    @Override
    public String getDescription()
    {
        if ("get1stPokemob".equals(statId)) { return I18n.format("achievement." + statId + ".desc"); }
        return I18n.format("achievement.catch", I18n.format(getPokemobTranslatedName()));
    }

    protected String getPokemobTranslatedName()
    {
        if (pokedexNb > 0 && Database.getEntry(pokedexNb) != null)
        {
            return Database.getEntry(pokedexNb).getUnlocalizedName();
        }
        else
        {
            System.out.println("shouldn't happen");
            return "AchievementCatch"; // should not happen
        }
    }

    @Override
    public ITextComponent getStatName()
    {
        if ("get1stPokemob".equals(statId)) { return super.getStatName(); }
        ITextComponent iTextComponent = new TextComponentTranslation(statId, new Object[0]);
        iTextComponent.getStyle().setColor(TextFormatting.GRAY);
        iTextComponent.getStyle().setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponentString(this.statId)));
        return iTextComponent;
    }

    @Override
    public String toString()
    {
        if ("get1stPokemob".equals(statId)) { return statId; }
        return getPokemobTranslatedName();
    }
}
