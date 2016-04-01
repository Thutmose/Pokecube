package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import pokecube.core.interfaces.IPokemob;

/**
 * Swimming AI, needed to make water pokemon not float on top of the ocean sometimes.
 * @author Patrick
 *
 */
public class PokemobAISwimming extends EntityAIBase {

	private EntityLiving theEntity;
	IPokemob pokemob;
	private boolean isWaterMob = false;

	public PokemobAISwimming(EntityLiving p_i1624_1_) {
		this.theEntity = p_i1624_1_;
		this.setMutexBits(4);
		pokemob = (IPokemob) theEntity;
		isWaterMob = pokemob.getPokedexEntry().swims();
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (isWaterMob)
			return false;
		return this.theEntity.isInWater()
				|| this.theEntity.isInLava();
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (this.theEntity.getRNG().nextFloat() < 0.8F) 
		{
			this.theEntity.getJumpHelper().setJumping();
		}
	}

}
