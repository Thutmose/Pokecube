package pokecube.core.ai.utils;

import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.BlockPos;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability.GuardState;
import pokecube.core.utils.TimePeriod;

/** Guards a given point. Constructor parameters:
 * <ul>
 * <li>EntityLiving owner - the entity this guard AI should work for
 * <li>BlockPos position - the position to guard; the entity's current position
 * if <i>null</i>
 * <li>float roamingDistance - how far the AI should be able to stray from the
 * guard point; at least 1.0f
 * <li>float pathSearchDistance - how far from the guard post should the AI
 * still try to find its way back; at least the same as roamingDistance + 1.0f
 * <li>TimePeriod guardingPeriod - which part of the day should this entity
 * guard its post; the full day if <i>null</i>
 * <li>boolean leaveTransports - if the AI should attempt to leave
 * transportation vehicles (boats, minecarts, horses) if too far from the guard
 * post
 * </ul>
*/
public class GuardAI extends EntityAIBase
{
    private AttributeModifier       goingHome = null;

    public final IGuardAICapability capability;
    private final EntityLiving      entity;
    public int                      cooldownTicks;

    public GuardAI(EntityLiving entity, IGuardAICapability capability)
    {
        this.entity = entity;
        this.capability = capability;
        goingHome = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0"), "Pokecube:GoingHome",
                32 / 16.0 - 1.0, 2);
    }

    @Override
    public boolean continueExecuting()
    {
        switch (capability.getState())
        {
        case RUNNING:
            if (entity.getNavigator().noPath()
                    || entity.getDistanceSq(capability.getPos()) < capability.getRoamDistance()
                            * capability.getRoamDistance() / 2)
            {
                capability.setState(GuardState.COOLDOWN);
                return true;
            }
        case COOLDOWN:
            if (cooldownTicks < 20 * 15)
            {
                ++cooldownTicks;
                return true;
            }
            else
            {
                cooldownTicks = 0;
                capability.setState(GuardState.IDLE);
                return false;
            }
        default:
            return false;
        }
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        capability.setState(GuardState.IDLE);
        entity.getEntityAttribute(SharedMonsterAttributes.followRange).removeModifier(goingHome);
    }

    public void setPos(BlockPos pos)
    {
        capability.setPos(pos);
    }

    public void setTimePeriod(TimePeriod time)
    {
        capability.setActiveTime(time);
    }

    @Override
    public boolean shouldExecute()
    {
        if (null == entity || entity.isDead || capability.getActiveTime() == null
                || capability.getPos() == null) { return false; }
        if (capability.getActiveTime() != TimePeriod.fullDay
                && !capability.getActiveTime().contains((int) (entity.worldObj.getWorldTime() % 24000L)))
        {
            return false;
        }
        BlockPos pos = capability.getPos();
        if (pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) return false;
        double distanceToGuardPointSq = entity.getDistanceSq(capability.getPos());
        return (distanceToGuardPointSq > capability.getRoamDistance() * capability.getRoamDistance());
    }

    @Override
    public void startExecuting()
    {
        capability.setState(GuardState.RUNNING);
        entity.getEntityAttribute(SharedMonsterAttributes.followRange).removeModifier(goingHome);
        entity.getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(goingHome);
        double speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        entity.getNavigator().tryMoveToXYZ(capability.getPos().getX() + 0.5, capability.getPos().getY(),
                capability.getPos().getZ() + 0.5, speed);
    }

    @Override
    public void updateTask()
    {
        super.updateTask();
    }
}
