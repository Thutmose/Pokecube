package pokecube.core.ai.utils;

import java.util.UUID;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import pokecube.core.utils.TimePeriod;

/**
 * Guards a given point. Constructor parameters:
 * <ul>
 * <li>EntityLiving owner - the entity this guard AI should work for
 * <li>BlockPos position - the position to guard; the entity's current position if <i>null</i>
 * <li>float roamingDistance - how far the AI should be able to stray from the guard point; at least 1.0f
 * <li>float pathSearchDistance - how far from the guard post should the AI still try to find its way back;
 *     at least the same as roamingDistance + 1.0f
 * <li>TimePeriod guardingPeriod - which part of the day should this entity guard its post; the full
 *     day if <i>null</i>
 * <li>boolean leaveTransports - if the AI should attempt to leave transportation vehicles
 *     (boats, minecarts, horses) if too far from the guard post
 * </ul>
 */
public class GuardAI extends EntityAIBase implements StorableAI
{
	private AttributeModifier goingHome = null;
	
	private final EntityLiving entity;
	public BlockPos pos;
	private final float roamDist;
	private final double roamDistSq;
	private final float pathSearchDist;
	private final double pathSearchDistSq;
	public TimePeriod guardPeriod;
	private final boolean shouldExitTransport;
	public GuardState state;
	public int cooldownTicks;
	
	public static GuardAI createFromNBT(EntityLiving living, NBTTagCompound data)
	{
		int posX = data.getInteger("PosX");
		int posY = data.getInteger("PosY");
		int posZ = data.getInteger("PosZ");
		int startTime = data.getInteger("GuardStartTime");
		int endTime = data.getInteger("GuardEndTime");
		GuardAI ai = new GuardAI(living, new BlockPos(posX, posY, posZ),
				data.getFloat("RoamDist"), data.getFloat("PathSearchDist"), new TimePeriod(startTime, endTime),
				data.getBoolean("ShouldExitTransport"));
		try
		{
			ai.state = GuardState.valueOf(data.getString("State"));
		}
		catch(Exception e)
		{
			ai.state = GuardState.IDLE;
		}
		ai.cooldownTicks = data.getInteger("CooldownTicks");
		return ai;
	}
	
	public GuardAI(EntityLiving owner, BlockPos position,
			float roamingDistance, float pathSearchDistance, TimePeriod guardingPeriod,
			boolean leaveTransports)
	{
		if( null == owner )
		{
			throw new IllegalArgumentException("Missing required argument: entity");
		}
		entity = owner;
		if( null == position )
		{
			pos = new BlockPos(MathHelper.floor_double(entity.posX + 0.5),
					MathHelper.floor_double(entity.posY),
					MathHelper.floor_double(entity.posZ + 0.5));
		}
		else
		{
			pos = position;
		}
		roamDist = Math.max(roamingDistance, 1.0f);
		roamDistSq = (double)roamDist * roamDist;
		pathSearchDist = Math.max(pathSearchDistance, roamingDistance + 1.0f);
		pathSearchDistSq = (double)pathSearchDist * pathSearchDist;
		guardPeriod = (null == guardingPeriod ? TimePeriod.fullDay : guardingPeriod);
		shouldExitTransport = leaveTransports;
		state = GuardState.IDLE;
		cooldownTicks = 0;
		setMutexBits(3); // Movement + Combat
		goingHome = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab60a7e1b0"), "Vernideas:GoingHome", pathSearchDistance / 16.0 - 1.0, 2);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		data.setInteger("PosX", pos.getX());
		data.setInteger("PosY", pos.getY());
		data.setInteger("PosZ", pos.getZ());
		data.setFloat("RoamDist", roamDist);
		data.setFloat("PathSearchDist", pathSearchDist);
		data.setInteger("GuardStartTime", guardPeriod.startTick);
		data.setInteger("GuardEndTime", guardPeriod.endTick);
		data.setBoolean("ShouldExitTransport", shouldExitTransport);
		data.setString("State", state.name());
		data.setInteger("CooldownTicks", cooldownTicks);
	}

	@Override
	public boolean shouldExecute()
	{
		if( null == entity || entity.isDead )
		{
			return false;
		}
		if( entity instanceof EntityAgeable && ((EntityAgeable)entity).getGrowingAge() < 0 )
		{
			// Entity is a child - don't guard anything until you grew up
			return false;
		}
		if( guardPeriod != TimePeriod.fullDay && !guardPeriod.contains((int)(entity.worldObj.getWorldTime() % 24000L)) )
		{
			// Not outside of the guard time
			return false;
		}
		if( null != entity.ridingEntity && !shouldExitTransport )
		{
			// If riding something and shouldExitTransport is false, keep on riding
			return false;
		}
		double distanceToGuardPointSq = entity.getDistanceSq(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		return (distanceToGuardPointSq > roamDistSq && distanceToGuardPointSq <= pathSearchDistSq);
	}

	@Override
	public void startExecuting()
	{
		state = GuardState.RUNNING;
		entity.getEntityAttribute(SharedMonsterAttributes.followRange).removeModifier(goingHome);
		entity.getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(goingHome);
		double speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
		entity.getNavigator().tryMoveToXYZ(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, speed);
	}

	@Override
    public boolean continueExecuting()
    {
		switch(state)
		{
			case RUNNING:
				if( entity.getNavigator().noPath()
						|| entity.getDistanceSq(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) < roamDistSq / 2 )
				{
					state = GuardState.COOLDOWN;
					return true;
				}
			case COOLDOWN:
				if( cooldownTicks < 20 * 15 )
				{
					++ cooldownTicks;
					return true;
				}
				else
				{
					cooldownTicks = 0;
					state = GuardState.IDLE;
					return false;
				}
			default:
				return false;
		}
    }

	@Override
	public void updateTask()
	{
		super.updateTask();
	}
	
	@Override
	public void resetTask()
	{
		super.resetTask();
		state = GuardState.IDLE;
		entity.getEntityAttribute(SharedMonsterAttributes.followRange).removeModifier(goingHome);
	}
	
	enum GuardState { IDLE, RUNNING, COOLDOWN }

}
