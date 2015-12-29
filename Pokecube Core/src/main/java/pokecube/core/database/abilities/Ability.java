package pokecube.core.database.abilities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.StatCollector;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public abstract class Ability
{
    /** Called during the pokemob's update tick.
     * 
     * @param mob */
    public abstract void onUpdate(IPokemob mob);

    /** Called whenever a move is used.
     * 
     * @param mob
     * @param move */
    public abstract void onMoveUse(IPokemob mob, MovePacket move);

    /** Calls when the pokemob first agresses the target.
     *  This is called by the agressor, so mob is the pokemob doing the agression.
     *  Target is the agressed mob.
     * @param mob
     * @param target */
    public abstract void onAgress(IPokemob mob, EntityLivingBase target);

    /** Inits the Ability, if args isn't null, it will usually have the Pokemob
     * passed in as the first argument.<br>
     * If there is a second argument, it should be and integer range for the
     * expected distance the ability affects.
     * 
     * @param args
     * @return */
    public Ability init(Object... args)
    {
        return this;
    }

    /** Called when the pokemob is set to dead. */
    public void destroy()
    {
    }

    @Override
    public String toString()
    {
        return AbilityManager.getNameForAbility(this);
    }

    // TODO localize these in lang files
    public String getName()
    {
        String translated = StatCollector.translateToLocal("ability." + toString() + ".name").trim();
        if (translated.contains(".")) { return toString(); }
        return translated;
    }
}
