package pokecube.core.ai.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Utility;
import thut.api.maths.Vector3;

/**
 * This AI executes the Utility moves, which are moves that have out of battle effects.
 * It makes the pokemon walk over to the location, face the correct way, then execute
 * the move.
 * @author Patrick
 *
 */
public class PokemobAIUtilityMove extends EntityAIBase {

	final IPokemob pokemon;
	final EntityLiving entity;
	final World world;
	Vector3 destination;
	double speed;
	Vector3 v = Vector3.getNewVectorFromPool(), v1 = Vector3.getNewVectorFromPool();
	
	public PokemobAIUtilityMove(EntityLiving pokemob) {
		this.pokemon = (IPokemob) pokemob;
		this.entity = pokemob;
		this.world = pokemob.worldObj;
        this.setMutexBits(3);
		speed = pokemon.getMovementSpeed();
	}

	@Override
	public boolean shouldExecute() {
		return pokemon.getPokemonAIState(IPokemob.NEWEXECUTEMOVE);
	}
	
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
	public boolean continueExecuting()
    {
    	return destination!=null&&!pokemon.getPokemonAIState(IPokemob.NEWEXECUTEMOVE)&&pokemon.getPokemonAIState(IPokemob.EXECUTINGMOVE);
    }
	
    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
    	EntityLivingBase owner = null;
    	pokemon.setPokemonAIState(IPokemob.NEWEXECUTEMOVE, false);
    	if(pokemon.getPokemonAIState(IPokemob.TAMED))
    	{
    		owner = pokemon.getPokemonOwner();
    	}

    	Move_Base move = MovesUtils.getMoveFromName(pokemon.getMove(pokemon.getMoveIndex()));
    	if(move==null)
    	{
    		return;
    	}
    	if(move.name.equalsIgnoreCase(IMoveNames.MOVE_FLASH) || move.name.equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
    	{
			((Move_Utility)move).attack(pokemon, (Entity) pokemon, 0);
			return;
    	}
    	
    	Vector3 look;
    	Vector3 source;
    	if(owner==null)
    	{
    		look = v.set(entity.getLookVec());
    		source = v1.set(entity, false);
    	}
    	else
    	{
    		look = v.set(owner.getLookVec());
    		source = v1.set(owner, false);
    	}
    	
    	destination = Vector3.findNextSolidBlock(world, source, look, 32);
    	if(destination==null)
    	{
    		
    	}
    	else
    	{
    		pokemon.setPokemonAIState(IPokemob.EXECUTINGMOVE, true);
    		entity.getNavigator().tryMoveToXYZ(destination.x, destination.y, destination.z, speed);
    	}
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
    	
    	if(destination==null)
		{
        	if(pokemon.getPokemonAIState(IPokemob.EXECUTINGMOVE))
        		pokemon.setPokemonAIState(IPokemob.EXECUTINGMOVE, false);
    		return;
		}
    	entity.getLookHelper().setLookPosition(destination.x, destination.y, destination.z, 10, entity.getVerticalFaceSpeed());
    	Vector3 loc = Vector3.getNewVectorFromPool().set(entity, false);
    	double dist = loc.distToSq(destination);
    	double var1 = 16;
        Move_Base move = MovesUtils.getMoveFromName(pokemon.getMove(pokemon.getMoveIndex()));
    	
    	if(move==null)
    		move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
    	if((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE)>0)
    	{
    		var1 = 64;
    	}
    	if(dist<var1)
    	{
    		if(move instanceof Move_Utility)
    		{
    			loc = loc.add(v.set(entity.getLookVec()).scalarMultBy(pokemon.getPokedexEntry().width * pokemon.getSize()));
    			((Move_Utility)move).doUtilityAction(pokemon, destination);
    		}
    		else
    		{
    			pokemon.executeMove(null, destination, 0);
    			entity.getNavigator().clearPathEntity();
    		}
    		pokemon.setPokemonAIState(IPokemob.EXECUTINGMOVE, false);
    		destination=null;
    	}
    	loc.freeVectorFromPool();
    }
    
}
