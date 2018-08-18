package pokecube.core.ai.thread.aiRunnables.combat;

import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.moves.MovesUtils;
import thut.api.TickHandler;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

/** This is the IAIRunnable for managing which attack is used when. It
 * determines whether the pokemob is in range, manages pathing to account for
 * range issues, and also manage auto selection of moves for wild or hunting
 * pokemobs.<br>
 * <br>
 * It also manages the message to notify the player that a wild pokemob has
 * decided to battle, as well as dealing with combat between rivals over a mate.
 * It is the one to queue the attack for the pokemob to perform. */
public class AIAttack extends AIBase implements IAICombat
{
    public final EntityLiving attacker;
    public final IPokemob     pokemob;
    /** The target being attacked. */
    EntityLivingBase          entityTarget;
    /** IPokemob version of entityTarget. */
    IPokemob                  pokemobTarget;
    /** Used to check whether we need to try swapping target, only check this
     * once per second or so. */
    int                       targetTestTime;
    /** Where the target is/was for attack. */
    Vector3                   targetLoc   = Vector3.getNewVector();
    /** Move we are using */
    Move_Base                 attack;
    Matrix3                   targetBox   = new Matrix3();
    Matrix3                   attackerBox = new Matrix3();

    /** Temp vectors for checking things. */
    Vector3                   v           = Vector3.getNewVector();
    Vector3                   v1          = Vector3.getNewVector();
    Vector3                   v2          = Vector3.getNewVector();
    /** Speed for pathing. */
    double                    movementSpeed;

    /** Used to determine when to give up attacking. */
    protected int             chaseTime;
    /** Also used to determine when to give up attacking. */
    protected boolean         canSee      = false;
    /** Used for when to execute attacks. */
    protected int             delayTime   = -1;
    boolean                   running     = false;

    public AIAttack(IPokemob entity)
    {
        this.attacker = entity.getEntity();
        this.pokemob = entity;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        this.setMutex(3);
    }

    private void checkMateFight(IPokemob pokemob)
    {
        if (pokemob.getCombatState(CombatStates.MATEFIGHT))
        {
            if (pokemobTarget != null)
            {
                if (pokemobTarget.getEntity().getHealth() < pokemobTarget.getEntity().getMaxHealth() / 1.5f)
                {
                    setCombatState(this.pokemob, CombatStates.MATEFIGHT, false);
                    setCombatState(pokemobTarget, CombatStates.MATEFIGHT, false);
                    setCombatState(this.pokemob, CombatStates.ANGRY, false);
                    setCombatState(pokemobTarget, CombatStates.ANGRY, false);
                    pokemobTarget.getEntity().setAttackTarget(null);
                }
            }
            else
            {
                setCombatState(this.pokemob, CombatStates.MATEFIGHT, false);
            }
        }
    }

    public boolean continueExecuting()
    {
        entityTarget = attacker.getAttackTarget();
        return entityTarget != null && !entityTarget.isDead || !pokemob.getCombatState(CombatStates.ANGRY);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        canSee = false;
        if (running)
        {
            attacker.getEntityData().setLong("lastAttackTick", attacker.getEntityWorld().getTotalWorldTime());
            if (entityTarget != null)
            {
                double dist = this.attacker.getDistanceSq(this.entityTarget.posX, this.entityTarget.posY,
                        this.entityTarget.posZ);
                canSee = dist < 1 || Vector3.isVisibleEntityFromEntity(attacker, entityTarget);

                if (CapabilityPokemob.getPokemobFor(entityTarget) == null && attacker.ticksExisted > targetTestTime
                        && pokemob.getCombatState(CombatStates.ANGRY))
                {
                    ForgeHooks.onLivingSetAttackTarget(attacker, entityTarget);
                    targetTestTime = attacker.ticksExisted + 20;
                }
            }
        }
    }

    @Override
    public void reset()
    {
        if (running)
        {
            running = false;
            addEntityPath(attacker, null, movementSpeed);
        }
    }

