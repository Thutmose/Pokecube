package pokecube.compat.ai;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class AITendPlants extends EntityAIBase
{

	final IPokemob pokemob;
	final EntityLiving entity;
	Random rand = new Random();
	public AITendPlants(EntityLiving entity_)
	{
		entity = entity_;
		pokemob = (IPokemob) entity;
	}

	@Override
	public boolean shouldExecute()
	{
		return AbilityManager.hasAbility("honey gather", pokemob) && rand.nextDouble() > 0.99;
	}

	@Override
    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void updateTask()
	{
		Vector3 here = Vector3.getNewVectorFromPool().set(pokemob);
		int range = 4;
		
		for(int i = 0; i<range*range*range; i++)
		{
			
			here.set(pokemob).addTo(5*(rand.nextDouble()-0.5), 5*(rand.nextDouble()-0.5), 5*(rand.nextDouble()-0.5));
			
			IBlockState state = here.getBlockState(entity.worldObj);
			Block block = state.getBlock();
	        if (block instanceof IGrowable)
	        {
	        	IGrowable growable = (IGrowable) block;
	            if (growable.canGrow(entity.worldObj, here.getPos(), here.getBlockState(entity.worldObj), entity.worldObj.isRemote))//.func_149851_a(entity.worldObj, here.intX(), here.intY(), here.intZ(), entity.worldObj.isRemote))
	            {
	                if (!entity.worldObj.isRemote)
	                {
	                    if (growable.canUseBonemeal(entity.worldObj, entity.worldObj.rand, here.getPos(), state))
	                    {
	                        growable.grow(entity.worldObj, entity.worldObj.rand, here.getPos(), state);
	    	                return;
	                    }
	                }
	            }
	        }
		}
		here.freeVectorFromPool();
	}
}
