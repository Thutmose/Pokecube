package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

/** This AI class attempts to make the pokemon dodge an incoming attack. This is
 * where the calculations should factor in accuracy and evasion.
 * 
 * @author Patrick */
public class PokemobAIDodge extends EntityAIBase
{

    IPokemob     dodger;
    EntityLiving entity;
    Vector3      v  = Vector3.getNewVector();
    Vector3      v1 = Vector3.getNewVector();

    public PokemobAIDodge(EntityLiving par1EntityLiving)
    {
        this.dodger = (IPokemob) par1EntityLiving;
        entity = par1EntityLiving;
        this.setMutexBits(16);
    }

    boolean shouldDodge()
    {
        boolean dodge = false;

        if (!entity.onGround && !(dodger.getPokedexEntry().floats() || dodger.getPokedexEntry().flys())) return dodge;

        byte[] mods = dodger.getModifiers();

        double evasionMod = Tools.modifierToRatio(mods[7], true) / 30d;

        dodge = Math.random() > (1 - evasionMod);
        return dodge;
    }

    @Override
    public boolean shouldExecute()
    {

        if (entity.getAttackTarget() != null && entity.getAttackTarget() instanceof IPokemob)
        {
            IPokemob target = (IPokemob) entity.getAttackTarget();

            boolean shouldDodgeMove = target.getPokemonAIState(IMoveConstants.EXECUTINGMOVE);

            if (shouldDodgeMove)
            {
                Move_Base move = MovesUtils.getMoveFromName(target.getMove(target.getMoveIndex()));
                if (move == null || ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0))
                {
                    shouldDodgeMove = false;
                }
            }
            shouldDodgeMove = shouldDodgeMove && shouldDodge();

            return shouldDodgeMove;
        }

        return false;
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
        Vector3 loc = v.set(dodger);
        Vector3 target = v1.set(entity.getAttackTarget());
        Vector3 temp = Vector3.getNewVector();
        Vector3 perp = target.subtractFrom(loc).rotateAboutLine(Vector3.secondAxis, Math.PI / 2, temp);

        if (Math.random() > 0.5) perp = perp.scalarMultBy(-1);
        dodger.setPokemonAIState(IMoveConstants.DODGING, true);
        perp = perp.normalize();
        if (perp.isNaN())
        {
            new Exception().printStackTrace();
            perp.clear();
        }
        perp.scalarMultBy(dodger.getPokedexEntry().width * dodger.getSize());
        perp.addVelocities(entity);

    }
}
