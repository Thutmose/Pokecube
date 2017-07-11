package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

/** Overriden to properly support mobs that move in 3D, such as flying or
 * swimming ones, as well as the make it so if a mob has transformed, it uses
 * the movement type of who it has transformed to. */
public class PokemobMoveHelper extends EntityMoveHelper
{
    public PokemobMoveHelper(EntityLiving entity)
    {
        super(entity);
    }

    @Override
    public double getSpeed()
    {
        return super.getSpeed();
    }

    @Override
    public boolean isUpdating()
    {
        return super.isUpdating();
    }

    @Override
    public void onUpdateMoveHelper()
    {
        if (this.action == EntityMoveHelper.Action.STRAFE)
        {
            float f = (float) this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .getAttributeValue();
            float f1 = (float) this.speed * f;
            float f2 = this.moveForward;
            float f3 = this.moveStrafe;
            float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);

            if (f4 < 1.0F)
            {
                f4 = 1.0F;
            }

            f4 = f1 / f4;
            f2 = f2 * f4;
            f3 = f3 * f4;
            float f5 = MathHelper.sin(this.entity.rotationYaw * 0.017453292F);
            float f6 = MathHelper.cos(this.entity.rotationYaw * 0.017453292F);
            float f7 = f2 * f6 - f3 * f5;
            float f8 = f3 * f6 + f2 * f5;
            PathNavigate pathnavigate = this.entity.getNavigator();

            if (pathnavigate != null)
            {
                NodeProcessor nodeprocessor = pathnavigate.getNodeProcessor();

                if (nodeprocessor != null && nodeprocessor.getPathNodeType(this.entity.world,
                        MathHelper.floor(this.entity.posX + (double) f7), MathHelper.floor(this.entity.posY),
                        MathHelper.floor(this.entity.posZ + (double) f8)) != PathNodeType.WALKABLE)
                {
                    this.moveForward = 1.0F;
                    this.moveStrafe = 0.0F;
                    f1 = f;
                }
            }

            this.entity.setAIMoveSpeed(f1);
            this.entity.setMoveForward(this.moveForward);
            this.entity.setMoveStrafing(this.moveStrafe);
            this.action = EntityMoveHelper.Action.WAIT;
        }
        else if (this.action == EntityMoveHelper.Action.MOVE_TO)
        {

            IPokemob pokemob = (IPokemob) entity;
            PokedexEntry entry = pokemob.getPokedexEntry();
            if (pokemob.getTransformedTo() instanceof IPokemob)
            {
                entry = ((IPokemob) pokemob.getTransformedTo()).getPokedexEntry();
            }
            pokemob.setDirectionPitch(0);
            entity.setMoveVertical(0);
            boolean water = entry.swims() && entity.isInWater();
            boolean air = entry.flys() || entry.floats();
            boolean shouldGoDown = false;
            boolean shouldGoUp = false;
            PathPoint p = null;
            if (!entity.getNavigator().noPath() && !entity.getNavigator().getPath().isFinished())
            {
                p = entity.getNavigator().getPath()
                        .getPathPointFromIndex(entity.getNavigator().getPath().getCurrentPathIndex());
                shouldGoDown = p.y < entity.posY - entity.stepHeight;
                shouldGoUp = p.y > entity.posY + entity.stepHeight;
                if (air || water)
                {
                    shouldGoUp = p.y > entity.posY - entity.stepHeight;
                    shouldGoDown = !shouldGoUp;
                }
            }
            if ((pokemob.getPokemonAIState(IPokemob.SLEEPING)
                    || (pokemob.getStatus() & (IPokemob.STATUS_SLP + IPokemob.STATUS_FRZ)) > 0) && air)
                shouldGoDown = true;

            this.action = EntityMoveHelper.Action.WAIT;
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posZ - this.entity.posZ;
            double d2 = this.posY - this.entity.posY;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            double d4 = d0 * d0 + d1 * d1;

            if (d3 < 2.500000277905201E-7D)
            {
                this.entity.setMoveForward(0.0F);
                return;
            }
            boolean upLadder = d2 > 0 && entity.isOnLadder();
            if (upLadder || (d2 > entity.stepHeight && d4 <= 2 * speed))
            {
                this.entity.getJumpHelper().setJumping();
            }

            float f9 = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
            this.entity.setAIMoveSpeed((float) (this.speed
                    * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

            if (shouldGoDown || shouldGoUp)
            {
                entity.rotationPitch = -(float) (Math.atan((float) (d2 / Math.sqrt(d4))) * 180 / Math.PI);
                ((IPokemob) entity).setDirectionPitch(entity.rotationPitch);
                float up = -MathHelper.sin(entity.rotationPitch * (float) Math.PI / 180.0F);
                entity.setMoveVertical(up);
            }
            if (d2 > (double) this.entity.stepHeight && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.entity.width))
            {
                this.entity.getJumpHelper().setJumping();
                this.action = EntityMoveHelper.Action.JUMPING;
            }
        }
        else if (this.action == EntityMoveHelper.Action.JUMPING)
        {
            this.entity.setAIMoveSpeed((float) (this.speed
                    * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

            if (this.entity.onGround)
            {
                this.action = EntityMoveHelper.Action.WAIT;
            }
        }
        else
        {
            this.entity.setMoveForward(0.0F);
            entity.setMoveVertical(0);
        }
    }

    /** Sets the speed and location to move to */
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn)
    {
        super.setMoveTo(x, y, z, speedIn);
    }
}