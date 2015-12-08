package pokecube.core.ai.pokemob;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

/**
 * This AI just makes the pokemon jump in the direction of the target.  PokemobAIAttack deals with the actual attack
 * .
 * @author Patrick
 *
 */
public class PokemobAILeapAtTarget extends EntityAILeapAtTarget {
    /** The entity that is leaping. */
    EntityLiving leaper;
    IPokemob pokemob;
    /** The entity that the leaper is leaping towards. */
    Entity leapTarget;

    public PokemobAILeapAtTarget(EntityLiving entity, float upwardSpeed)
    {
    	super(entity, upwardSpeed);
        this.leaper = entity;
        this.setMutexBits(32);
        pokemob = (IPokemob) leaper;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
        this.leapTarget = this.leaper.getAttackTarget();
    	Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));
    	if(move==null)
    		move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);

    	if(pokemob.getPokemonAIState(IPokemob.LEAPING))
    	{
    		pokemob.setPokemonAIState(IPokemob.LEAPING, false);
    		return false;
    	}
    	
    	if(pokemob.getPokemonAIState(IPokemob.HUNTING) && !pokemob.getPokemonAIState(IPokemob.ANGRY))
    	{
    		if(pokemob.getPokedexEntry().swims())
    		{
                List<EntityFishHook> list = leaper.worldObj.getEntitiesWithinAABB(EntityFishHook.class, new AxisAlignedBB(leaper.posX, leaper.posY, leaper.posZ, leaper.posX + 1.0D,
                		leaper.posY + 1.0D, leaper.posZ + 1.0D).expand(20D, 20D, 20D));
                if(!list.isEmpty())
                {
                	Entity nearest = null;
                	double ds = 600;
                	double dt;
                	Vector3 v = Vector3.getNewVectorFromPool();
                	for(Entity e: list)
                	{
                		dt = e.getDistanceSqToEntity(leaper);
                		if(dt<ds && Vector3.isVisibleEntityFromEntity(e, leaper))
                		{
                			ds = dt;
                			nearest = e;
                		}
                	}
                	v.freeVectorFromPool();
                	if(nearest!=null)
                	{
                		System.out.println("Found a bait "+pokemob.getPokemonDisplayName());
//                		leaper.getNavigator().tryMoveToEntityLiving(nearest, leaper.getMovementSpeed());
                		leapTarget = nearest;
    	            	return true;
                	}
                }
    		}
    	}
    	
    	if(!leaper.onGround && !(pokemob.getPokedexEntry().floats()|| pokemob.getPokedexEntry().flys()))
    		return false;
    	
        if(((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE)>0))
        {
        	return false;
        }
        if(((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF)>0))
        {
        	return false;
        }
        if (this.leapTarget == null)
        {
            return false;
        }
        else if(!pokemob.getPokemonAIState(IPokemob.LEAPING) && !((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE)>0))
        {
            double d0 = this.leaper.getDistanceSqToEntity(this.leapTarget);
            double d1 = pokemob.getPokedexEntry().flys()?9:4+leaper.width;
        	return d0<d1;
        }
        else
        {
            double d0 = this.leaper.getDistanceSqToEntity(this.leapTarget);
            
            float diff = leaper.width + leapTarget.width;
            diff = diff * diff;
            return d0 >= diff && d0 <= 16.0D ? (this.leaper.getRNG().nextInt(5) == 0) : false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
	public boolean continueExecuting()
    {
        return !pokemob.getPokemonAIState(IPokemob.LEAPING);
    }
    
    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
    	 this.leaper.getLookHelper().setLookPositionWithEntity(this.leapTarget, 30.0F, 30.0F);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
    	if(leapTarget instanceof IPokemob)
    	{
    		IPokemob targ = (IPokemob) leapTarget;
    		if(!targ.getPokemonAIState(IPokemob.ANGRY))
    		{
    			((EntityLiving)targ).setAttackTarget(leaper);
    			targ.setPokemonAIState(IPokemob.ANGRY, true);
    		}
    	}
    	pokemob.setPokemonAIState(IPokemob.LEAPING, true);

    	Vector3 targetLoc = Vector3.getNewVectorFromPool().set(leapTarget);
    	Vector3 leaperLoc = Vector3.getNewVectorFromPool().set(leaper);
    	
    	Vector3 dir = targetLoc.subtract(leaperLoc).scalarMultBy(0.5f);

        if(dir.isNaN())
        {
            new Exception().printStackTrace();
            dir.clear();
        }
    	dir.addVelocities(leaper);
    	
    	dir.freeVectorFromPool();
    	leaperLoc.freeVectorFromPool();
    	targetLoc.freeVectorFromPool();
    	
    }
}