package pokecube.core.ai.pokemob;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

/** This AI is what the pokemon does when attacked. It will sometimes result in
 * nearby pokemon of the same species trying to protect the pokemon, resulting
 * in horde like behavior.
 * 
 * @author Patrick */
public class PokemobAIHurt extends EntityAIBase
{

    boolean                  entityCallsForHelp;
    private int              revengeTimer;
    IPokemob                 pokemob;
    /** The entity that this task belongs to */
    protected EntityCreature taskOwner;
    /** If true, EntityAI targets must be able to be seen (cannot be blocked by
     * walls) to be suitable targets. */
    protected boolean        shouldCheckSight;
    /** When true, only entities that can be reached with minimal effort will be
     * targetted. */
    private boolean          nearbyOnly;
    /** When nearbyOnly is true: 0 -> No target, but OK to search; 1 -> Nearby
     * target found; 2 -> Target too far. */
    private int              targetSearchStatus;
    /** When nearbyOnly is true, this throttles target searching to avoid
     * excessive pathfinding. */
    private int              targetSearchDelay;

    private int              lastSeenTime;

    public PokemobAIHurt(EntityCreature entity, boolean callForHelp)
    {
        this.taskOwner = entity;
        this.shouldCheckSight = true;
        this.nearbyOnly = true;
        this.entityCallsForHelp = callForHelp;
        this.setMutexBits(1);
        pokemob = (IPokemob) taskOwner;
    }

    /** Checks to see if this entity can find a short path to the given
     * target. */
    private boolean canEasilyReach(EntityLivingBase p_75295_1_)
    {
        this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
        Path path = this.taskOwner.getNavigator().getPathToEntityLiving(p_75295_1_);

        if (path == null)
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = path.getFinalPathPoint();

            if (pathpoint == null)
            {
                return false;
            }
            else
            {
                int i = pathpoint.xCoord - MathHelper.floor_double(p_75295_1_.posX);
                int j = pathpoint.zCoord - MathHelper.floor_double(p_75295_1_.posZ);
                return i * i + j * j <= 2.25D;
            }
        }
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    @Override
    public boolean continueExecuting()
    {
        EntityLivingBase entitylivingbase = this.taskOwner.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        else
        {
            double d0 = this.getTargetDistance();

            if (this.taskOwner.getDistanceSqToEntity(entitylivingbase) > d0 * d0)
            {
                return false;
            }
            else
            {
                if (this.shouldCheckSight)
                {
                    if (this.taskOwner.getEntitySenses().canSee(entitylivingbase))
                    {
                        this.lastSeenTime = 0;
                    }
                    else if (++this.lastSeenTime > 60) { return false; }
                }

                return !(entitylivingbase instanceof EntityPlayerMP)
                        || !((EntityPlayerMP) entitylivingbase).interactionManager.isCreative();
            }
        }
    }

    protected double getTargetDistance()
    {
        IAttributeInstance iattributeinstance = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }

    /** A method used to see if an entity is a suitable target through a number
     * of checks. */
    protected boolean isSuitableTarget(EntityLivingBase p_75296_1_, boolean p_75296_2_)
    {
        if (p_75296_1_ == null)
        {
            return false;
        }
        else if (p_75296_1_ == this.taskOwner)
        {
            return false;
        }
        else if (!p_75296_1_.isEntityAlive())
        {
            return false;
        }
        else if (!this.taskOwner.canAttackClass(p_75296_1_.getClass()))
        {
            return false;
        }
        else
        {
            if (this.taskOwner instanceof IEntityOwnable && ((IEntityOwnable) this.taskOwner).getOwnerId() != null)
            {
                if (p_75296_1_ instanceof IEntityOwnable && ((IEntityOwnable) this.taskOwner).getOwnerId()
                        .equals(((IEntityOwnable) p_75296_1_).getOwnerId())) { return false; }

                if (p_75296_1_ == ((IEntityOwnable) this.taskOwner).getOwner()) { return false; }
            }
            else if (p_75296_1_ instanceof EntityPlayer && !p_75296_2_
                    && ((EntityPlayer) p_75296_1_).capabilities.disableDamage) { return false; }

            if (!this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor_double(p_75296_1_.posX),
                    MathHelper.floor_double(p_75296_1_.posY), MathHelper.floor_double(p_75296_1_.posZ))))
            {
                return false;
            }
            else if (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(p_75296_1_))
            {
                return false;
            }
            else
            {
                if (this.nearbyOnly)
                {
                    if (--this.targetSearchDelay <= 0)
                    {
                        this.targetSearchStatus = 0;
                    }

                    if (this.targetSearchStatus == 0)
                    {
                        this.targetSearchStatus = this.canEasilyReach(p_75296_1_) ? 1 : 2;
                    }

                    if (this.targetSearchStatus == 2) { return false; }
                }

                return true;
            }
        }
    }

    /** Resets the task */
    @Override
    public void resetTask()
    {
        ;
    }

    /** Returns whether the EntityAIBase should begin execution. */
    @Override
    public boolean shouldExecute()
    {
        int i = this.taskOwner.getRevengeTimer();
        return i != this.revengeTimer && this.isSuitableTarget(this.taskOwner.getAITarget(), false);
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {

        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.lastSeenTime = 0;
        // this.taskOwner.setAttackTarget(this.taskOwner.getAITarget());
        this.revengeTimer = this.taskOwner.getRevengeTimer();

        if (this.entityCallsForHelp && Math.random() > 0.95)
        {
            double d0 = this.getTargetDistance();
            List<? extends EntityCreature> list = this.taskOwner.worldObj.getEntitiesWithinAABB(
                    this.taskOwner.getClass(),
                    new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ,
                            this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)
                                    .expand(d0, 10.0D, d0));
            Iterator<? extends EntityCreature> iterator = list.iterator();

            while (iterator.hasNext())
            {
                IPokemob mob = (IPokemob) iterator.next();

                if (this.taskOwner != mob && ((EntityLiving) mob).getAttackTarget() == null
                        && this.taskOwner.getAttackTarget() != null
                        && !((EntityLiving) mob).isOnSameTeam(this.taskOwner.getAttackTarget())
                        && mob.getPokedexEntry().areRelated(pokemob.getPokedexEntry()))
                {
                    ((EntityLiving) mob).setAttackTarget(this.taskOwner.getAttackTarget());
                    mob.setPokemonAIState(IMoveConstants.ANGRY, true);
                }
            }
        }

        super.startExecuting();
    }
}
