/**
 *
 */
package pokecube.core;

import net.minecraft.block.Block;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import pokecube.core.database.Pokedex;

/**
 * @author Manchou
 *
 */
public class AchievementCatch extends Achievement
{
    int pokedexNb;
    public AchievementCatch(int par1, String par2Str, int par3, int par4,
            Block block, Achievement par6Achievement)
    {
    	super(par2Str, par2Str, par3, par4, block, par6Achievement);//super(2000+par1, par2Str, par3, par4, block, par6Achievement);
        pokedexNb = par1;
    }

    public AchievementCatch(int par1, String par2Str, int par3, int par4,
            Item item, Achievement par6Achievement)
    {
        super(par2Str, par2Str, par3, par4, item, par6Achievement);
        pokedexNb = par1;
    }

    @Override
    public String getDescription()
    {
        if ("get1stPokemob".equals(statId))
        {
            return StatCollector.translateToLocal("achievement."+statId + ".desc");
        }
        return StatCollector.translateToLocalFormatted("achievement.catch", getPokemobTranslatedName());
    }
    
    protected String getPokemobTranslatedName()
    {
        if (pokedexNb > 0&&Pokedex.getInstance().getEntry(pokedexNb)!=null)
        {
            return Pokedex.getInstance().getEntry(pokedexNb).getTranslatedName();
        }
        else
        {
        	System.out.println("shouldn't happen");
            return "AchievementCatch";    // should not happen
        }
    }

    @Override
	public IChatComponent getStatName() {
		if ("get1stPokemob".equals(statId)) {
			return super.getStatName();
		}
		IChatComponent ichatcomponent = new ChatComponentTranslation(statId,
				new Object[0]);
		ichatcomponent.getChatStyle().setColor(EnumChatFormatting.GRAY);
		ichatcomponent.getChatStyle().setChatHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT,
						new ChatComponentText(this.statId)));
		return ichatcomponent;
	}


    @Override
    public String toString()
    {
    	if ("get1stPokemob".equals(statId))
    	{
    		return statId;
    	}
        return getPokemobTranslatedName();
    }
}