    @Override
    public void run()
    {
        if (!continueExecuting())
        {
            reset();
            return;
        }
        Path path;
        // Check if the pokemob has an active move being used, if so return
        if (pokemob.getActiveMove() != null) { return; }
        if (!running)
        {

            if (!(attack == null || ((attack.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0))
                    && !pokemob.getGeneralState(GeneralStates.CONTROLLED))
            {
                path = this.attacker.getNavigator().getPathToEntityLiving(entityTarget);
                addEntityPath(attacker, path, movementSpeed);
            }
            targetLoc.set(entityTarget);
            this.chaseTime = 0;
            running = true;
            /** Don't want to notify if the pokemob just broke out of a
             * pokecube. */
            boolean previousCaptureAttempt = !EntityPokecubeBase.canCaptureBasedOnConfigs(pokemob);

            /** Check if it should notify the player of agression, and do so if
             * it should. */
            if (!previousCaptureAttempt && PokecubeMod.core.getConfig().pokemobagresswarning
                    && entityTarget instanceof EntityPlayerMP && !(entityTarget instanceof FakePlayer)
                    && !pokemob.getGeneralState(GeneralStates.TAMED)
                    && ((EntityPlayer) entityTarget).getRevengeTarget() != attacker
                    && ((EntityPlayer) entityTarget).getLastAttackedEntity() != attacker)
            {
                ITextComponent message = new TextComponentTranslation("pokemob.agress",
                        pokemob.getPokemonDisplayName().getFormattedText());
                try
                {
                    entityTarget.sendMessage(message);
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error with message for " + entityTarget, e);
                }
                pokemob.setAttackCooldown(PokecubeMod.core.getConfig().pokemobagressticks);
            }
            return;
        }

        // Look at the target
        this.attacker.getLookHelper().setLookPositionWithEntity(entityTarget, 30.0F, 30.0F);

        // Check if it is fighting over a mate, and deal with it accordingly.
        checkMateFight(pokemob);

        // No executing move state with no target location.
        if (pokemob.getCombatState(CombatStates.EXECUTINGMOVE) && targetLoc.isEmpty())
        {
            setCombatState(pokemob, CombatStates.EXECUTINGMOVE, false);
        }

        // If it has been too long since last seen the target, give up.
        if (chaseTime > 200)
        {
            setCombatState(pokemob, CombatStates.ANGRY, false);
            addEntityPath(attacker, null, movementSpeed);
            chaseTime = 0;
            if (PokecubeCore.debug)
            {
                PokecubeMod.log(Level.INFO, "Too Long Chase, Forgetting Target: " + attacker + " " + entityTarget);
            }
            // Send deagress message and put mob on cooldown.
            ITextComponent message = new TextComponentTranslation("pokemob.deagress.timeout",
                    pokemob.getPokemonDisplayName().getFormattedText());
            try
            {
                entityTarget.sendMessage(message);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error with message for " + entityTarget, e);
            }
            pokemob.setAttackCooldown(PokecubeMod.core.getConfig().pokemobagressticks);
            return;
        }

        double var1 = (double) (this.attacker.width * 2.0F) * (this.attacker.width * 2.0F);
        boolean distanced = false;
        boolean self = false;
        Move_Base move = null;
        double dist = this.attacker.getDistanceSq(this.entityTarget.posX, this.entityTarget.posY,
                this.entityTarget.posZ);

        move = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));

