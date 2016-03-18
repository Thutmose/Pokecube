package pokecube.core.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import pokecube.core.database.MoveEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public abstract class Move_Base
{
    public static Move_Base instance;

    public final int        index;
    public final String     name;
    private IMoveAnimation  animation;
    public boolean          aoe              = false;

    public boolean          fixedDamage      = false;

    protected SoundEvent    sound;

    public boolean          hasStatModSelf   = false;
    public boolean          hasStatModTarget = false;

    public final MoveEntry  move;

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

        if (instance == null) instance = this;
    }

    public abstract void attack(IPokemob attacker, Entity attacked, float f);

    public abstract void attack(IPokemob attacker, Vector3 location, float f);

    public abstract boolean doAttack(IPokemob attacker, Entity attacked, float f);

    /** Do anything special for self attacks, usually raising/lowering of stats.
     * 
     * @param mob */
    public abstract void doSelfAttack(IPokemob mob, float f);

    public abstract void doWorldAction(IPokemob attacker, Vector3 location);

    protected abstract void finalAttack(IPokemob attacker, Entity attacked, float f);

    protected abstract void finalAttack(IPokemob attacker, Entity attacked, float f, boolean message);

    public IMoveAnimation getAnimation()
    {
        return animation;
    }

    /** Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT}
     * or {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     * 
     * @return the attack category */
    public byte getAttackCategory()
    {
        return (byte) move.attackCategory;
    }

    public abstract int getAttackDelay(IPokemob attacker);

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

    public abstract boolean isMoveImplemented(String s);

    /** Sends a message to clients to display specific animation on the client
     * side.
     *
     * @param attacker
     * @param attacked */
    public abstract void notifyClient(Entity attacker, Vector3 target, Entity attacked);

    /** Called after the attack for special post attack treatment.
     * 
     * @param attacker
     * @param attacked
     * @param f
     * @param finalAttackStrength
     *            the number of HPs the attack takes from target */
    public abstract void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength);

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
