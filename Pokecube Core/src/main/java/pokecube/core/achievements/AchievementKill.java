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
public class AchievementKill extends Achievement
{
    PokedexEntry entry;

    public AchievementKill(PokedexEntry entry, int column, int row, Item item, Achievement parent)
    {
        super("achievement.pokecube." + entry.getName() + ".kill", entry.getName(), column, row, item, parent);
        this.entry = entry;
    }

    @Override
    public String getDescription()
    {
        return I18n.format("achievement.pokecube.kill", I18n.format(getPokemobTranslatedName()));
    }

    protected String getPokemobTranslatedName()
    {
        return entry.getUnlocalizedName();
    }

    @Override
    public ITextComponent getStatName()
    {
        ITextComponent iTextComponent = new TextComponentTranslation("achievement.pokecube.kill",
                new TextComponentTranslation(getPokemobTranslatedName()));
        iTextComponent.getStyle().setColor(this.getSpecial() ? TextFormatting.DARK_PURPLE : TextFormatting.GREEN);
        iTextComponent.getStyle()
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponentString(statId)));
        return iTextComponent;
    }

    @Override
    public String toString()
    {
        return getPokemobTranslatedName();
    }
}