        if (move == null) move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
        // Check to see if the move is ranged, contact or self.
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
        {
            var1 = PokecubeMod.core.getConfig().rangedAttackDistance
                    * PokecubeMod.core.getConfig().rangedAttackDistance;
            distanced = true;
        }
        else if (PokecubeMod.core.getConfig().contactAttackDistance > 0)
        {
            var1 = PokecubeMod.core.getConfig().contactAttackDistance
                    * PokecubeMod.core.getConfig().contactAttackDistance;
            distanced = true;
        }
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0)
        {
            self = true;
        }

        delayTime = pokemob.getAttackCooldown();
        boolean canUseMove = MovesUtils.canUseMove(pokemob);
        if (!canUseMove) return;
        boolean shouldPath = delayTime <= 0;
        boolean inRange = false;

        // Checks to see if the target is in range.
        if (distanced)
        {
            inRange = dist < var1;
        }
        else
        {
            if (attacker.getParts() != null)
            {
                for (Entity part : attacker.getParts())
                {
                    if (inRange) break;

                    float attackerLength = part.width;
                    float attackerHeight = part.height;
                    float attackerWidth = part.width;

                    float attackedLength = entityTarget.width;
                    float attackedHeight = entityTarget.height;
                    float attackedWidth = entityTarget.width;

                    float dx = (float) (attacker.posX - entityTarget.posX);
                    float dz = (float) (attacker.posZ - entityTarget.posZ);
                    float dy = (float) (attacker.posY - entityTarget.posY);

                    AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, attackerWidth, attackerHeight, attackerLength);
                    AxisAlignedBB box2 = new AxisAlignedBB(dx, dy, dz, dx + attackedWidth, dy + attackedHeight,
                            dz + attackedLength);
                    inRange = box.intersects(box2);// intersects 1.12
                }
            }
            else
            {
                float attackerLength = pokemob.getPokedexEntry().length * pokemob.getSize() + 0.5f;
                float attackerHeight = pokemob.getPokedexEntry().height * pokemob.getSize() + 0.5f;
                float attackerWidth = pokemob.getPokedexEntry().height * pokemob.getSize() + 0.5f;

                attackerLength = Math.max(attackerLength, attackerHeight);
                attackerWidth = Math.max(attackerWidth, attackerHeight);
                attackerLength = Math.max(attackerLength, attackerWidth);
                attackerWidth = attackerLength;

                float attackedLength = entityTarget.width;
                float attackedHeight = entityTarget.height;
                float attackedWidth = entityTarget.width;

                float dx = (float) (attacker.posX - entityTarget.posX);
                float dz = (float) (attacker.posZ - entityTarget.posZ);
                float dy = (float) (attacker.posY - entityTarget.posY);

                AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, attackerWidth, attackerHeight, attackerLength);
                AxisAlignedBB box2 = new AxisAlignedBB(dx, dy, dz, dx + attackedWidth, dy + attackedHeight,
                        dz + attackedLength);
                inRange = box.intersects(box2);// intersects 1.12

            }
        }
        if (self)
        {
            inRange = true;
            targetLoc.set(attacker);
        }

        // If can't see, increment the timer for giving up later.
        if (!canSee)
        {
            chaseTime++;
            if (!pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            {
                targetLoc.set(entityTarget).addTo(0, entityTarget.height / 2, 0);
            }
            // Try to path to target if you can't see it, regardless of what
            // move you have selected.
            shouldPath = true;
        }
        else
        {
            // Otherwise set timer to 0, and if newly executing the move, set
            // the target location as a "aim". This aiming is done so that when
            // the move is fired, it is fired at the location, not the target,
            // giving option to dodge.
            chaseTime = 0;
            if (!pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            {
                targetLoc.set(entityTarget).addTo(0, entityTarget.height / 2, 0);
            }
        }

        boolean delay = false;
        // Check if the attack should, applying a new delay if this is the
        // case..
        if ((inRange || self))
        {
            if (canSee || self)
            {
                if (delayTime <= 0 && (attacker.addedToChunk))
                {
                    delayTime = pokemob.getAttackCooldown();
                    delay = canUseMove;
                }
                shouldPath = false;
                setCombatState(pokemob, CombatStates.EXECUTINGMOVE, true);
            }
        }
        else
        {
            setCombatState(pokemob, CombatStates.EXECUTINGMOVE, false);
        }

        // If all the conditions match, queue up an attack.
        if (!targetLoc.isEmpty() && delay && inRange)
        {
            // If the target is not trying to dodge, and the move allows it,
            // then
            // set target location to where the target is now. This is so that
            // it can use the older postion set above, lowering the accuracy of
            // move use, allowing easier dodging.
            if ((pokemobTarget != null && !pokemobTarget.getCombatState(CombatStates.DODGING))
                    || !(pokemobTarget != null) || attack.move.isNotIntercepable())
            {
                targetLoc.set(entityTarget).addTo(0, entityTarget.height / 2, 0);
            }
            else
            {

            }
            // Tell the target no need to try to dodge anymore, move is fired.
            if (pokemobTarget != null)
            {
                setCombatState(pokemobTarget, CombatStates.DODGING, false);
            }
            // Swing arm for effect.
            if (this.attacker.getHeldItemMainhand() != null)
            {
                this.attacker.swingArm(EnumHand.MAIN_HAND);
            }
            // Apply the move.
            float f = (float) targetLoc.distToEntity(attacker);
            if (attacker.addedToChunk)
            {
                if (!entityTarget.isDead)
                {
                    addMoveInfo(attacker.getEntityId(), entityTarget.getEntityId(), attacker.dimension,
                            targetLoc.copy(), f);
                }
                // Reset executing move and no item use status now that we have
                // used a move.
                setCombatState(pokemob, CombatStates.EXECUTINGMOVE, false);
                setCombatState(pokemob, CombatStates.NOITEMUSE, false);
                targetLoc.clear();
                shouldPath = false;
                delayTime = pokemob.getAttackCooldown();
            }
        }
        // If the conditions that failed were due to distance, try to start
        // leaping to close distance.
        else if (shouldPath && !(distanced || self) && !pokemob.getCombatState(CombatStates.LEAPING))
        {
            setCombatState(pokemob, CombatStates.EXECUTINGMOVE, true);
            setCombatState(pokemob, CombatStates.LEAPING, true);
            if (PokecubeCore.debug)
            {
                PokecubeMod.log(Level.INFO, "Set To Leap: " + attacker);
            }
        }
        // If there is a target location, and it should path to it, queue a path
        // for the mob.
        if (!targetLoc.isEmpty() && shouldPath)
        {
            path = this.attacker.getNavigator().getPathToXYZ(targetLoc.x, targetLoc.y, targetLoc.z);
            if (path != null) addEntityPath(attacker, path, movementSpeed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(attacker.dimension);
        if (world == null) return false;
        EntityLivingBase var1 = attacker.getAttackTarget();
        // No target, we can't do anything, so return false
        if (var1 == null)
        {
            if (attacker.getNavigator().noPath() && pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            {
                setCombatState(pokemob, CombatStates.EXECUTINGMOVE, false);
            }
            return false;
        }
        // If either us, or target is dead, or about to be so (0 health) return
        // false
        if (var1.isDead || var1.getHealth() <= 0 || attacker.getHealth() <= 0 || attacker.isDead) { return false; }

        // If we do have the target, but are not angry, return false.
        if (!pokemob.getCombatState(CombatStates.ANGRY)) return false;

        // Set target, set attack, return true
        attack = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));
        entityTarget = var1;
        if (attack == null) attack = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
        return true;
    }
}
