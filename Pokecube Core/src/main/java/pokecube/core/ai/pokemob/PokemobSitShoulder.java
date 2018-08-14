package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

public class PokemobSitShoulder extends EntityAIBase
{
    private final EntityLiving entity;
    private final IPokemob     pokemob;
    private EntityPlayer       owner;
    private int                cooldownTicks = 100;
    private boolean            isSittingOnShoulder;

    public PokemobSitShoulder(EntityLiving entityIn)
    {
        this.entity = entityIn;
        this.pokemob = CapabilityPokemob.getPokemobFor(entityIn);
    }

    /** Returns whether the EntityAIBase should begin execution. */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = (EntityLivingBase) this.pokemob.getOwner();
        if (!(entitylivingbase instanceof EntityPlayer) || pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        boolean flag = entitylivingbase != null && !((EntityPlayer) entitylivingbase).isSpectator()
                && !((EntityPlayer) entitylivingbase).capabilities.isFlying && !entitylivingbase.isInWater()
                && !this.pokemob.getLogicState(LogicStates.SITTING);
        if (!flag) cooldownTicks = 100;
        if (cooldownTicks < -100) cooldownTicks = 100;
        return flag && cooldownTicks-- <= 0;
    }

    /** Determine if this AI Task is interruptible by a higher (= lower value)
     * priority task. All vanilla AITask have this value set to true. */
    public boolean isInterruptible()
    {
        return !this.isSittingOnShoulder;
    }

    /** Execute a one shot task or start executing a continuous task */
    public void startExecuting()
    {
        this.owner = (EntityPlayer) this.pokemob.getOwner();
        this.isSittingOnShoulder = false;
    }

    /** Keep ticking a continuous task that has already been started */
    public void updateTask()
    {
        if (!this.isSittingOnShoulder && !this.pokemob.getLogicState(LogicStates.SITTING) && !this.entity.getLeashed())
        {
            if (this.entity.getEntityBoundingBox().intersects(this.owner.getEntityBoundingBox()))
            {
                this.isSittingOnShoulder = this.pokemob.moveToShoulder(this.owner);
            }
        }
    }
}