/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.utils.Permissions;
import thut.api.entity.IMultiplePassengerEntity;

/** Handles the HM behaviour.
 * 
 * @author Manchou */
public abstract class EntityMountablePokemob extends EntityEvolvablePokemob implements IMultiplePassengerEntity
{
    @SuppressWarnings("unchecked")
    static final DataParameter<Seat>[] SEAT = new DataParameter[10];

    static
    {
        for (int i = 0; i < SEAT.length; i++)
        {
            SEAT[i] = EntityDataManager.<Seat> createKey(EntityMountablePokemob.class, SEATSERIALIZER);
        }
    }

    private int          mountCounter = 0;
    protected double     yOffset;
    private boolean      init         = false;
    private PokedexEntry lastCheck    = null;
    private int          seatCount    = 0;

    public int           counterMount = 0;

    public EntityMountablePokemob(World world)
    {
        super(world);
        this.stepHeight = 1;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        for (int i = 0; i < 10; i++)
            dataManager.register(SEAT[i], new Seat(new Vector3f(), null));
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
        boolean canDive = pokemobCap.canUseDive();
        if (rider instanceof EntityPlayerMP)
        {
            EntityPlayer player = (EntityPlayer) rider;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            PokedexEntry entry = pokemobCap.getPokedexEntry();
            Config config = PokecubeCore.core.getConfig();
            if (config.permsDive && canDive
                    && !handler.hasPermission(player.getGameProfile(), Permissions.DIVEPOKEMOB, context))
            {
                canDive = false;
            }
            if (config.permsDiveSpecific && canDive
                    && !handler.hasPermission(player.getGameProfile(), Permissions.DIVESPECIFIC.get(entry), context))
            {
                canDive = false;
            }
        }
        return !canDive;
    }

    @Override
    public Vector3f getSeat(Entity passenger)
    {
        initSeats();
        Vector3f ret = null;
        for (int i = 0; i < seatCount; i++)
        {
            Seat seat;
            if ((seat = getSeat(i)).entityId.equals(passenger.getUniqueID())) { return seat.seat; }
        }
        return ret;
    }

    public boolean canPassengerSteer()
    {
        // We return false here, as we handle our own steering/control.
        // Otherwise vanilla makes this not move properly.
        return false;
    }

    @Override
    @Nullable
    public Entity getControllingPassenger()
    {
        Entity first = null;
        if (this.isBeingRidden() && (first = getPassengers().get(0)) == getOwner()) { return first; }
        return null;
    }

    @Override
    public Entity getPassenger(Vector3f seatl)
    {
        initSeats();
        UUID id = null;
        for (int i = 0; i < seatCount; i++)
        {
            Seat seat;
            if ((seat = getSeat(i)).seat.equals(seatl))
            {
                id = seat.entityId;
            }
        }
        if (id != null)
        {
            for (Entity e : getPassengers())
            {
                if (e.getUniqueID().equals(id)) return e;
            }
        }
        return null;
    }

    @Override
    public List<Vector3f> getSeats()
    {
        initSeats();
        List<Vector3f> ret = Lists.newArrayList();
        for (int i = 0; i < seatCount; i++)
        {
            Seat seat = getSeat(i);
            ret.add(seat.seat);
        }
        return null;
    }

    private void initSeats()
    {
        if (init && lastCheck == pokemobCap.getPokedexEntry()) return;
        lastCheck = pokemobCap.getPokedexEntry();
        init = true;
        seatCount = pokemobCap.getPokedexEntry().passengerOffsets.length;
        for (int index = 0; index < seatCount; index++)
        {
            Vector3f seat = new Vector3f();
            double[] offset = pokemobCap.getPokedexEntry().passengerOffsets[index];
            seat.x = (float) offset[0];
            seat.y = (float) offset[1];
            seat.z = (float) offset[2];
            float dx = pokemobCap.getPokedexEntry().width * pokemobCap.getSize(),
                    dz = pokemobCap.getPokedexEntry().length * pokemobCap.getSize();
            seat.x *= dx;
            seat.y *= this.height;
            seat.z *= dz;
            getSeat(index).seat = seat;
        }
    }

    @Override
    public float getYaw()
    {
        return renderYawOffset;
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

    void setSeatID(int index, UUID id)
    {
        Seat toSet = this.getSeat(index);
        UUID old = toSet.entityId;
        if (!old.equals(id))
        {
            toSet.entityId = id;
            this.dataManager.set(SEAT[index], toSet);
            this.dataManager.setDirty(SEAT[index]);
        }
    }

    Seat getSeat(int index)
    {
        return this.dataManager.get(SEAT[index]);
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
        }
    }

    @Override
    protected void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        initSeats();
        if (!world.isRemote)
        {
            for (int i = 0; i < seatCount; i++)
            {
                if (getSeat(i).entityId == Seat.BLANK)
                {
                    setSeatID(i, passenger.getUniqueID());
                    break;
                }
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        initSeats();
        if (!world.isRemote) for (int i = 0; i < seatCount; i++)
        {
            if (getSeat(i).entityId.equals(passenger.getUniqueID()))
            {
                setSeatID(i, Seat.BLANK);
                break;
            }
        }
    }

    @Override
    public boolean canFitPassenger(Entity passenger)
    {
        if (this.getPassengers().isEmpty()) return passenger == pokemobCap.getOwner();
        return this.getPassengers().size() < seatCount;
    }
}
