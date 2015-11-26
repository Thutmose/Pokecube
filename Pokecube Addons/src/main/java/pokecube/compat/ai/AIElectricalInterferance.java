package pokecube.compat.ai;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.maths.ExplosionCustom.ClientUpdateInfo;
import thut.api.maths.ExplosionCustom.ExplosionVictimTicker;

public class AIElectricalInterferance extends EntityAIBase
{
	static boolean init = true;
	static boolean eln = false;
	
	final IPokemob pokemob;
	final Vector3 mobLoc = Vector3.getNewVectorFromPool();
	final EntityLiving entity;
	final Set linesAffectedThisTick = new HashSet();
	
	public AIElectricalInterferance(IPokemob pokemob_)
	{
		pokemob = pokemob_;
		entity = (EntityLiving) pokemob;
		if(init)
		{
			init = false;
			try
			{
				new ELNInterfacer();
				eln = true;
			}
			catch (Exception e)
			{
				System.out.println("Electrical Age not found, not adding interference for it");
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean shouldExecute()
	{
		return pokemob.isType(PokeType.electric) && entity.ticksExisted % 5 == 0;
	}
	@Override
    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void updateTask()
	{
		int range = 4, x,y,z;
		int currentRadius = 0, subIndex = 0;
		int nextRadius = 1;
		linesAffectedThisTick.clear();
		int currentRadSq = 0;
		int nextRadCb = 1;

		int statFactor = pokemob.getActualStats()[1] + pokemob.getActualStats()[3];
		
		statFactor /= 2;
		float timescale = 1f;
		int tempFactor = 0;
		float number = 10f;
		for(int i = 0; i<number; i++)
		{
			tempFactor += statFactor * MathHelper.cos((entity.ticksExisted + i/number)/timescale)/number;
		}
		statFactor = tempFactor;
		
		int radCbDiff = 1;
		int radSqDiff = 1;
		int[] toFill = new int[3];
		range *= 2;
		for(int i = 0; i<range*range*range; i++)
		{
			if (i >= nextRadCb)
			{
				nextRadius++;
				currentRadius++;
				int temp = (2 * nextRadius - 1);
				nextRadCb = temp * temp * temp;
				temp = (2 * currentRadius - 1);
				currentRadSq = temp * temp * temp;
				radCbDiff = nextRadCb - currentRadSq;
				radSqDiff = (2 * nextRadius - 1) * (2 * nextRadius - 1) - temp * temp;
			}
			subIndex = (i - currentRadSq);

			Cruncher.indexToVals(currentRadius, subIndex, radSqDiff, radCbDiff, toFill);
			x = toFill[0];
			z = toFill[1];
			y = toFill[2];

			mobLoc.set(pokemob).addTo(x, y, z);
			
			TileEntity tile = mobLoc.getTileEntity(entity.worldObj);

			if(tile instanceof IEnergyHandler)
			{
				int radSq = (currentRadius * currentRadius);
				radSq = Math.max(1, radSq);
				IEnergyHandler energySource = (IEnergyHandler) tile;
				int num = statFactor / radSq;
				
				if(num==0)
					return;
				directions:
				for(EnumFacing dir: EnumFacing.VALUES)
				{
					if(num < 0)
					{
						if(energySource.extractEnergy(dir, num, false)!=0)
							break directions;
					}
					else
					{
						if(energySource.receiveEnergy(dir, num, false)!=0)
							break directions;
					}
				}
			}
			else if(tile!=null && eln)
			{
				try
				{
					ELNInterfacer.doELNInterference(entity, currentRadius, statFactor, tile);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
}
