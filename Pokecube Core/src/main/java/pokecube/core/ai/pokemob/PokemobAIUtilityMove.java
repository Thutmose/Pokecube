package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

/** This AI executes the Utility moves, which are moves that have out of battle
 * effects. It makes the pokemon walk over to the location, face the correct
 * way, then execute the move.
 * 
 * @author Patrick */
public class PokemobAIUtilityMove extends EntityAIBase
{

    final IPokemob     pokemon;
    final EntityLiving entity;
    final World        world;
    public Vector3     destination;
    double             speed;
    Vector3            v = Vector3.getNewVector(), v1 = Vector3.getNewVector();

    public PokemobAIUtilityMove(EntityLiving pokemob)
    {
        this.pokemon = CapabilityPokemob.getPokemobFor(pokemob);
        this.entity = pokemob;
        this.world = pokemob.getEntityWorld();
        this.setMutexBits(3);
        speed = pokemon.getMovementSpeed();
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    @Override
    public boolean shouldContinueExecuting()
    {
        return destination != null && !pokemon.getPokemonAIState(IMoveConstants.NEWEXECUTEMOVE)
                && pokemon.getPokemonAIState(IMoveConstants.EXECUTINGMOVE);
    }

    @Override
    public boolean shouldExecute()
    {
        return pokemon.getPokemonAIState(IMoveConstants.NEWEXECUTEMOVE) && entity.getAttackTarget() == null
                && pokemon.getAttackCooldown() <= 0;
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
        pokemon.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, false);

        Move_Base move = MovesUtils.getMoveFromName(pokemon.getMove(pokemon.getMoveIndex()));
        if (move == null) { return; }
        if (move.name.equalsIgnoreCase(IMoveNames.MOVE_FLASH) || move.name.equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
        {
            move.attack(pokemon, pokemon.getEntity());
            return;
        }
        if (destination == null)
        {

        }
        else
        {
            pokemon.setPokemonAIState(IMoveConstants.EXECUTINGMOVE, true);
            entity.getNavigator().tryMoveToXYZ(destination.x, destination.y, destination.z, speed);
        }
    }

    /** Updates the task */
    @Override
    public void updateTask()
    {
        if (destination == null || entity.getAttackTarget() != null)
        {
            if (pokemon.getPokemonAIState(IMoveConstants.EXECUTINGMOVE))
                pokemon.setPokemonAIState(IMoveConstants.EXECUTINGMOVE, false);
            return;
        }
        entity.getLookHelper().setLookPosition(destination.x, destination.y, destination.z, 10,
                entity.getVerticalFaceSpeed());
        Vector3 loc = Vector3.getNewVector().set(entity, false);
        double dist = loc.distToSq(destination);
        double var1 = 16;
        Move_Base move = MovesUtils.getMoveFromName(pokemon.getMove(pokemon.getMoveIndex()));
        if (move == null) move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
        {
            var1 = 64;
        }
        if (dist < var1)
        {
            pokemon.executeMove(null, destination, 0);
            entity.getNavigator().clearPathEntity();
            pokemon.setPokemonAIState(IMoveConstants.EXECUTINGMOVE, false);
            destination = null;
        }
    }

}
