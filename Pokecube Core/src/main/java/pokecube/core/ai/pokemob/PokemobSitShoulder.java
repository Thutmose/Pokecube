package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;

public class PokemobSitShoulder extends EntityAIBase
{
    private final EntityPokemob entity;
    private EntityPlayer        owner;
    private int                 cooldownTicks = 100;
    private boolean             isSittingOnShoulder;

    public PokemobSitShoulder(EntityPokemob p_i47415_1_)
    {
        this.entity = p_i47415_1_;
    }

    /** Returns whether the EntityAIBase should begin execution. */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.entity.getOwner();
        if (!(entitylivingbase instanceof EntityPlayer)) return false;
        boolean flag = entitylivingbase != null && !((EntityPlayer) entitylivingbase).isSpectator()
                && !((EntityPlayer) entitylivingbase).capabilities.isFlying && !entitylivingbase.isInWater()
                && !this.entity.getPokemonAIState(IPokemob.SITTING);
        if (!flag) cooldownTicks = 100;
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
        this.owner = (EntityPlayer) this.entity.getOwner();
        this.isSittingOnShoulder = false;
    }

    /** Keep ticking a continuous task that has already been started */
    public void updateTask()
    {
        if (!this.isSittingOnShoulder && !this.entity.getPokemonAIState(IPokemob.SITTING) && !this.entity.getLeashed())
        {
            if (this.entity.getEntityBoundingBox().intersects(this.owner.getEntityBoundingBox()))
            {
                this.isSittingOnShoulder = this.entity.moveToShoulder(this.owner);
            }
        }
    }
}