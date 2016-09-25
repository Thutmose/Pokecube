package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

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
        PokedexEntry entry = ((IPokemob) entity).getPokedexEntry();
        if (((IPokemob) entity).getTransformedTo() instanceof IPokemob)
        {
            entry = ((IPokemob) ((IPokemob) entity).getTransformedTo()).getPokedexEntry();
        }
        boolean water = entry.swims() && entity.isInWater();
        boolean air = entry.flys() || entry.floats();
        if (this.action == EntityMoveHelper.Action.MOVE_TO)
        {
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

            float f9 = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
            this.entity.setAIMoveSpeed((float) (this.speed
                    * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            if (air || water)
            {
                entity.rotationPitch = -(float) (Math.atan((float) (d2 / Math.sqrt(d4))) * 180 / Math.PI);
                ((IPokemob) entity).setDirectionPitch(entity.rotationPitch);
            }
            if (d2 > this.entity.stepHeight && d0 * d0 + d1 * d1 < 1.0D)
            {
                this.entity.getJumpHelper().setJumping();
            }
        }
        else
        {
            this.entity.setMoveForward(0.0F);
        }

        // this.entity.setMoveForward(0.0F);
        // if (this.update)
        // {
        // pos.set(entity);
        // PokedexEntry entry = ((IPokemob) entity).getPokedexEntry();
        // if (((IPokemob) entity).getTransformedTo() instanceof IPokemob)
        // {
        // entry = ((IPokemob) ((IPokemob)
        // entity).getTransformedTo()).getPokedexEntry();
        // }
        // boolean water = entry.swims() && entity.isInWater();
        // boolean air = entry.flys() || entry.floats();
        // this.update = false;
        // double i = (int) (this.entity.posY);
        // double d0 = this.posX - this.entity.posX;
        // double d1 = this.posZ - this.entity.posZ;
        // double d2 = this.posY - i;
        //
        // double d4 = d0 * d0 + d1 * d1;
        // double d3 = d4 + d2 * d2;
        //
        // double dd4 = 0.1;
        //
        // if (!((IPokemob) entity).getPokemonAIState(IPokemob.ANGRY))
        // {
        // double dim = ((IPokemob) entity).getSize() * ((IPokemob)
        // entity).getPokedexEntry().length / 2;
        // dd4 = Math.max(dd4, dim);
        // }
        //
        // if (d3 >= 2.5E-7D)
        // {
        // if (!(water || air) || d4 > dd4)
        // {
        // float f = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        // entity.getLookHelper().setLookPosition(posX, posY, posZ, 10.0F,
        // 10.0F);
        // this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f,
        // 180.0F);
        // }
        // else if (entity.getAITarget() != null)
        // {
        // d0 = entity.getAITarget().posX - this.entity.posX;
        // d1 = entity.getAITarget().posZ - this.entity.posZ;
        // float f = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        // this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f,
        // 180.0F);
        // }
        // if (air || water)
        // {
        // entity.rotationPitch = -(float) (Math.atan((float) (d2 /
        // Math.sqrt(d4))) * 180 / Math.PI);
        // ((IPokemob) entity).setDirectionPitch(entity.rotationPitch);
        // }
        // float newSpeed = (float) (this.speed
        // *
        // this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        // this.entity.setAIMoveSpeed(newSpeed);
        //
        // BlockPos pos = new BlockPos(posX, posY, posZ);
        // IBlockState stateDown = entity.worldObj.getBlockState(pos.down());
        // IBlockState state = entity.worldObj.getBlockState(pos);
        // boolean jump = stateDown.getMaterial().isSolid() ||
        // state.getMaterial().isSolid();
        // if (d2 >= this.entity.stepHeight && jump && !air)
        // {
        // this.entity.getJumpHelper().setJumping();
        // }
        // }
        // lastPos.set(entity);
        // }
    }

    /** Sets the speed and location to move to */
    /** Sets the speed and location to move to */
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn)
    {
        super.setMoveTo(x, y, z, speedIn);
    }
}