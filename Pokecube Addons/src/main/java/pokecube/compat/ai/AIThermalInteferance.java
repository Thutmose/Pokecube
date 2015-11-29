package pokecube.compat.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class AIThermalInteferance extends EntityAIBase
{
	final IPokemob pokemob;
	final Vector3 mobLoc = Vector3.getNewVectorFromPool();
	final Entity entity;
	double tempLastTick = 0;

	public AIThermalInteferance(IPokemob pokemob_)
	{
		pokemob = pokemob_;
		entity = (Entity) pokemob;
		tempLastTick = -12345;
	}

	@Override
	public boolean shouldExecute()
	{
		return AbilityManager.hasAbility("flame body", pokemob) || AbilityManager.hasAbility("magma armor", pokemob);// && entity.ticksExisted % 20 == 0;
	}
	@Override
    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void updateTask()
	{
		if(pokemob.getPokemonAIState(IMoveConstants.SLEEPING))
		{
			double heating = 0;
			if(entity.getEntityData().hasKey("heatOut"))
			{
				heating = entity.getEntityData().getDouble("heatOut");
			}
			if(heating <= 0)
			{
				pokemob.setPokemonAIState(IMoveConstants.SLEEPING, false);
				pokemob.setPokemonAIState(IPokemob.TIRED, false);
			}
			else
			{
				entity.getEntityData().setDouble("heatOut", Math.max(heating-0.01 * pokemob.getLevel(), 0));
			}
		}
		else
		{
			double heating = 0;
			if(entity.getEntityData().hasKey("heatOut"))
			{
				heating = entity.getEntityData().getDouble("heatOut");
			}
			if(heating > pokemob.getLevel())
			{
				entity.getEntityData().setDouble("heatOut", 150 - pokemob.getLevel());
				pokemob.setPokemonAIState(IMoveConstants.SLEEPING, true);
				pokemob.setPokemonAIState(IPokemob.TIRED, true);
			}
//			heating  = 100;
			double temp = tempLastTick;
			mobLoc.set(pokemob);
			mobLoc.addTo(0, 0.5, 0);
			TileEntity tile = mobLoc.getTileEntity(entity.worldObj, EnumFacing.DOWN);
			if(tile instanceof IHeatExchanger)
			{
				IHeatExchanger heater = (IHeatExchanger) tile;
				IHeatExchangerLogic logic = heater.getHeatExchangerLogic(EnumFacing.UP);
				if(logic!=null)
				{
					temp = logic.getTemperature();
					if(tempLastTick == -12345)
						tempLastTick = temp;
					
					double diff = (temp - tempLastTick) * 1 / logic.getThermalCapacity();
					logic.addHeat(1 + pokemob.getLevel()/50d);
					if(diff>0)
					{
						heating += diff;
//						System.out.println(temp+" "+diff+" "+heating);
						IHungrymob mob = (IHungrymob) pokemob;
						mob.setHungerTime(mob.getHungerTime()+150 - pokemob.getLevel());
					}
				}
				
				if(heating > 0)
				{
					entity.getEntityData().setDouble("heatOut", Math.max(heating-0.01 * pokemob.getLevel(), 0));
				}
				tempLastTick = temp;
			}
			else if(tile!=null)//TODO && eln
			{
				doElnThermalInterference();
			}
		}
	}

	//TODO write ELN thermal effects
	private void doElnThermalInterference(){}
	
}
