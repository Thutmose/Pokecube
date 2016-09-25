/**
 *
 */
package pokecube.core.items.pokemobeggs;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

/** @author Manchou */
public class EntityPokemobEgg extends EntityLiving
{
    int        delayBeforeCanPickup = 0;
    int        age                  = 0;
    int        lastIncubate         = 0;
    public int hatch                = 0;
    Vector3    here                 = Vector3.getNewVector();

    /** Do not call this, this is here only for vanilla reasons
     * 
     * @param world */
    public EntityPokemobEgg(World world)
    {
        super(world);
        this.setSize(0.35f, 0.35f);
        hatch = 1000 + worldObj.rand.nextInt(PokecubeMod.core.getConfig().eggHatchTime);
    }

    /** @param world
     * @param d
     * @param d1
     * @param d2
     * @param itemstack */
    public EntityPokemobEgg(World world, double d, double d1, double d2, ItemStack itemstack, Entity placer)
    {
        this(world);
        this.setHeldItem(EnumHand.MAIN_HAND, itemstack);
        this.setPosition(d, d1, d2);
        delayBeforeCanPickup = 20;
    }

    /** @param world
     * @param d
     * @param d1
     * @param d2
     * @param itemstack */
    public EntityPokemobEgg(World world, double d, double d1, double d2, ItemStack itemstack, Entity placer,
            IPokemob father)
    {
        this(world);
        ItemPokemobEgg.initStack(placer, father, itemstack);
        this.setHeldItem(EnumHand.MAIN_HAND, itemstack);
        this.setPosition(d, d1, d2);
        delayBeforeCanPickup = 20;
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        Entity e = source.getEntity();
        if (!worldObj.isRemote && e != null && e instanceof EntityPlayer)
        {
            if (this.delayBeforeCanPickup > 0) { return false; }

            ItemStack itemstack = this.getHeldItemMainhand();
            int i = itemstack.stackSize;
            EntityPlayer player = (EntityPlayer) e;
            if (this.delayBeforeCanPickup <= 0 && (i <= 0 || player.inventory.addItemStackToInventory(itemstack)))
            {
                player.onItemPickup(this, i);
                if (itemstack.stackSize <= 0)
                {
                    this.setDead();
                }
                return true;
            }
        }
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        this.setBeenAttacked();
        return false;
    }

    /** returns the bounding box for this entity */
    @Override
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return getEntityBoundingBox();
    }

    public Entity getEggOwner()
    {
        IPokemob pokemob = getPokemob(true);
        if (pokemob != null) return pokemob.getPokemonOwner();
        return null;
    }

    public UUID getMotherId()
    {
        if (getHeldItemMainhand() != null && getHeldItemMainhand().hasTagCompound())
        {
            if (getHeldItemMainhand().getTagCompound().hasKey("motherId")) { return UUID
                    .fromString(getHeldItemMainhand().getTagCompound().getString("motherId")); }
        }
        return null;
    }

    /** Called when a user uses the creative pick block button on this entity.
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    @Override
    public ItemStack getPickedResult(RayTraceResult target)
    {
        return getHeldItemMainhand().copy();
    }

    /** Returns a generic pokemob instance with the data of the one in the egg,
     * this is not to be used for spawning into the world.
     * 
     * @return */
    public IPokemob getPokemob(boolean real)
    {
        if (!real)
        {
            IPokemob pokemob = ItemPokemobEgg.getFakePokemob(worldObj, here, getHeldItemMainhand());
            if (pokemob == null) return null;
            ((Entity) pokemob).worldObj = worldObj;
            return pokemob;
        }
        if (getHeldItemMainhand() == null || !getHeldItemMainhand().hasTagCompound()
                || !getHeldItemMainhand().getTagCompound().hasKey("pokemobNumber"))
            return null;
        int number = getHeldItemMainhand().getTagCompound().getInteger("pokemobNumber");
        PokedexEntry entry = Database.getEntry(number);
        IPokemob pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexEntry(entry, worldObj);
        if (pokemob == null) return null;
        here.moveEntity((Entity) pokemob);
        ItemPokemobEgg.initPokemobGenetics(pokemob, getHeldItemMainhand().getTagCompound());
        ((Entity) pokemob).setWorld(worldObj);
        return pokemob;
    }

    public void incubateEgg()
    {
        if (ticksExisted != lastIncubate)
        {
            lastIncubate = ticksExisted;
            age++;
        }
    }

    @Override
    public void onUpdate()
    {
        motionY -= 0.06;
        motionX *= 0.6;
        motionZ *= 0.6;
        moveEntity(motionX, motionY, motionZ);
        if (getHeldItemMainhand() == null)
        {
            this.setDead();
            return;
        }
        here.set(this);
        if (worldObj.isRemote) return;
        this.delayBeforeCanPickup--;
        boolean spawned = getHeldItemMainhand().hasTagCompound()
                && getHeldItemMainhand().getTagCompound().hasKey("nestLocation");

        if (age++ >= hatch || spawned)
        {
            EggEvent.PreHatch event = new EggEvent.PreHatch(this);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
            {
                EggEvent.Hatch evt = new EggEvent.Hatch(this);
                MinecraftForge.EVENT_BUS.post(evt);
                ItemPokemobEgg.spawn(worldObj, getHeldItemMainhand(), Math.floor(posX) + 0.5, Math.floor(posY) + 0.5,
                        Math.floor(posZ) + 0.5);
                setDead();
            }
        }
        else if (age > hatch * 0.8 && rand.nextInt(20 + hatch - age) == 0)
        {
            IPokemob mob = getPokemob(false);
            if (mob == null) this.setDead();
            else((EntityLiving) mob).playLivingSound();
        }
        TileEntity te = here.getTileEntity(worldObj, EnumFacing.DOWN);
        if (te == null) te = here.getTileEntity(worldObj);
        if (te instanceof TileEntityHopper)
        {
            TileEntityHopper hopper = (TileEntityHopper) te;
            EntityItem item = new EntityItem(worldObj, posX, posY, posZ, getHeldItemMainhand());
            boolean added = TileEntityHopper.putDropInInventoryAllSlots(hopper, item);
            if (added)
            {
                this.setDead();
            }
        }
    }

    @Override
    /** (abstract) Protected helper method to read subclass entity data from
     * NBT. */
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        age = nbt.getInteger("age");
        hatch = nbt.getInteger("hatchtime");
    }

    @Override
    /** (abstract) Protected helper method to write subclass entity data to
     * NBT. */
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("age", age);
        nbt.setInteger("hatchtime", hatch);
    }
}
