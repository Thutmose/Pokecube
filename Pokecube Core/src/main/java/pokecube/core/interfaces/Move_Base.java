package pokecube.core.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public abstract class Move_Base
{
    public final int       index;
    public final String    name;
    private IMoveAnimation animation;
    public boolean         aoe              = false;
    public boolean         fixedDamage      = false;
    protected SoundEvent   sound;
    public boolean         hasStatModSelf   = false;
    public boolean         hasStatModTarget = false;
    public final MoveEntry move;

    /** Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name
     *            the English name of the attack, used as identifier and
     *            translation key
     * @param attackCategory
     *            can be either {@link MovesUtils#CATEGORY_CONTACT} or
     *            {@link MovesUtils#CATEGORY_DISTANCE} */
    public Move_Base(String name)
    {
        this.name = name;
        this.move = MoveEntry.get(name);
        this.index = move.index;

        boolean mod = false;
        for (int i : move.attackedStatModification)
        {
            if (i != 0)
            {
                mod = true;
                break;
            }
        }
        if (!mod) move.attackedStatModProb = 0;
        mod = false;
        for (int i : move.attackerStatModification)
        {
            if (i != 0)
            {
                mod = true;
                break;
            }
        }
        if (!mod) move.attackerStatModProb = 0;

        if (move.attackedStatModProb > 0) hasStatModTarget = true;
        if (move.attackerStatModProb > 0) hasStatModSelf = true;
    }

    /** First stage of attack use, this is called when the attack is being
     * initiated.<br>
     * This version is called for an attack at a specific entity, should only be
     * called for not-interceptable attacks.
     * 
     * @param attacker
     * @param attacked */
    public abstract void attack(IPokemob attacker, Entity attacked);

    /** First stage of attack use, this is called when the attack is being
     * initiated.<br>
     * This version is called for an attack at a location.
     * 
     * @param attacker
     * @param location */
    public abstract void attack(IPokemob attacker, Vector3 location);

    /** This is where the move's damage should be applied to the mob.
     * 
     * @param packet
     * @return */
    public abstract void onAttack(MovePacket packet);

    public abstract void doWorldAction(IPokemob attacker, Vector3 location);

    public IMoveAnimation getAnimation()
    {
        return animation;
    }

    public IMoveAnimation getAnimation(IPokemob user)
    {
        return getAnimation();
    }

    /** Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT}
     * or {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     * 
     * @return the attack category */
    public byte getAttackCategory()
    {
        return (byte) move.attackCategory;
    }

    public abstract void applyHungerCost(IPokemob attacker);

    /** Index getter.
     * 
     * @return a int ID for this move */
    public int getIndex()
    {
        return index;
    }

    public abstract Move_Base getMove(String name);

    /** Name getter
     * 
     * @return the name of this move */
    public String getName()
    {
        return name;
    }

    /** PP getter
     * 
     * @return the number of Power points of this move */
    public int getPP()
    {
        return move.pp;
    }

    /** PRE getter
     * 
     * @return the precision of this move */
    public int getPRE()
    {
        return move.accuracy;
    }

    /** PWR getter
     * 
     * @return the power of this move */
    public int getPWR()
    {
        return move.power;
    }

    /** PWR getter
     * 
     * @return the power of this move */
    public int getPWR(IPokemob user, Entity target)
    {
        return move.power;
    }

    /** Type getter
     * 
     * @return the type of this move */
    public PokeType getType(IPokemob user)
    {
        return move.type;
    }

    /** Called after the attack is done for any additional effects needed Both
     * involved mobs should be notified of the packet here.
     * 
     * @param packet */
    public abstract void postAttack(MovePacket packet);

    /** Called before the attack is applied. Both involved mobs should be
     * notified of the packet here.
     * 
     * @param packet */
    public abstract void preAttack(MovePacket packet);

    /** Called after the attack is done but before postAttack is called.
     * 
     * @param packet */
    public abstract void handleStatsChanges(MovePacket packet);

    /** This is a factor for how long of a cooldown occurs after the attack is
     * done.
     * 
     * @param attacker
     * @return */
    public float getPostDelayFactor(IPokemob attacker)
    {
        return 1;
    }

    public Move_Base setAnimation(IMoveAnimation anim)
    {
        this.animation = anim;
        return this;
    }

    /** Sets if the attack hits all targets in the area, this area is default
     * 4x4 around the mob, but should be specified via Overriding the
     * doFinalAttack method, see Earthquake for an example.
     * 
     * @return */
    public Move_Base setAOE()
    {
        aoe = true;
        return this;
    }

    /** Sets if the attack hits all targets in the direction it is fired,
     * example being flamethrower, that should hit all things in front.
     * 
     * @return */
    public Move_Base setFixedDamage()
    {
        fixedDamage = true;
        return this;
    }

    /** Sets if the attack hits all targets in the direction it is fired,
     * example being flamethrower, that should hit all things in front.
     * 
     * @return */
    public Move_Base setMultiTarget()
    {
        move.multiTarget = true;
        return this;
    }

    /** Sets if the move can not be intercepted. this should be used for moves
     * like psychic, which should not be intercepted.
     * 
     * @param bool
     * @return */
    public Move_Base setNotInterceptable()
    {
        move.notIntercepable = true;
        return this;
    }

    /** Sets if the move can not be intercepted. this should be used for moves
     * like psychic, which should not be intercepted.
     * 
     * @param bool
     * @return */
    public Move_Base setSelf()
    {
        hasStatModSelf = true;
        return this;
    }

    /** Specify the sound this move should play when executed.
     * 
     * @param sound
     *            the string id of the sound to play
     * @return the move */
    public Move_Base setSound(String sound)
    {
        this.sound = new SoundEvent(new ResourceLocation(sound));
        return this;
    }
}
