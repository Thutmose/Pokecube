package pokecube.adventures.ai.trainers;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import thut.api.maths.Vector3;

public class EntityAITrainer extends EntityAIBase {

	World world;

	// The entity (normally a player) that is the target of this trainer.
	EntityLivingBase target;
	Class targetClass;
	Vector3 loc = Vector3.getNewVectorFromPool();

	// The trainer Entity
	final EntityTrainer trainer;

	public EntityAITrainer(EntityTrainer trainer,
			Class<? extends EntityLivingBase> targetClass) {
		this.trainer = trainer;
		this.world = trainer.worldObj;
		this.setMutexBits(3);
		this.targetClass = targetClass;
	}

	@Override
	public boolean shouldExecute() {

		if (!trainer.isEntityAlive())
			return false;

		Vector3 here = loc.set(trainer);

		List targets = world.getEntitiesWithinAABB(targetClass, here.getAABB()
				.expand(16, 16, 16));
		for (Object o : targets) {
			EntityLivingBase e = (EntityLivingBase) o;
			if (Vector3.isVisibleEntityFromEntity(trainer, e)) {
				target = e;
				break;
			}
		}
		if (target instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) target;
			if (player.capabilities.isCreativeMode)
				target = null;
			else if(trainer.friendlyCooldown > 0)
				target = null;
			else if(trainer instanceof EntityLeader)
			{
				if(((EntityLeader)trainer).hasDefeated(target))
					target = null;
			}
		}

		for (int i = 0; i < 6; i++)
			trainer.attackCooldown[i]--;
		trainer.cooldown--;
		
		trainer.setTrainerTarget(target);
		return target != null;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		trainer.setTrainerTarget(target);
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		double distance = trainer.getDistanceSqToEntity(target);
		trainer.faceEntity(target, trainer.rotationPitch, trainer.rotationYaw);
		if (distance > 100) {

		} else {
			doAggression();
		}
	}

	void doAggression() {
		boolean angry = trainer.getAITarget() != null;
		EntityLivingBase target = null;

		if (angry) {
			target = trainer.getAITarget() != null ? trainer.getAITarget()
					: trainer.getAttackTarget() != null ? trainer
							.getAttackTarget() : null;
			angry = target != null;
			if (!Vector3.isVisibleEntityFromEntity(trainer, target)) {
				angry = false;
				target = null;
			}
		}
		if(trainer instanceof EntityLeader)
		{
			if(((EntityLeader)trainer).hasDefeated(target))
			{
				target = null;
				return;
			}
		}
		if (angry && !trainer.worldObj.isRemote && trainer.cooldown < 0) {
			trainer.setTrainerTarget(target);
			trainer.throwCubeAt(target);

		}

	}

}
