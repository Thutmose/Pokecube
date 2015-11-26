package pokecube.core.ai.pokemob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

/**
 * Breeding AI. Finds a mate, then tries to get the pokemon to walk over an breed.
 * @author Patrick
 *
 */
public class PokemobAIMate extends EntityAIBase {
	   private EntityAnimal theAnimal;
	    
	    private EntityAnimal targetMate;

	    /**
	     * Delay preventing a baby from spawning immediately when two mate-able animals find each other.
	     */
	    int spawnBabyDelay;

	    /** The speed the creature moves at during mating behavior. */
	    double moveSpeed;

	    public PokemobAIMate(EntityAnimal par1EntityAnimal)
	    {
	        this.theAnimal = par1EntityAnimal;
	        this.moveSpeed = ((EntityPokemob)theAnimal).getMovementSpeed();
	        this.setMutexBits(1);
	    }

	    /**
	     * Returns whether the EntityAIBase should begin execution.
	     */
	    @Override
		public boolean shouldExecute()
	    {
	        if(this.theAnimal instanceof EntityPokemob)
	        {
	        	EntityPokemob mob = (EntityPokemob) theAnimal;
	        	boolean isMating = mob.getPokemonAIState(IPokemob.MATING);
	        	if((mob.getLover()!=null && !((EntityPokemob)mob.getLover()).isInLove()) || (isMating && mob.males.isEmpty()))
	        		mob.resetInLove();
	        	
	    		//System.out.println(mob.getPokemonDisplayName()+" "+isMating+" "+mob.males);
	        	if(isMating)
	        		return true;
	        	targetMate = (EntityAnimal) mob.getLover();
	        	if(mob.getLover()!=null)
	        	{
	        		mob.setPokemonAIState(IPokemob.MATING, true);
	        		return true;
	        	}
	        	if(mob.getSexe() == IPokemob.MALE || !mob.isInLove())
	        		return false;
	        	
	        	targetMate = (EntityAnimal) mob.findLover();
	        	if(mob.males.size()==0)
	        		return false;
	        	
	        	ArrayList<IPokemob> toRemove = new ArrayList<IPokemob>();
	        	for(IPokemob m : mob.males)
	        	{
	        		if(((Entity)m).isDead)
	        		{
	        			toRemove.add(m);
	        		}
	        	}
	        	mob.males.removeAll(toRemove);
	        	if(mob.males.isEmpty())
	        	{
	        		mob.resetInLove();
	        		targetMate = null;
	        		return false;
	        	}
        		mob.setPokemonAIState(IPokemob.MATING, true);
	        	return true;
	        }
	        return false;
	    }

	    /**
	     * Returns whether an in-progress EntityAIBase should continue executing
	     */
	    @Override
		public boolean continueExecuting()
	    {
	        return targetMate!=null && this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
	    }

	    /**
	     * Resets the task
	     */
	    @Override
		public void resetTask()
	    {
	        this.targetMate = null;
	        this.spawnBabyDelay = 0;
	    }

	    /**
	     * Updates the task
	     */
	    @Override
		public void updateTask()
	    {
	        boolean rePath = true;

        	EntityPokemob mob = (EntityPokemob) theAnimal;
	        if(mob.getSexe() == IPokemob.MALE && targetMate!=null)
	        {
		        this.theAnimal.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, this.theAnimal.getVerticalFaceSpeed());
		        if(((EntityPokemob)targetMate).males.size()>1)
		        {
		        	IPokemob[] males = ((EntityPokemob)targetMate).males.toArray(new IPokemob[0]);
		            Arrays.sort(males, new Comparator<IPokemob>() {
	
		    			@Override
		    			public int compare(IPokemob o1, IPokemob o2) {
		    					if (o2.getLevel() == o1.getLevel())
		    						return (o1.getPokemonDisplayName().compareTo(o2.getPokemonDisplayName()));
		    					return o2.getLevel() - o1.getLevel();
		    			}
		    		});
		            int level = males[0].getLevel();
		            int n = 0;
		            for(int i = 0; i<males.length; i++){
		            	if(males[i].getLevel()<level || ((EntityPokemob)males[i]).getHealth() < ((EntityPokemob)males[i]).getMaxHealth()/1.5f)
		            	{
		            		((EntityPokemob)targetMate).males.remove(males[i]);
		            		((EntityPokemob)males[i]).resetInLove();
		            		n++;
		            	}
		            }
		            if(n==0 && ((EntityPokemob)targetMate).males.size()>1)
		            {
		            	((EntityPokemob)((EntityPokemob)targetMate).males.get(0)).resetInLove();
		            	((EntityPokemob)((EntityPokemob)targetMate).males.get(1)).resetInLove();
		            	((EntityPokemob)targetMate).males.get(0).setPokemonAIState(IPokemob.MATEFIGHT, true);
		            	((EntityPokemob)targetMate).males.get(1).setPokemonAIState(IPokemob.MATEFIGHT, true);
		            	((EntityPokemob)((EntityPokemob)targetMate).males.get(0)).setAttackTarget(((EntityPokemob)((EntityPokemob)targetMate).males.get(1)));
		            	//System.out.println("fight");
		            }
		            
		        }
		        
		        if(((EntityPokemob)targetMate).males.size()>1)
		        	return;
		        else if(((EntityPokemob)targetMate).males.size()==0)
		        {
		        	((EntityPokemob)targetMate).resetInLove();
		        	mob.resetInLove();
		        }
	        }
	        else if(mob.males.size() == 1)
	        {
	        	targetMate = (EntityAnimal) mob.males.get(0);
	        	mob.setLover(targetMate);
	        	((EntityPokemob)targetMate).setLover(mob);
	        }
	        
	        if(targetMate==null)
	        	return;
	        
	        if(theAnimal.getNavigator().getPath()!=null)
	        {
	        	Vector3 temp = Vector3.getNewVectorFromPool();
	        	Vector3 temp1 = Vector3.getNewVectorFromPool().set(targetMate);
	        	temp.set(theAnimal.getNavigator().getPath().getFinalPathPoint());
	        	if(temp.distToSq(temp1) < 4)
	        		rePath = false;
		        temp.freeVectorFromPool();
	        	temp1.freeVectorFromPool();
	        }
	        if(rePath)
	        	this.theAnimal.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
        		
	        ++this.spawnBabyDelay;
	        if (this.spawnBabyDelay >= 60 && this.theAnimal.getDistanceSqToEntity(this.targetMate) < 9.0D)
	        {
	        	((EntityPokemob)theAnimal).fuck((IPokemob) targetMate);
	        }
	        if(this.spawnBabyDelay > 200)
	        {
	        	((EntityPokemob)theAnimal).resetInLove();
	        }
	    }
}
