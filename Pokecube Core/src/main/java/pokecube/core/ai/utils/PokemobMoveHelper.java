package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

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
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        boolean water = entry.swims() && entity.isInWater();
        boolean air = entry.flys() || entry.floats();

        if (!(air || water) || this.action != EntityMoveHelper.Action.MOVE_TO)
        {
            pokemob.setDirectionPitch(0);
            super.onUpdateMoveHelper();
            return;
        }

        this.action = EntityMoveHelper.Action.WAIT;
        double d0 = this.posX - this.entity.posX;
        double d1 = this.posZ - this.entity.posZ;
        double d2 = this.posY - this.entity.posY;
        double d3 = d0 * d0 + d2 * d2 + d1 * d1;
        double d4 = d0 * d0 + d1 * d1;

        pokemob.setDirectionPitch(0);
        entity.setMoveVertical(0);
        boolean shouldGoDown = false;
        boolean shouldGoUp = false;
        PathPoint p = null;
        if (!entity.getNavigator().noPath() && Math.abs(d2) > 0.05)
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
        if ((pokemob.getPokemonAIState(IPokemob.SLEEPING)
                || (pokemob.getStatus() & (IPokemob.STATUS_SLP + IPokemob.STATUS_FRZ)) > 0) && air)
            shouldGoDown = true;
        float length = pokemob.getPokedexEntry().length * pokemob.getSize();
        boolean skipped = d3 < (entity.width * entity.width + length * length);
        if (d3 < 2.500000277905201E-7D || skipped)
        {
            this.entity.setMoveForward(0.0F);
            if (skipped && !entity.getNavigator().noPath())
            {
                entity.getNavigator().getPath()
                        .setCurrentPathIndex(entity.getNavigator().getPath().getCurrentPathIndex() + 1);
            }
            return;
        }
        boolean upLadder = d2 > 0 && entity.isOnLadder();
        if (upLadder || (d2 > entity.stepHeight && d4 <= 2 * speed))
        {
            this.entity.getJumpHelper().setJumping();
        }

        float f9 = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
        this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 180.0F);
        float v = (float) (this.speed
                * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        if ((air && !entity.onGround && !entity.isInWater())) v *= PokecubeCore.core.getConfig().flyPathingSpeedFactor;
        else if (water) v *= PokecubeCore.core.getConfig().swimPathingSpeedFactor;
        if (shouldGoDown || shouldGoUp)
        {
            entity.rotationPitch = -(float) (Math.atan((float) (d2 / Math.sqrt(d4))) * 180 / Math.PI);
            pokemob.setDirectionPitch(entity.rotationPitch);
            float up = -MathHelper.sin(entity.rotationPitch * (float) Math.PI / 180.0F);
            entity.setMoveVertical(up);
        }
        this.entity.setAIMoveSpeed(v);
        if (d2 > (double) this.entity.stepHeight && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.entity.width))
        {
            this.entity.getJumpHelper().setJumping();
            this.action = EntityMoveHelper.Action.JUMPING;
        }
    }

    /** Sets the speed and location to move to */
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn)
    {
        super.setMoveTo(x, y, z, speedIn);
    }
}