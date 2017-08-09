package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

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
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        boolean water = entry.swims() && entity.isInWater();
        boolean air = entry.flys() || entry.floats();

        if (!(air || water))
        {
            pokemob.setDirectionPitch(0);
            super.onUpdateMoveHelper();
            return;
        }

        if (this.action == EntityMoveHelper.Action.MOVE_TO)
        {
            this.action = EntityMoveHelper.Action.WAIT;
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posZ - this.entity.posZ;
            double d2 = this.posY - this.entity.posY;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            double d4 = d0 * d0 + d1 * d1;
            boolean upLadder = d2 > 0 && entity.isOnLadder();
            if (d3 < 2.500000277905201E-7D)
            {
                this.entity.setMoveForward(0.0F);
                return;
            }
            if (upLadder || (d2 > 0.5 && d4 <= 2 * speed && !(air || water)))
            {
                this.entity.getJumpHelper().setJumping();
            }
            double diff = pokemob.getSize() * entry.length / 2;
            diff *= diff;
            if (d4 > diff && (d2 < 0 || d2 <= entity.stepHeight))
            {
                float f9 = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
                this.entity.setAIMoveSpeed((float) (this.speed
                        * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            }
            entity.rotationPitch = -(float) (Math.atan((float) (d2 / Math.sqrt(d4))) * 180 / Math.PI);
            pokemob.setDirectionPitch(entity.rotationPitch);

        }
        else
        {
            this.entity.setMoveForward(0.0F);
        }
    }

    /** Sets the speed and location to move to */
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn)
    {
        super.setMoveTo(x, y, z, speedIn);
    }
}