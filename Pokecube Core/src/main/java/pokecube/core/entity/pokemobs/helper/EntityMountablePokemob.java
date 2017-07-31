/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import thut.api.entity.IMultiplePassengerEntity;

/** Handles the HM behaviour.
 * 
 * @author Manchou */
public abstract class EntityMountablePokemob extends EntityEvolvablePokemob implements IMultiplePassengerEntity
{
    private int       mountCounter = 0;
    protected double  yOffset;

    public int        counterMount = 0;

    public EntityMountablePokemob(World world)
    {
        super(world);
        this.stepHeight = 1;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        if (isRiding())
        {
            dismountRidingEntity();
            counterMount = 0;
        }
        return super.attackEntityFrom(source, i);
    }

    /** Returns the Y offset from the entity's position for any entity riding
     * this one. */
    @Override
    public double getMountedYOffset()
    {
        return this.height * pokemobCap.getPokedexEntry().passengerOffsets[0][1];
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
    }

    @Override
    public double getYOffset()
    {
        double ret = yOffset;
        return ret;// - 1.6F;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (mountCounter > 0) motionX = motionY = motionZ = 0;
        mountCounter--;
        if (getRidingEntity() != null)
        {
            rotationYaw = getRidingEntity().rotationYaw;
            if (this.getAttackTarget() != null && !getEntityWorld().isRemote)
            {
                this.dismountRidingEntity();
                counterMount = 0;
            }
        }
    }

    /** If the rider should be dismounted from the entity when the entity goes
     * under water
     *
     * @param rider
     *            The entity that is riding
     * @return if the entity should be dismounted when under water */
    @Override
    public boolean shouldDismountInWater(Entity rider)
    {
        return !pokemobCap.canUseDive();
    }

    @Override
    public Vector3f getSeat(Entity passenger)
    {
        Vector3f seat = new Vector3f();
        int index = 0;
        if (passenger != null)
        {
            List<Entity> passengers = this.getPassengers();
            for (int i = 0; i < passengers.size(); i++)
            {
                if (passenger == passengers.get(index))
                {
                    index = i;
                    break;
                }
            }
        }
        if (index >= pokemobCap.getPokedexEntry().passengerOffsets.length) index = 0;
        double[] offset = pokemobCap.getPokedexEntry().passengerOffsets[index];
        seat.x = (float) offset[0];
        seat.y = (float) offset[1];
        seat.z = (float) offset[2];
        float dx = pokemobCap.getPokedexEntry().width * pokemobCap.getSize(),
                dz = pokemobCap.getPokedexEntry().length * pokemobCap.getSize();
        seat.x *= dx;
        seat.y *= this.height;
        seat.z *= dz;
        return seat;
    }

    @Override
    public Entity getPassenger(Vector3f seat)
    {
        List<Entity> passengers = this.getPassengers();
        if (!passengers.isEmpty()) return passengers.get(0);
        return null;
    }

    @Override
    public List<Vector3f> getSeats()
    {
        return Lists.newArrayList(getSeat(null));
    }

    @Override
    public float getYaw()
    {
        return rotationYaw;
    }

    @Override
    public float getPitch()
    {
        return this.pokemobCap.getDirectionPitch();
    }

    // We do our own rendering, so don't need this.
    @Override
    public float getPrevYaw()
    {
        return getYaw();
    }

    // We do our own rendering, so don't need this.
    @Override
    public float getPrevPitch()
    {
        return getPitch();
    }
}
