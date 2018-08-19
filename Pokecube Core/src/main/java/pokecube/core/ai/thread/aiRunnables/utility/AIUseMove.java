package pokecube.core.ai.thread.aiRunnables.utility;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AIUseMove extends AIBase
{
    final IPokemob     pokemob;
    final EntityLiving entity;
    private boolean    running    = false;
    private boolean    checkRange = false;
    double             speed;

    public AIUseMove(IPokemob pokemob)
    {
        this.pokemob = pokemob;
        this.entity = pokemob.getEntity();
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);

        // If the move was ranged, check that it is visible, if so, execute
        // move, otherwise path to location.
        if (checkRange)
        {
            Vector3 destination = pokemob.getTargetPos();
            if (destination != null)
            {
                RayTraceResult result = world.rayTraceBlocks(entity.getPositionVector(),
                        new Vec3d(destination.x, destination.y, destination.z), true);

                // Adjust destination accordingly based on side hit, since it is
                // normally center of block.
                if (result != null)
                {
                    Vec3i dir = result.sideHit.getDirectionVec();
                    // Make a new location that is shifted to closer to edge of
                    // the block for the visiblity checks.
                    Vector3 loc = destination.copy();
                    if (loc.x % 1 == 0.5)
                    {
                        loc.x += dir.getX() * 0.49;
                    }
                    if (loc.y % 1 == 0.5)
                    {
                        loc.y += dir.getY() * 0.49;
                    }
                    if (loc.z % 1 == 0.5)
                    {
                        loc.z += dir.getZ() * 0.49;
                    }
                    // Raytrace against shifted location.
                    result = world.rayTraceBlocks(entity.getPositionVector(), new Vec3d(loc.x, loc.y, loc.z), true);
                }

                // Apply move directly from here.
                if (result == null || result.getBlockPos().equals(destination.getPos()))
                {
                    addMoveInfo(entity.getEntityId(), -1, entity.dimension, destination, 0);
                    addEntityPath(entity, null, speed);
                    pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                    running = false;
                }
                else
                {
                    // Set destination and wait for move to be checked again.
                    running = true;
                    pokemob.setCombatState(CombatStates.EXECUTINGMOVE, true);
                    Path path = entity.getNavigator().getPathToXYZ(destination.x, destination.y, destination.z);
                    addEntityPath(entity, path, speed);
                }
            }
            checkRange = false;
        }
    }

    @Override
    public void run()
    {
        Vector3 destination = pokemob.getTargetPos();
        Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));
        if (!running)
        {
            speed = pokemob.getMovementSpeed();
            pokemob.setCombatState(CombatStates.NEWEXECUTEMOVE, false);
            if (move == null) { return; }
            // No destination given, just apply the move directly.
            if (destination == null)
            {
                addMoveInfo(entity.getEntityId(), -1, entity.dimension, null, 0);
                pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                return;
            }
            else
            {
                boolean self = (move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0;
                // Apply self moves directly.
                if (self)
                {
                    addMoveInfo(entity.getEntityId(), entity.getEntityId(), entity.dimension, null, 0);
                    return;
                }
                boolean ranged = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) != 0;
                if (ranged && !checkRange)
                {
                    double dist = destination.distToEntity(entity);
                    // If in range, divert to main thread to see if visible.
                    if (dist < PokecubeCore.core.getConfig().rangedAttackDistance)
                    {
                        checkRange = true;
                        return;
                    }
                }
                // Try to path to where the move is needed.
                pokemob.setCombatState(CombatStates.EXECUTINGMOVE, true);
                Path path = entity.getNavigator().getPathToXYZ(destination.x, destination.y, destination.z);
                addEntityPath(entity, path, speed);
            }
            running = true;
        }
        // Look at your destination
        entity.getLookHelper().setLookPosition(destination.x, destination.y, destination.z, 10,
                entity.getVerticalFaceSpeed());
        Vector3 loc = Vector3.getNewVector().set(entity, false);
        double dist = loc.distToSq(destination);
        double var1 = 16;
        if (move == null)
        {
            running = false;
            pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            return;
        }
        if (!checkRange && (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
        {
            var1 = PokecubeCore.core.getConfig().rangedAttackDistance
                    * PokecubeCore.core.getConfig().rangedAttackDistance;
            // Divert ranged moves to main thread for visiblity checks.
            checkRange = true;
        }
        if (!checkRange && dist < var1)
        {
            // If in range, apply the move and reset tasks
            addMoveInfo(entity.getEntityId(), -1, entity.dimension, destination, 0);
            addEntityPath(entity, null, speed);
            pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            // Leap at the target location
            pokemob.setCombatState(CombatStates.LEAPING, true);
            running = false;
        }

    }

    @Override
    public boolean shouldRun()
    {
        return running || (pokemob.getCombatState(CombatStates.NEWEXECUTEMOVE)
                && !pokemob.getCombatState(CombatStates.ANGRY) && pokemob.getAttackCooldown() <= 0);
    }

}
