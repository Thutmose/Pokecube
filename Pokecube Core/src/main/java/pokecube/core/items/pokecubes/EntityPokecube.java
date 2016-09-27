package pokecube.core.items.pokecubes;

import java.util.UUID;

import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.commands.CommandTools;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketSound;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityPokecube extends EntityLiving implements IEntityAdditionalSpawnData, IProjectile
{

    private static final DataParameter<Optional<ItemStack>> ITEM           = EntityDataManager
            .<Optional<ItemStack>> createKey(EntityPokecube.class, DataSerializers.OPTIONAL_ITEM_STACK);
    static final DataParameter<Integer>                     ENTITYID       = EntityDataManager
            .<Integer> createKey(EntityPokecube.class, DataSerializers.VARINT);
    static final DataParameter<Boolean>                     RELEASING      = EntityDataManager
            .<Boolean> createKey(EntityPokecube.class, DataSerializers.BOOLEAN);

    public static boolean                                   SEEKING        = true;

    public static final SoundEvent                          CAUGHT_EVENT   = new SoundEvent(
            new ResourceLocation(PokecubeMod.ID + ":pokecube_caught"));

    public int                                              time           = 0;
    public int                                              tilt           = -1;
    public EntityLivingBase                                 shootingEntity;
    public EntityLivingBase                                 targetEntity;
    public Vector3                                          targetLocation = Vector3.getNewVector();
    public UUID                                             shooter;

    private Vector3                                         v0             = Vector3.getNewVector();
    private Vector3                                         v1             = Vector3.getNewVector();
    public double                                           speed          = 2;

    private BlockPos                                        tilePos;
    private Block                                           tile;
    private int                                             inData;
    private boolean                                         inGround;
    /** 1 if the player can pick up the arrow */
    public int                                              canBePickedUp;
    /** Seems to be some sort of timer for animating an arrow. */
    public int                                              arrowShake;
    /** The owner of this arrow. */
    private int                                             ticksInGround;

    public EntityPokecube(World world)
    {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.isImmuneToFire = true;
        this.enablePersistence();
    }

    public EntityPokecube(World world, EntityLivingBase shootingEntity, ItemStack entityItem)
    {
        this(world);
        if (shootingEntity != null)
        {
            Vector3 start = Vector3.getNewVector().set(shootingEntity, false);
            Vector3 dir = Vector3.getNewVector().set(shootingEntity.getLookVec());
            start.addTo(dir).moveEntity(this);
            setVelocity(speed, dir);
            shooter = shootingEntity.getPersistentID();
        }
        this.setEntityItemStack(entityItem);
        this.shootingEntity = shootingEntity;
        if (PokecubeManager.isFilled(entityItem)) tilt = -2;
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        super.collideWithNearbyEntities();
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    @Override
    public void applyEntityCollision(Entity e)
    {
        if (e == shootingEntity || isReleasing() || worldObj.isRemote || e instanceof EntityPokecube || e.isDead)
        {
            super.applyEntityCollision(e);
            return;
        }
        if (shootingEntity != null && e instanceof IPokemob
                && ((IPokemob) e).getPokemonOwner() == shootingEntity) { return; }

        if (e instanceof EntityLivingBase && e instanceof IPokemob && ((EntityLivingBase) e).getHealth() > 0
                && tilt == -1)
        {
            IPokemob hitten = (IPokemob) e;
            if (hitten.getPokemonOwner() == shootingEntity) { return; }

            int tiltBak = tilt;
            CaptureEvent.Pre capturePre = new Pre(hitten, this);
            MinecraftForge.EVENT_BUS.post(capturePre);
            if (capturePre.isCanceled() || capturePre.getResult() == Result.DENY)
            {
                if (tilt != tiltBak)
                {
                    if (tilt == 5)
                    {
                        time = 10;
                    }
                    else
                    {
                        time = 20 * tilt;
                    }
                    hitten.setPokecubeId(PokecubeItems.getCubeId(getEntityItem()));
                    setEntityItemStack(PokecubeManager.pokemobToItem(hitten));
                    PokecubeManager.setTilt(getEntityItem(), tilt);
                    ((Entity) hitten).setDead();
                    Vector3 v = Vector3.getNewVector();
                    v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                    motionX = 0;
                    motionY = 0.1;
                    motionZ = 0;
                }
            }
            else
            {
                int n = Tools.computeCatchRate(hitten, PokecubeItems.getCubeId(getEntityItem()));
                tilt = n;

                if (n == 5)
                {
                    time = 10;
                }
                else
                {
                    time = 20 * n;
                }

                hitten.setPokecubeId(PokecubeItems.getCubeId(getEntityItem()));
                setEntityItemStack(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(getEntityItem(), n);
                ((Entity) hitten).setDead();
                Vector3 v = Vector3.getNewVector();
                v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                motionX = 0;
                motionY = 0.1;
                motionZ = 0;
            }
        }
        else if (PokecubeManager.isFilled(getEntityItem()))
        {
            IPokemob entity1 = sendOut();
            if (entity1 != null && shootingEntity != null)
            {
                if (e instanceof EntityLivingBase)
                {
                    EntityLivingBase entityHit = (EntityLivingBase) e;
                    if (entityHit instanceof IPokemob && entity1.getPokemonOwnerName() != null
                            && entity1.getPokemonOwnerName().equals(((IPokemob) entityHit).getPokemonOwnerName()))
                    {
                        // do not attack a mob of the same team.
                    }
                    else
                    {
                        ((EntityCreature) entity1).setAttackTarget(entityHit);
                        entity1.setPokemonAIState(IMoveConstants.SITTING, false);
                        if (entityHit instanceof EntityCreature)
                        {
                            ((EntityCreature) entityHit).setAttackTarget((EntityLiving) entity1);
                        }
                        if (entityHit instanceof IPokemob)
                        {
                            ((IPokemob) entityHit).setPokemonAIState(IMoveConstants.ANGRY, true);
                        }
                    }
                }
            }
        }
        else
        {
            sendOut();
        }
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        return false;
    }

    public Entity getOwner()
    {
        if (PokecubeManager.isFilled(getEntityItem()))
        {
            String name = PokecubeManager.getOwner(getEntityItem());
            if (!name.isEmpty())
            {
                UUID id = UUID.fromString(name);
                EntityPlayer player = worldObj.getPlayerEntityByUUID(id);
                return player;
            }
        }
        return null;
    }

    public Entity copy()
    {
        EntityPokecube copy = new EntityPokecube(worldObj, shootingEntity, getEntityItem());
        copy.posX = this.posX;
        copy.posY = this.posY;
        copy.posZ = this.posZ;
        copy.motionX = this.motionX;
        copy.motionY = this.motionY;
        copy.motionZ = this.motionZ;
        copy.setEntityItemStack(getEntityItem());
        copy.tilt = this.tilt;
        copy.time = this.time;
        return copy;
    }

    @Override
    protected void doBlockCollisions()
    {
        super.doBlockCollisions();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.getDataManager().register(ITEM, Optional.<ItemStack> absent());
        getDataManager().register(RELEASING, false);
        getDataManager().register(ENTITYID, -1);
    }

    /** Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone) */
    public ItemStack getEntityItem()
    {
        ItemStack itemstack = (ItemStack) ((Optional<?>) this.getDataManager().get(ITEM)).orNull();
        return itemstack == null ? new ItemStack(Blocks.STONE) : itemstack;
    }

    public Entity getReleased()
    {
        int id = getDataManager().get(ENTITYID);
        Entity ret = worldObj.getEntityByID(id);
        return ret;
    }

    public boolean isReleasing()
    {
        return getDataManager().get(RELEASING);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if (entityplayer.getName() == PokecubeManager.getOwner(getEntityItem())
                || entityplayer.getCachedUniqueIdString() == PokecubeManager.getOwner(getEntityItem()))
        {
        }
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        boolean releasing = isReleasing();

        if (shooter != null && shootingEntity == null)
        {
            shootingEntity = worldObj.getPlayerEntityByUUID(shooter);
        }

        if (releasing)
        {
            motionX = motionY = motionZ = 0;
            this.setDead();
            return;
        }
        if (PokecubeManager.isFilled(getEntityItem())) time--;

        if (time == 0 && tilt >= 4) // Captured the pokemon
        {
            PokecubeManager.setTilt(getEntityItem(), -1);
            IPokemob mob = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
            if (mob == null)
            {
                new NullPointerException("Mob is null").printStackTrace();
                return;
            }
            HappinessType.applyHappiness(mob, HappinessType.TRADE);
            if (shootingEntity != null && !mob.getPokemonAIState(IMoveConstants.TAMED))
                mob.setPokemonOwner((shootingEntity));
            ItemStack mobStack = PokecubeManager.pokemobToItem(mob);
            this.setEntityItemStack(mobStack);
            if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokecube.caught", "green",
                        mob.getPokemonDisplayName());
                ((EntityPlayer) shootingEntity).addChatMessage(mess);
                this.setPosition(shootingEntity.posX, shootingEntity.posY, shootingEntity.posZ);
                PacketSound.sendMessage((EntityPlayer) shootingEntity, Vector3.getNewVector().set(shootingEntity),
                        "pokecube:pokecube_caught");
            }
            CaptureEvent.Post event = new CaptureEvent.Post(this);
            MinecraftForge.EVENT_BUS.post(event);
            setDead();
            return;
        }
        else if (time < 0 && tilt >= 4)
        {
            if (shootingEntity != null)
            {
                Vector3 here = Vector3.getNewVector().set(this);
                Vector3 dir = Vector3.getNewVector().set(shootingEntity);
                double dist = dir.distanceTo(here);
                dir.subtractFrom(here);
                dir.scalarMultBy(1 / (dist));
                dir.setVelocities(this);
            }
        }
        else if (time <= 0 && tilt >= 0) // Missed the pokemon
        {
            IPokemob entity1 = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);

            if (entity1 != null)
            {
                ((Entity) entity1).setLocationAndAngles(posX, posY + 1.0D, posZ, rotationYaw, 0.0F);
                boolean ret = worldObj.spawnEntityInWorld((Entity) entity1);

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
                entity1.setPokemonOwnerByName("");

                if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
                {
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokecube.missed", "red");
                    ((EntityPlayer) shootingEntity).addChatMessage(mess);
                    ((EntityCreature) entity1).setAttackTarget(shootingEntity);
                }
            }

            setDead();
            return;
        }

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D
                    / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / Math.PI);
        }

        BlockPos pos = tilePos == null ? getPosition() : tilePos;
        IBlockState state = worldObj.getBlockState(pos);

        Block block = state.getBlock();
        if (state.getMaterial() != Material.AIR)
        {
            AxisAlignedBB axisalignedbb = state.getBoundingBox(worldObj, pos);

            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
                tilePos = pos;
            }
            if (state.getMaterial().isLiquid())
            {
                motionY += 0.1;
            }
        }
        if (motionX == motionZ && motionZ == 0)
        {
            this.inGround = true;
        }

        if (this.inGround || tilt >= 0)
        {
            int j = block.getMetaFromState(state);

            if (block == this.tile && j == this.inData)
            {
                ++this.ticksInGround;
            }
            else
            {
                this.inGround = false;
                this.ticksInGround = 0;
            }
            if (tilt < 0 && !(targetEntity == null && targetLocation.isEmpty()))
            {
                if (PokecubeManager.isFilled(getEntityItem()))
                {
                    sendOut();
                }
                else
                {

                }
                return;
            }
        }
        if (tilt > 0 || (targetEntity != null && targetEntity.isDead))
        {
            targetEntity = null;
            if (!targetLocation.equals(Vector3.secondAxisNeg)) targetLocation.clear();
        }

        Vector3 target = Vector3.getNewVector();
        if (targetEntity != null)
        {
            target.set(targetEntity);
            if (target.distToEntity(this) < 2)
            {
                this.collideWithEntity(targetEntity);
            }
        }
        else
        {
            target.set(targetLocation);
        }
        if (!target.isEmpty() && target.y >= 0 && SEEKING)
        {
            Vector3 here = Vector3.getNewVector().set(this);
            Vector3 dir = Vector3.getNewVector().set(target);
            double dist = dir.distanceTo(here);
            if (dist > 1) dist = 1 / dist;
            else dist = 1;
            dir.subtractFrom(here);
            dir.scalarMultBy(dist);
            dir.setVelocities(this);
        }
    }

    @Override
    public void setDead()
    {
        super.setDead();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!player.getEntityWorld().isRemote)
        {
            if (!isReleasing())
            {
                IPokemob pokemob = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
                if (pokemob != null) sendOut();
                else
                {
                    EntityItem entityitem = player.dropItem(getEntityItem(), false);
                    if (entityitem != null)
                    {
                        entityitem.setNoPickupDelay();
                        entityitem.setOwner(player.getName());
                    }
                    this.setDead();
                }
            }
        }
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        tilt = nbttagcompound.getInteger("tilt");
        time = nbttagcompound.getInteger("time");
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Item");
        this.setEntityItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound1));

        ItemStack item = getEntityItem();

        if (nbttagcompound.hasKey("shooter"))
        {
            shooter = UUID.fromString(nbttagcompound.getString("shooter"));
        }

        if (item == null || item.stackSize <= 0)
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

    @Override
    public void readSpawnData(ByteBuf buffer)
    {
        motionX = buffer.readDouble();
        motionY = buffer.readDouble();
        motionZ = buffer.readDouble();
    }

    public IPokemob sendOut()
    {
        if (worldObj.isRemote || isReleasing()) { return null; }
        IPokemob entity1 = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
        if (entity1 != null)
        {
            Vector3 v = v0.set(this).addTo(-motionX, -motionY, -motionZ);
            Vector3 dv = v1.set(motionX, motionY, motionZ);
            v = Vector3.getNextSurfacePoint(worldObj, v, dv, Math.max(2, dv.mag()));
            if (v == null) v = v0.set(this);
            v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
            IBlockState state = v.getBlockState(worldObj);
            if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
            EntityLiving entity = (EntityLiving) entity1;
            entity.fallDistance = 0;
            v.moveEntity(((Entity) entity1));

            SendOut evt = new SendOut.Pre(entity1.getPokedexEntry(), v, worldObj, entity1);
            if (MinecraftForge.EVENT_BUS.post(evt))
            {
                if (shootingEntity != null && shootingEntity instanceof EntityPlayer)
                {
                    EntityItem entityitem = ((EntityPlayer) shootingEntity).dropItem(getEntityItem(), false);
                    if (entityitem != null)
                    {
                        entityitem.setNoPickupDelay();
                        entityitem.setOwner(shootingEntity.getName());
                    }
                    this.setDead();
                }
                return null;
            }

            worldObj.spawnEntityInWorld((Entity) entity1);

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
                this.worldObj.setEntityState((Entity) entity1, (byte) 3);
            }
            setReleased((Entity) entity1);
            motionX = motionY = motionZ = 0;
            time = 10;
            setReleasing(true);
            evt = new SendOut.Post(entity1.getPokedexEntry(), v, worldObj, entity1);
            MinecraftForge.EVENT_BUS.post(evt);
        }
        else
        {
            this.entityDropItem(getEntityItem(), 0.5f);
            this.setDead();
        }
        return entity1;
    }

    /** Sets the ItemStack for this entity */
    public void setEntityItemStack(ItemStack stack)
    {
        this.getDataManager().set(ITEM, Optional.fromNullable(stack));
        this.getDataManager().setDirty(ITEM);
    }

    /** Sets the position and rotation. Only difference from the other one is no
     * bounding on the rotation. Args: posX, posY, posZ, yaw, pitch */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_,
            float p_70056_8_, int p_70056_9_)
    {
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
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
    public void setThrowableHeading(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_,
            float p_70186_8_)
    {
        float f2 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
        p_70186_1_ /= f2;
        p_70186_3_ /= f2;
        p_70186_5_ /= f2;
        p_70186_1_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * p_70186_8_;
        p_70186_3_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * p_70186_8_;
        p_70186_5_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * p_70186_8_;
        p_70186_1_ *= p_70186_7_;
        p_70186_3_ *= p_70186_7_;
        p_70186_5_ *= p_70186_7_;
        this.motionX = p_70186_1_;
        this.motionY = p_70186_3_;
        this.motionZ = p_70186_5_;
        float f3 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70186_3_, f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    {
        this.motionX = p_70016_1_;
        this.motionY = p_70016_3_;
        this.motionZ = p_70016_5_;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70016_1_, p_70016_5_) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70016_3_, f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    public void setVelocity(double speed, Vector3 dir)
    {
        dir = dir.scalarMult(speed);
        dir.setVelocities(this);
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
    public void writeSpawnData(ByteBuf buffer)
    {
        buffer.writeDouble(motionX);
        buffer.writeDouble(motionY);
        buffer.writeDouble(motionZ);
    }
}
