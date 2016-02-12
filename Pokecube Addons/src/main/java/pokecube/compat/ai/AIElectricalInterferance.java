package pokecube.compat.ai;

import java.util.HashSet;
import java.util.Set;

import cofh.api.energy.IEnergyStorage;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;

public class AIElectricalInterferance extends EntityAIBase
{
	static boolean init = true;
	static boolean eln = false;
	
	final IPokemob pokemob;
	final Vector3 mobLoc = Vector3.getNewVectorFromPool();
	final EntityLiving entity;
	final Set<?> linesAffectedThisTick = new HashSet<Object>();
	
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
		int range = 4;
		linesAffectedThisTick.clear();

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
		
		Vector3 toFill = Vector3.getNewVectorFromPool();
		range *= 2;
		for(int i = 0; i<range*range*range; i++)
		{
			Cruncher.indexToVals(i, toFill);

			mobLoc.set(pokemob).addTo(toFill);
			
			TileEntity tile = mobLoc.getTileEntity(entity.worldObj);

			if(tile instanceof IEnergyStorage)
			{
				int radSq = (int) toFill.magSq();
				radSq = Math.max(1, radSq);
				IEnergyStorage energySource = (IEnergyStorage) tile;
				int num = statFactor / radSq;
				
				if(num==0)
					return;
				energySource.receiveEnergy(num, false);
			}
			else if(tile!=null && eln)
			{
				try
				{
					ELNInterfacer.doELNInterference(entity, (int) toFill.mag(), statFactor, tile);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		toFill.freeVectorFromPool();
	}
	
}
