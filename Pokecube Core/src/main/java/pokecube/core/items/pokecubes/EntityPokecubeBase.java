package pokecube.core.items.pokecubes;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.commands.CommandTools;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class EntityPokecubeBase extends EntityLiving implements IEntityAdditionalSpawnData, IProjectile
{
    public static SoundEvent                      POKECUBESOUND;
    static final DataParameter<Integer>           ENTITYID       = EntityDataManager
            .<Integer> createKey(EntityPokecube.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> ITEM           = EntityDataManager
            .<ItemStack> createKey(EntityPokecube.class, DataSerializers.OPTIONAL_ITEM_STACK);
    static final DataParameter<Boolean>           RELEASING      = EntityDataManager
            .<Boolean> createKey(EntityPokecube.class, DataSerializers.BOOLEAN);

    public static boolean                         SEEKING        = true;

    /** Seems to be some sort of timer for animating an arrow. */
    public int                                    arrowShake;
    /** 1 if the player can pick up the arrow */
    public int                                    canBePickedUp;
    public boolean                                isLoot         = false;
    protected int                                 inData;
    protected boolean                             inGround;
    public UUID                                   shooter;
    public EntityLivingBase                       shootingEntity;

    public double                                 speed          = 2;
    public EntityLivingBase                       targetEntity;
    public Vector3                                targetLocation = Vector3.getNewVector();

    /** The owner of this arrow. */
    protected int                                 ticksInGround;
    protected Block                               tile;
    protected BlockPos                            tilePos;
    public int                                    tilt           = -1;
    public int                                    time           = 0;
    protected Vector3                             v0             = Vector3.getNewVector();
    protected Vector3                             v1             = Vector3.getNewVector();

    public EntityPokecubeBase(World worldIn)
    {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
        this.isImmuneToFire = true;
        this.enablePersistence();
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        if (source == DamageSource.OUT_OF_WORLD)
        {
            if (PokecubeManager.isFilled(getEntityItem()))
            {
                IPokemob mob = this.sendOut();
                if (mob != null) mob.returnToPokecube();
            }
            this.setDead();
        }
        return false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.getDataManager().register(ITEM, CompatWrapper.nullStack);
        getDataManager().register(RELEASING, false);
        getDataManager().register(ENTITYID, -1);
    }

    /** Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone) */
    public ItemStack getEntityItem()
    {
        ItemStack itemstack = this.getDataManager().get(ITEM);
        return itemstack == null ? new ItemStack(Blocks.STONE) : itemstack;
    }

    public Entity getReleased()
    {
        int id = getDataManager().get(ENTITYID);
        Entity ret = world.getEntityByID(id);
        return ret;
    }

    public boolean isReleasing()
    {
        return getDataManager().get(RELEASING);
    }

    @Override
    public void readSpawnData(ByteBuf buffer)
    {
        motionX = buffer.readDouble();
        motionY = buffer.readDouble();
        motionZ = buffer.readDouble();
    }

    /** Sets the ItemStack for this entity */
    public void setEntityItemStack(ItemStack stack)
    {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    public void setReleased(Entity entity)
    {
        getDataManager().set(ENTITYID, entity.getEntityId());
    }

    public void setReleasing(boolean tag)
    {
        getDataManager().set(RELEASING, tag);
    }

    @Override
    public void setThrowableHeading(double x, double y, double z, float velocity, float inacurracy)
    {
        float f2 = MathHelper.sqrt(x * x + y * y + z * z);
        x /= f2;
        y /= f2;
        z /= f2;
        x += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inacurracy;
        y += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inacurracy;
        z += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inacurracy;
        x *= velocity;
        y *= velocity;
        z *= velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f3 = MathHelper.sqrt(x * x + z * z);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z)
    {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(x * x + z * z);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        buffer.writeDouble(motionX);
        buffer.writeDouble(motionY);
        buffer.writeDouble(motionZ);
    }

    protected void captureFailed()
    {
        IPokemob entity1 = PokecubeManager.itemToPokemob(getEntityItem(), world);

        if (entity1 != null)
        {
            ((Entity) entity1).setLocationAndAngles(posX, posY + 1.0D, posZ, rotationYaw, 0.0F);
            boolean ret = world.spawnEntity((Entity) entity1);

            if (ret == false)
            {
                System.err.println(String.format("The pokemob %1$s spawn from pokecube has failed. ",
                        entity1.getPokemonDisplayName().getFormattedText()));
            }
            ((Entity) entity1).getEntityData().setLong("lastCubeTime",
                    getEntityWorld().getTotalWorldTime() + PokecubeMod.core.getConfig().captureDelayTicks);
            entity1.setPokemonAIState(IMoveConstants.ANGRY, true);
            entity1.setPokemonAIState(IMoveConstants.SITTING, false);
            entity1.setPokemonAIState(IMoveConstants.TAMED, false);
            entity1.setPokemonOwner((UUID) null);

            if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
            {
                ITextComponent mess = new TextComponentTranslation("pokecube.missed", entity1.getPokemonDisplayName());
                ((EntityPlayer) shootingEntity).sendMessage(mess);
                ((EntityCreature) entity1).setAttackTarget(shootingEntity);
            }
        }
    }

    protected boolean captureSucceed()
    {
        PokecubeManager.setTilt(getEntityItem(), -1);
        IPokemob mob = PokecubeManager.itemToPokemob(getEntityItem(), world);
        if (mob == null)
        {
            new NullPointerException("Mob is null").printStackTrace();
            return false;
        }
        HappinessType.applyHappiness(mob, HappinessType.TRADE);
        if (shootingEntity != null && !mob.getPokemonAIState(IMoveConstants.TAMED))
            mob.setPokemonOwner((shootingEntity));
        ItemStack mobStack = PokecubeManager.pokemobToItem(mob);
        this.setEntityItemStack(mobStack);
        if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
        {
            ITextComponent mess = new TextComponentTranslation("pokecube.caught", mob.getPokemonDisplayName());
            ((EntityPlayer) shootingEntity).sendMessage(mess);
            this.setPosition(shootingEntity.posX, shootingEntity.posY, shootingEntity.posZ);
            this.playSound(POKECUBESOUND, 1, 1);
        }
        return true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("tilt", tilt);
        nbttagcompound.setInteger("time", time);
        if (shooter != null) nbttagcompound.setString("shooter", shooter.toString());
        if (this.getEntityItem() != null)
        {
            nbttagcompound.setTag("Item", this.getEntityItem().writeToNBT(new NBTTagCompound()));
        }
        if (tilePos != null)
        {
            nbttagcompound.setInteger("xTile", this.tilePos.getX());
            nbttagcompound.setInteger("yTile", this.tilePos.getY());
            nbttagcompound.setInteger("zTile", this.tilePos.getZ());
        }
        nbttagcompound.setShort("life", (short) this.ticksInGround);
        nbttagcompound.setByte("inTile", (byte) Block.getIdFromBlock(this.tile));
        nbttagcompound.setByte("inData", (byte) this.inData);
        nbttagcompound.setByte("shake", (byte) this.arrowShake);
        nbttagcompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        tilt = nbttagcompound.getInteger("tilt");
        time = nbttagcompound.getInteger("time");
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Item");
        this.setEntityItemStack(CompatWrapper.fromTag(nbttagcompound1));

        ItemStack item = getEntityItem();

        if (nbttagcompound.hasKey("shooter"))
        {
            shooter = UUID.fromString(nbttagcompound.getString("shooter"));
        }

        if (!CompatWrapper.isValid(item))
        {
            this.setDead();
        }
        this.tilePos = new BlockPos(nbttagcompound.getInteger("xTile"), nbttagcompound.getInteger("yTile"),
                nbttagcompound.getInteger("zTile"));
        this.ticksInGround = nbttagcompound.getShort("life");
        this.tile = Block.getBlockById(nbttagcompound.getByte("inTile") & 255);
        this.inData = nbttagcompound.getByte("inData") & 255;
        this.arrowShake = nbttagcompound.getByte("shake") & 255;
        this.inGround = nbttagcompound.getByte("inGround") == 1;
    }

    public IPokemob sendOut()
    {
        if (world.isRemote || isReleasing()) { return null; }
        IPokemob entity1 = PokecubeManager.itemToPokemob(getEntityItem(), world);
        if (entity1 != null)
        {
            Vector3 v = v0.set(this).addTo(-motionX, -motionY, -motionZ);
            Vector3 dv = v1.set(motionX, motionY, motionZ);
            v = Vector3.getNextSurfacePoint(world, v, dv, Math.max(2, dv.mag()));
            if (v == null) v = v0.set(this);
            v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
            IBlockState state = v.getBlockState(world);
            if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
            EntityLiving entity = (EntityLiving) entity1;
            entity.fallDistance = 0;
            v.moveEntity(((Entity) entity1));

            SendOut evt = new SendOut.Pre(entity1.getPokedexEntry(), v, world, entity1);
            if (MinecraftForge.EVENT_BUS.post(evt))
            {
                if (shootingEntity != null && shootingEntity instanceof EntityPlayer)
                {
                    Tools.giveItem((EntityPlayer) shootingEntity, getEntityItem());
                    this.setDead();
                }
                return null;
            }

            world.spawnEntity((Entity) entity1);
            entity1.popFromPokecube();
            entity1.setPokemonAIState(IMoveConstants.ANGRY, false);
            entity1.setPokemonAIState(IMoveConstants.TAMED, true);
            entity1.setPokemonAIState(IMoveConstants.EXITINGCUBE, true);
            Entity owner = entity1.getPokemonOwner();
            if (owner instanceof EntityPlayer)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.sendout", "green",
                        entity1.getPokemonDisplayName());
                entity1.displayMessageToOwner(mess);
            }

            if (((EntityLiving) entity1).getHealth() <= 0)
            {
                // notify the mob is dead
                this.world.setEntityState((Entity) entity1, (byte) 3);
            }
            setReleased((Entity) entity1);
            motionX = motionY = motionZ = 0;
            time = 10;
            setReleasing(true);
            evt = new SendOut.Post(entity1.getPokedexEntry(), v, world, entity1);
            MinecraftForge.EVENT_BUS.post(evt);
        }
        else
        {
            System.err.println("Send out no pokemob?");
            this.entityDropItem(getEntityItem(), 0.5f);
            this.setDead();
        }
        return entity1;
    }
}
