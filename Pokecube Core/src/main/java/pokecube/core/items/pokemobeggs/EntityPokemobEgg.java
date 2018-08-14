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
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** @author Manchou */
public class EntityPokemobEgg extends EntityLiving
{
    int               delayBeforeCanPickup = 0;
    int               age                  = 0;
    int               lastIncubate         = 0;
    public int        hatch                = 0;
    public IPokemob   mother               = null;
    Vector3           here                 = Vector3.getNewVector();
    private ItemStack eggCache             = null;

    /** Do not call this, this is here only for vanilla reasons
     * 
     * @param world */
    public EntityPokemobEgg(World world)
    {
        super(world);
        this.setSize(0.35f, 0.35f);
        hatch = 1000 + getEntityWorld().rand.nextInt(PokecubeMod.core.getConfig().eggHatchTime);
        this.isImmuneToFire = true;
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
    public EntityPokemobEgg(World world, double d, double d1, double d2, Entity placer, IPokemob father)
    {
        this(world);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(placer);
        ItemStack itemstack = ItemPokemobEgg.getEggStack(pokemob);
        ItemPokemobEgg.initStack(placer, father, itemstack);
        this.setHeldItem(EnumHand.MAIN_HAND, itemstack);
        this.setPosition(d, d1, d2);
        delayBeforeCanPickup = 20;
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        Entity e = source.getImmediateSource();
        if (!getEntityWorld().isRemote && e instanceof EntityPlayer)
        {
            if (this.delayBeforeCanPickup > 0) { return false; }

            ItemStack itemstack = this.getHeldItemMainhand();
            int i = itemstack.getCount();
            EntityPlayer player = (EntityPlayer) e;
            if (mother != null && mother.getOwner() != player)
            {
                mother.getEntity().setAttackTarget(player);
            }
            if ((i <= 0 || player.inventory.addItemStackToInventory(itemstack)))
            {
                player.onItemPickup(this, i);
                if (itemstack.isEmpty())
                {
                    this.setDead();
                }
                return true;
            }
        }
        if (this.isEntityInvulnerable(source)) { return false; }
        this.markVelocityChanged();
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

    @Override
    public void setHeldItem(EnumHand hand, ItemStack stack)
    {
        super.setHeldItem(hand, stack);
        eggCache = stack;
    }

    public ItemStack getHeldItemMainhand()
    {
        if (getEntityWorld().isRemote) return super.getHeldItemMainhand();
        if (eggCache == null) return eggCache = super.getHeldItemMainhand();
        return eggCache;
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
            IPokemob pokemob = ItemPokemobEgg.getFakePokemob(getEntityWorld(), here, getHeldItemMainhand());
            if (pokemob == null) return null;
            pokemob.getEntity().setWorld(getEntityWorld());
            return pokemob;
        }
        PokedexEntry entry = ItemPokemobEgg.getEntry(getHeldItemMainhand());
        if (entry == null) return null;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, getEntityWorld()));
        if (pokemob == null) return null;
        here.moveEntity(pokemob.getEntity());
        ItemPokemobEgg.initPokemobGenetics(pokemob, getHeldItemMainhand().getTagCompound());
        pokemob.getEntity().setWorld(getEntityWorld());
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
    protected void handleJumpWater()
    {
        this.motionY += 0.021D;
    }

    @Override
    protected void handleJumpLava()
    {
        this.motionY += 0.021D;
    }

    @Override
    public void onUpdate()
    {
        motionX *= 0.9;
        motionZ *= 0.9;

        if (this.isInWater() || this.isInLava()) this.getJumpHelper().setJumping();

        here.set(this);
        super.onUpdate();
        if (getEntityWorld().isRemote) return;
        if (!CompatWrapper.isValid(getHeldItemMainhand()))
        {
            this.setDead();
            return;
        }
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
                ItemPokemobEgg.spawn(getEntityWorld(), getHeldItemMainhand(), Math.floor(posX) + 0.5,
                        Math.floor(posY) + 0.5, Math.floor(posZ) + 0.5);
                setDead();
            }
        }
        else if (age > hatch * 0.8 && rand.nextInt(20 + hatch - age) == 0)
        {
            IPokemob mob = getPokemob(false);
            if (mob == null) this.setDead();
            else mob.getEntity().playLivingSound();
        }
        TileEntity te = here.getTileEntity(getEntityWorld(), EnumFacing.DOWN);
        if (te == null) te = here.getTileEntity(getEntityWorld());
        if (te instanceof TileEntityHopper)
        {
            TileEntityHopper hopper = (TileEntityHopper) te;
            EntityItem item = new EntityItem(getEntityWorld(), posX, posY, posZ, getHeldItemMainhand());
            boolean added = TileEntityHopper.putDropInInventoryAllSlots(null, hopper, item);
            if (added)// needs null as first argument for 1.11+
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