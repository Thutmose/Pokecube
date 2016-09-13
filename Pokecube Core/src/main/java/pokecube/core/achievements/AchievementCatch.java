/**
 *
 */
package pokecube.core.achievements;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import pokecube.core.database.PokedexEntry;

/** @author Manchou */
public class AchievementCatch extends Achievement
{
    PokedexEntry entry;

    public AchievementCatch(PokedexEntry entry, int column, int row, Item item, Achievement parent)
    {
        super(entry == null ? "get1stPokemob" : "achievement.pokecube." + entry.getName() + ".catch",
                entry == null ? "get1stPokemob" : "achievement.pokecube." + entry.getName(), column, row, item, parent);
        this.entry = entry;
        if (entry != null && entry.legendary) this.setSpecial();
    }

    @Override
    public String getDescription()
    {
        if ("get1stPokemob".equals(statId)) { return I18n.format("achievement." + statId + ".desc"); }
        return I18n.format("achievement.pokecube.catch", I18n.format(getPokemobTranslatedName()));
    }

    protected String getPokemobTranslatedName()
    {
        return entry.getUnlocalizedName();
    }

    @Override
    public ITextComponent getStatName()
    {
        if ("get1stPokemob".equals(statId)) { return super.getStatName(); }
        ITextComponent iTextComponent = new TextComponentTranslation("achievement.pokecube.catch",
                new TextComponentTranslation(getPokemobTranslatedName()));
        iTextComponent.getStyle().setColor(this.getSpecial() ? TextFormatting.DARK_PURPLE : TextFormatting.GREEN);
        iTextComponent.getStyle()
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponentString(statId)));
        return iTextComponent;
    }

    @Override
    public String toString()
    {
        if ("get1stPokemob".equals(statId)) { return statId; }
        return getPokemobTranslatedName();
    }
}
