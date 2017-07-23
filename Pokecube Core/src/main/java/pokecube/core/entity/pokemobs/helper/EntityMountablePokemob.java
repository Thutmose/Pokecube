/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import thut.api.entity.IMultiplePassengerEntity;

/** Handles the HM behaviour.
 * 
 * @author Manchou */
public abstract class EntityMountablePokemob extends EntityEvolvablePokemob implements IMultiplePassengerEntity
{
    private int       mountCounter = 0;
    protected double  yOffset;

    public int        counterMount = 0;

    protected boolean pokemobJumping;

    protected float   jumpPower;

    public EntityMountablePokemob(World world)
    {
        super(world);
        this.stepHeight = 1.2f;
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

    public boolean checkHunger()
    {
        return false;
    }

    /** Returns the Y offset from the entity's position for any entity riding
     * this one. */
    @Override
    public double getMountedYOffset()
    {
        return this.height * this.getPokedexEntry().passengerOffsets[0][1];
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
    }

    @Override
    public boolean getOnGround()
    {
        return pokemobCap.getOnGround();
    }

    @Override
    public double getYOffset()
    {
        double ret = yOffset;
        return ret;// - 1.6F;
    }

    /** Called when a player interacts with its pokemob with an item such as HM
     * or saddle.
     * 
     * @param entityplayer
     *            the player which makes the action
     * @param itemstack
     *            the id of the item
     * @return if the use worked */
    protected boolean handleHmAndSaddle(EntityPlayer entityplayer, ItemStack itemstack)
    {
        if (isRidable(entityplayer))
        {
            if (!world.isRemote) entityplayer.startRiding(this);
            return true;
        }
        return false;
    }

    public boolean isPokemobJumping()
    {
        return this.pokemobJumping;
    }

    public boolean isRidable(Entity rider)
    {
        PokedexEntry entry = this.getPokedexEntry();
        if (entry == null)
        {
            System.err.println("Null Entry for " + this);
            return false;
        }
        if (!entry.ridable) return false;
        return (entry.height * getSize() + entry.width * getSize()) > rider.width
                && Math.max(entry.width, entry.length) * getSize() > rider.width * 1.8;
    }

    /** Returns true if the entity is riding another entity, used by render to
     * rotate the legs to be in 'sit' position for players. */
    @Override
    public boolean isRiding()
    {
        return super.isRiding();
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
            if (this.getAttackTarget() != null && !world.isRemote)
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
        return !this.canUseDive();
    }

    /** main AI tick function, replaces updateEntityActionState */
    protected void updateAITick()
    {

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
        if (index >= getPokedexEntry().passengerOffsets.length) index = 0;
        double[] offset = this.getPokedexEntry().passengerOffsets[index];
        seat.x = (float) offset[0];
        seat.y = (float) offset[1];
        seat.z = (float) offset[2];
        float dx = this.getPokedexEntry().width * this.getSize(), dz = this.getPokedexEntry().length * this.getSize();
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
