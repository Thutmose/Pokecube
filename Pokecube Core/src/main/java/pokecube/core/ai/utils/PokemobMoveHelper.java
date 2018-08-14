package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

/** Overriden to properly support mobs that move in 3D, such as flying or
 * swimming ones, as well as the make it so if a mob has transformed, it uses
 * the movement type of who it has transformed to. */
public class PokemobMoveHelper extends EntityMoveHelper
{
    final IPokemob pokemob;

    public PokemobMoveHelper(EntityLiving entity)
    {
        super(entity);
        this.pokemob = CapabilityPokemob.getPokemobFor(entity);
    }

    @Override
    public void onUpdateMoveHelper()
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        IPokemob theMob = pokemob;
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
            theMob = transformed;
        }
        boolean water = entry.swims() && entity.isInWater();
        boolean air = theMob.flys() || theMob.floats();

        if (this.action != EntityMoveHelper.Action.MOVE_TO)
        {
            pokemob.setDirectionPitch(0);
            super.onUpdateMoveHelper();
            return;
        }

        this.action = EntityMoveHelper.Action.WAIT;
        double dx = this.posX - this.entity.posX;
        double dy = this.posY - this.entity.posY;
        double dz = this.posZ - this.entity.posZ;
        double dr = dx * dx + dy * dy + dz * dz;
        double dhoriz = dx * dx + dz * dz;

        pokemob.setDirectionPitch(0);
        entity.setMoveVertical(0);
        boolean shouldGoDown = false;
        boolean shouldGoUp = false;
        PathPoint p = null;
        if (!entity.getNavigator().noPath() && Math.abs(dy) > 0.05)
        {
            p = entity.getNavigator().getPath()
                    .getPathPointFromIndex(entity.getNavigator().getPath().getCurrentPathIndex());
            shouldGoDown = p.y < entity.posY - entity.stepHeight;
            shouldGoUp = p.y > entity.posY + entity.stepHeight;
            if (air || water)
            {
                shouldGoUp = p.y > entity.posY;
                shouldGoDown = !shouldGoUp;
            }
        }
        if ((pokemob.getLogicState(LogicStates.SLEEPING)
                || (pokemob.getStatus() & (IPokemob.STATUS_SLP + IPokemob.STATUS_FRZ)) > 0) && air)
            shouldGoDown = true;
        float length = pokemob.getPokedexEntry().length * pokemob.getSize();
        float dSize = Math.max(0.25f, entity.width * entity.width + length * length);
        if (!entity.getNavigator().noPath())
        {
            BlockPos pos = entity.getPosition();
            PathPoint p2 = entity.getNavigator().getPath().getFinalPathPoint();
            if (p2 == p && pos.getX() == p2.x && (!(air || water) || pos.getY() == p2.y) && pos.getZ() == p2.z)
            {
                dSize = 1;
            }
        }
        if (dr < dSize)
        {
            this.entity.setMoveForward(0.0F);
            if (!entity.getNavigator().noPath())
            {
                entity.getNavigator().getPath()
                        .setCurrentPathIndex(entity.getNavigator().getPath().getCurrentPathIndex() + 1);
            }
            return;
        }

        float newYaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        newYaw = this.limitAngle(this.entity.rotationYaw, newYaw, 30.0F);
        this.entity.rotationYaw = newYaw;
        float v = (float) (this.speed
                * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        if ((air && !entity.onGround && !entity.isInWater())) v *= PokecubeCore.core.getConfig().flyPathingSpeedFactor;
        else if (water) v *= PokecubeCore.core.getConfig().swimPathingSpeedFactor;
        this.entity.setAIMoveSpeed(v);

        if (shouldGoDown || shouldGoUp)
        {
            float pitch = -(float) (Math.atan((dy / Math.sqrt(dhoriz))) * 180 / Math.PI);
            pokemob.setDirectionPitch(pitch);
            float factor = 1;
            if (water && dy < 0.5) factor = 2;
            float up = -MathHelper.sin(pitch * (float) Math.PI / 180.0F) * factor;
            entity.setMoveVertical(up);
        }

        boolean upLadder = dy > 0 && entity.isOnLadder();
        boolean jump = upLadder || (dy > this.entity.stepHeight && dhoriz < Math.max(1.0F, this.entity.width))
                || (dy > entity.stepHeight && dhoriz <= 2 * speed);

        if (jump)
        {
            this.entity.getJumpHelper().setJumping();
            this.action = EntityMoveHelper.Action.JUMPING;
        }
    }
}