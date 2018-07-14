package pokecube.core.ai.pokemob;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityCreature;
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
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

/** This AI is what the pokemon does when attacked. It will sometimes result in
 * nearby pokemon of the same species trying to protect the pokemon, resulting
 * in horde like behavior. */
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

    public PokemobAIHurt(IPokemob entity, boolean callForHelp)
    {
        this.taskOwner = (EntityCreature) entity.getEntity();
        this.shouldCheckSight = true;
        this.nearbyOnly = true;
        this.entityCallsForHelp = callForHelp;
        this.setMutexBits(1);
        pokemob = entity;
    }

    /** Checks to see if this entity can find a short path to the given
     * target. */
    private boolean canEasilyReach(EntityLivingBase target)
    {
        if (this.taskOwner.getDistanceSq(target) > 16) return false;

        this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
        Path path = this.taskOwner.getNavigator().getPathToEntityLiving(target);
        if (path == null) { return false; }
        PathPoint pathpoint = path.getFinalPathPoint();

        if (pathpoint == null) { return false; }
        int i = pathpoint.x - MathHelper.floor(target.posX);
        int j = pathpoint.z - MathHelper.floor(target.posZ);
        return i * i + j * j <= 2.25D;
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    @Override
    public boolean shouldContinueExecuting()
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

            if (this.taskOwner.getDistanceSq(entitylivingbase) > d0 * d0) { return false; }
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

    protected double getTargetDistance()
    {
        IAttributeInstance iattributeinstance = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }

    /** A method used to see if an entity is a suitable target through a number
     * of checks. */
    protected boolean isSuitableTarget(EntityLivingBase target, boolean targetsPlayers)
    {
        if (target == null || target.isDead || !target.addedToChunk)
        {
            return false;
        }
        else if (target == this.taskOwner)
        {
            return false;
        }
        else if (!target.isEntityAlive())
        {
            return false;
        }
        else if (!this.taskOwner.canAttackClass(target.getClass()))
        {
            return false;
        }
        else
        {
            if (this.taskOwner instanceof IEntityOwnable && pokemob.getOwnerId() != null)
            {
                if (target instanceof IEntityOwnable
                        && pokemob.getOwnerId().equals(((IEntityOwnable) target).getOwnerId())) { return false; }

                if (target == pokemob.getOwner()) { return false; }
            }
            else if (target instanceof EntityPlayer && !targetsPlayers
                    && ((EntityPlayer) target).capabilities.disableDamage) { return false; }

            if (!this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor(target.posX),
                    MathHelper.floor(target.posY), MathHelper.floor(target.posZ))))
            {
                return false;
            }
            else if (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(target))
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
                        this.targetSearchStatus = this.canEasilyReach(target) ? 1 : 2;
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
        return i != this.revengeTimer && this.isSuitableTarget(this.taskOwner.getAttackTarget(), false);
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {

        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.lastSeenTime = 0;

        this.revengeTimer = this.taskOwner.getRevengeTimer();

        if (this.entityCallsForHelp && Math.random() > 0.95)
        {
            double d0 = this.getTargetDistance();
            List<? extends EntityCreature> list = this.taskOwner.getEntityWorld().getEntitiesWithinAABB(
                    this.taskOwner.getClass(),
                    new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ,
                            this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D).grow(d0,
                                    10.0D, d0));
            Iterator<? extends EntityCreature> iterator = list.iterator();

            while (iterator.hasNext())
            {
                IPokemob mob = CapabilityPokemob.getPokemobFor(iterator.next());
                if (mob == null) continue;
                if (this.taskOwner != mob && mob.getEntity().getAttackTarget() == null
                        && this.taskOwner.getAttackTarget() != null
                        && !mob.getEntity().isOnSameTeam(this.taskOwner.getAttackTarget())
                        && mob.getPokedexEntry().areRelated(pokemob.getPokedexEntry()))
                {
                    mob.getEntity().setAttackTarget(this.taskOwner.getAttackTarget());
                    mob.setPokemonAIState(IMoveConstants.ANGRY, true);
                }
            }
        }

        super.startExecuting();
    }
}
