package pokecube.core.ai.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class PokemobMoveHelper extends EntityMoveHelper
{
    /** The EntityLiving that is being moved */
    private EntityLiving entity;
    private double       posX;
    private double       posY;
    private double       posZ;
    private Vector3      pos     = Vector3.getNewVector();
    private Vector3      lastPos = Vector3.getNewVector();
    /** The speed at which the entity should move */
    private double       speed;
    private boolean      update;

    public PokemobMoveHelper(EntityLiving entity)
    {
        super(entity);
        this.entity = entity;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
    }

    @Override
    public double getSpeed()
    {
        return this.speed;
    }

    @Override
    public boolean isUpdating()
    {
        return this.update;
    }

    /** Limits the given angle to a upper and lower limit. */
    @Override
    public float limitAngle(float old, float newAngle, float target)
    {
        float f3 = MathHelper.wrapDegrees(newAngle - old);
        target = Math.max(target, Math.abs(f3));

        if (f3 > target)
        {
            f3 = target;
        }

        if (f3 < -target)
        {
            f3 = -target;
        }

        return old + f3;
    }

    @Override
    public void onUpdateMoveHelper()
    {
        this.entity.setMoveForward(0.0F);
        if (this.update)
        {
            pos.set(entity);
            PokedexEntry entry = ((IPokemob) entity).getPokedexEntry();
            if (((IPokemob) entity).getTransformedTo() instanceof IPokemob)
            {
                entry = ((IPokemob) ((IPokemob) entity).getTransformedTo()).getPokedexEntry();
            }
            boolean water = entry.swims() && entity.isInWater();
            boolean air = entry.flys() || entry.floats();
            this.update = false;
            double i = (int) (this.entity.posY);
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posZ - this.entity.posZ;
            double d2 = this.posY - i;

            double d4 = d0 * d0 + d1 * d1;
            double d3 = d4 + d2 * d2;
            if (d3 >= 2.5E-7D)
            {
                if (!(water || air) || d4 > 1.0E-1D)
                {
                    float f = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
                    entity.getLookHelper().setLookPosition(posX, posY, posZ, 10.0F, 10.0F);
                    this.entity.rotationYaw = f;
                }
                else if (entity.getAITarget() != null)
                {
                    d0 = entity.getAITarget().posX - this.entity.posX;
                    d1 = entity.getAITarget().posZ - this.entity.posZ;
                    float f = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
                    this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 180.0F);
                }
                if (air || water)
                {
                    entity.rotationPitch = -(float) (Math.atan((float) (d2 / Math.sqrt(d4))) * 180 / Math.PI);
                    ((IPokemob) entity).setDirectionPitch(entity.rotationPitch);
                }
                float newSpeed = (float) (this.speed
                        * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                this.entity.setAIMoveSpeed(newSpeed);

                BlockPos pos = new BlockPos(posX, posY, posZ);
                IBlockState stateDown = entity.worldObj.getBlockState(pos.down());
                IBlockState state = entity.worldObj.getBlockState(pos);
                boolean jump = stateDown.getMaterial().isSolid() || state.getMaterial().isSolid();
                if (d2 >= this.entity.stepHeight && jump && !air)
                {
                    this.entity.getJumpHelper().setJumping();
                }
            }
            lastPos.set(entity);
        }
    }

    /** Sets the speed and location to move to */
    @Override
    public void setMoveTo(double p_75642_1_, double p_75642_3_, double p_75642_5_, double p_75642_7_)
    {
        this.posX = p_75642_1_;
        this.posY = p_75642_3_;
        this.posZ = p_75642_5_;
        this.speed = p_75642_7_;
        this.update = true;
    }
}