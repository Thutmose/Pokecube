package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.core.ai.thread.IAICombat;
import pokecube.core.commands.CommandTools;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.TickHandler;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class AIAttack extends AIBase implements IAICombat
{
    public final EntityLiving attacker;
    EntityLivingBase          entityTarget;
    Vector3                   targetLoc   = Vector3.getNewVector();
    Move_Base                 attack;
    Matrix3                   targetBox   = new Matrix3();
    Matrix3                   attackerBox = new Matrix3();

    Vector3                   v           = Vector3.getNewVector();
    Vector3                   v1          = Vector3.getNewVector();
    Vector3                   v2          = Vector3.getNewVector();
    double                    movementSpeed;

    protected int             chaseTime;
    protected int             delayTime   = -1;

    boolean                   running     = false;

    public AIAttack(EntityLiving par1EntityLiving)
    {
        this.attacker = par1EntityLiving;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        this.setMutex(3);
    }

    private void applyDelay(IPokemob attacker, String moveName, boolean distanced)
    {
        byte[] mods = attacker.getModifiers();
        int cd = PokecubeMod.core.getConfig().attackCooldown;
        if (entityTarget instanceof EntityPlayer) cd *= 2;
        double accuracyMod = Tools.modifierToRatio(mods[6], true);
        double moveMod = MovesUtils.getDelayMultiplier(attacker, moveName);
        delayTime = (int) (cd * moveMod / accuracyMod);
    }

    private void checkMateFight(IPokemob pokemob)
    {
        if (pokemob.getPokemonAIState(IMoveConstants.MATEFIGHT))
        {
            if (entityTarget instanceof IPokemob)
            {
                IPokemob target = (IPokemob) entityTarget;
                if (((EntityLiving) target).getHealth() < ((EntityLiving) target).getMaxHealth() / 1.5f)
                {
                    setPokemobAIState((IPokemob) attacker, IMoveConstants.MATEFIGHT, false);
                    setPokemobAIState(target, IMoveConstants.MATEFIGHT, false);
                    addTargetInfo(attacker, null);
                    pokemob.setPokemonAIState(IMoveConstants.ANGRY, false);
                    ((EntityLiving) target).setAttackTarget(null);
                    target.setPokemonAIState(IMoveConstants.ANGRY, false);
                }
            }
            else
            {
                setPokemobAIState((IPokemob) attacker, IMoveConstants.MATEFIGHT, false);
            }
        }
    }

    public boolean continueExecuting()
    {
        entityTarget = attacker.getAttackTarget();

        if (entityTarget != null && entityTarget.isDead)
        {
            addTargetInfo(attacker.getEntityId(), -1, attacker.dimension);
            entityTarget = null;
        }
        return entityTarget != null && !entityTarget.isDead;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (running)
        {
            attacker.getEntityData().setLong("lastAttackTick", attacker.getEntityWorld().getTotalWorldTime());
        }
    }

    @Override
    public void reset()
    {
        if (running)
        {
            running = false;
            addEntityPath(attacker.getEntityId(), attacker.dimension, null, movementSpeed);
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
        if (!running)
        {
            if (!(attack == null || ((attack.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0))
                    && !attacker.isBeingRidden())
            {
                path = this.attacker.getNavigator().getPathToEntityLiving(entityTarget);
                addEntityPath(attacker.getEntityId(), attacker.dimension, path, movementSpeed);
            }
            targetLoc.set(entityTarget);
            this.chaseTime = 0;
            running = true;
            boolean previousCaptureAttempt = attacker.getEntityData().hasKey("lastCubeTime");
            if (previousCaptureAttempt)
            {
                long time = attacker.getEntityData().getLong("lastCubeTime");
                if (attacker.getEntityWorld().getTotalWorldTime() - time > 20 * 60 * 2)
                {
                    previousCaptureAttempt = false;
                }
            }
            if (!previousCaptureAttempt && PokecubeMod.core.getConfig().pokemobagresswarning && delayTime == -1
                    && entityTarget instanceof EntityPlayer
                    && !((IPokemob) attacker).getPokemonAIState(IMoveConstants.TAMED)
                    && ((EntityPlayer) entityTarget).getLastAttacker() != attacker
                    && ((EntityPlayer) entityTarget).getAITarget() != attacker)
            {
                delayTime = PokecubeMod.core.getConfig().pokemobagressticks;
                ITextComponent message = CommandTools.makeTranslatedMessage("pokemob.agress", "red",
                        ((IPokemob) attacker).getPokemonDisplayName().getFormattedText());
                entityTarget.addChatMessage(message);
            }
            else if (delayTime < 0)
            {
                delayTime = 0;
            }
            return;
        }

        this.attacker.getLookHelper().setLookPositionWithEntity(entityTarget, 30.0F, 30.0F);

        IPokemob pokemob = (IPokemob) attacker;

        checkMateFight(pokemob);

        if (pokemob.getPokemonAIState(IMoveConstants.EXECUTINGMOVE) && targetLoc.isEmpty())
        {
            setPokemobAIState((IPokemob) attacker, IMoveConstants.EXECUTINGMOVE, false);
        }

        if (chaseTime > 200)
        {
            addTargetInfo(attacker.getEntityId(), -1, attacker.dimension);
            pokemob.setPokemonAIState(IMoveConstants.ANGRY, false);
            addEntityPath(attacker.getEntityId(), attacker.dimension, null, movementSpeed);
            return;
        }
        double var1 = (double) (this.attacker.width * 2.0F) * (this.attacker.width * 2.0F);
        boolean distanced = false;
        boolean self = false;
        Move_Base move = null;
        double dist = this.attacker.getDistanceSq(this.entityTarget.posX, this.entityTarget.posY,
                this.entityTarget.posZ);
        boolean canSee = dist < 1 || Vector3.isVisibleEntityFromEntity(attacker, entityTarget);
        if (attacker instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) attacker;
            move = MovesUtils.getMoveFromName(mob.getMove(mob.getMoveIndex()));

            if (mob.getPokemonAIState(IMoveConstants.HUNTING) && !pokemob.getPokemonAIState(IMoveConstants.TAMED))
            {
                if (move == null || move.getPWR(mob, entityTarget) <= 0)
                {
                    for (int i = 0; i < 4; i++)
                    {
                        String choice = mob.getMove(i);
                        if (choice != null && !choice.isEmpty())
                        {
                            move = MovesUtils.getMoveFromName(choice);
                            if (move != null && move.getPWR(mob, entityTarget) > 0)
                            {
                                mob.setMoveIndex(i);
                            }
                        }
                    }
                }
                move = MovesUtils.getMoveFromName(mob.getMove(mob.getMoveIndex()));
            }
            else if (mob.getPokemonAIState(IMoveConstants.GUARDING))
            {
                int index = mob.getMoveIndex();
                int max = 0;
                String[] moves = mob.getMoves();
                for (int i = 0; i < 4; i++)
                {
                    String s = moves[i];
                    if (s != null)
                    {
                        Move_Base m = MovesUtils.getMoveFromName(s);
                        int temp = Tools.getPower(s, mob, entityTarget);
                        if (dist > 5 && (m.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
                        {
                            temp *= 1.5;
                        }
                        if (temp > max)
                        {
                            index = i;
                            max = temp;
                        }
                    }
                }
                if (index != mob.getMoveIndex()) mob.setMoveIndex(index);
            }

            if (move == null) move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);

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
        }
        boolean canUseMove = MovesUtils.canUseMove(pokemob);
        boolean shouldPath = delayTime <= 0;
        boolean inRange = false;

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
                    inRange = box.intersectsWith(box2);
                }
            }
            else
            {
                float attackerLength = pokemob.getPokedexEntry().length * pokemob.getSize() + 0.5f;
                float attackerHeight = pokemob.getPokedexEntry().height * pokemob.getSize() + 0.5f;
                float attackerWidth = pokemob.getPokedexEntry().height * pokemob.getSize() + 0.5f;

                float attackedLength = entityTarget.width;
                float attackedHeight = entityTarget.height;
                float attackedWidth = entityTarget.width;

                float dx = (float) (attacker.posX - entityTarget.posX);
                float dz = (float) (attacker.posZ - entityTarget.posZ);
                float dy = (float) (attacker.posY - entityTarget.posY);

                AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, attackerWidth, attackerHeight, attackerLength);
                AxisAlignedBB box2 = new AxisAlignedBB(dx, dy, dz, dx + attackedWidth, dy + attackedHeight,
                        dz + attackedLength);
                inRange = box.intersectsWith(box2);
                if (shouldPath && !(distanced || self))
                    setPokemobAIState((IPokemob) attacker, IMoveConstants.LEAPING, true);

            }
        }
        if (self)
        {
            inRange = true;
            targetLoc.set(attacker);
        }

        if (!canSee)
        {
            chaseTime++;
        }
        else
        {
            chaseTime = 0;
            if (!pokemob.getPokemonAIState(IMoveConstants.EXECUTINGMOVE))
            {
                targetLoc.set(entityTarget).addTo(0, entityTarget.height / 2, 0);
            }
        }
        if (delayTime < -20)
        {
            shouldPath = true;
            applyDelay(pokemob, move.name, distanced);
            addTargetInfo(attacker, entityTarget);
            ((IPokemob) attacker).setPokemonAIState(IMoveConstants.ANGRY, true);
            targetLoc.set(entityTarget);
        }
        boolean delay = false;
        if ((inRange || self))
        {
            if (canSee || self)
            {
                if (delayTime <= 0)
                {
                    applyDelay(pokemob, move.name, distanced);
                    delay = canUseMove;
                }
                shouldPath = false;
                setPokemobAIState((IPokemob) attacker, IMoveConstants.EXECUTINGMOVE, true);
            }
        }
        else
        {
            setPokemobAIState((IPokemob) attacker, IMoveConstants.EXECUTINGMOVE, false);
        }
        if (!delay && delayTime % 5 == 0)
        {
            addTargetInfo(attacker, entityTarget);
        }
        if (!targetLoc.isEmpty() && delay && inRange)
        {
            if ((entityTarget instanceof IPokemob
                    && !((IPokemob) entityTarget).getPokemonAIState(IMoveConstants.DODGING))
                    || !(entityTarget instanceof IPokemob) || attack.move.notIntercepable)
            {
                targetLoc.set(entityTarget).addTo(0, entityTarget.height / 2, 0);
            }
            else
            {

            }
            if (entityTarget instanceof IPokemob)
            {
                setPokemobAIState((IPokemob) entityTarget, IMoveConstants.DODGING, false);
            }
            if (this.attacker.getHeldItemMainhand() != null)
            {
                this.attacker.swingArm(EnumHand.MAIN_HAND);
            }
            float f = (float) targetLoc.distToEntity(attacker);
            Vector3 loc = targetLoc.copy();
            addMoveInfo(attacker.getEntityId(), entityTarget.getEntityId(), attacker.dimension, loc, f);
            shouldPath = false;
            setPokemobAIState((IPokemob) attacker, IMoveConstants.EXECUTINGMOVE, false);
            targetLoc.clear();
            applyDelay(pokemob, move.name, distanced);
        }
        if (!targetLoc.isEmpty() && shouldPath)
        {
            path = this.attacker.getNavigator().getPathToXYZ(targetLoc.x, targetLoc.y, targetLoc.z);
            if (path != null) addEntityPath(attacker.getEntityId(), attacker.dimension, path, movementSpeed);
        }
        delayTime--;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(attacker.dimension);
        if (world == null) return false;
        EntityLivingBase var1 = attacker.getAttackTarget();
        if (var1 == null)
        {
            if (attacker.getNavigator().noPath()
                    && ((IPokemob) attacker).getPokemonAIState(IMoveConstants.EXECUTINGMOVE))
            {
                setPokemobAIState((IPokemob) attacker, IMoveConstants.EXECUTINGMOVE, false);
            }
            return false;
        }
        else if (var1.isDead)
        {
            return false;
        }
        else
        {
            attack = MovesUtils.getMoveFromName(((IPokemob) attacker).getMove(((IPokemob) attacker).getMoveIndex()));
            entityTarget = var1;
            if (attack == null) attack = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
            return true;
        }
    }
}
