package pokecube.core.items.pokecubes;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.entity.IMultibox;
import thut.api.maths.Vector3;

public class EntityPokecube extends EntityLiving implements IEntityAdditionalSpawnData, IProjectile
{

    public int              time = 0;
    public int              tilt = -1;
    public EntityLivingBase shootingEntity;
    public EntityLivingBase targetEntity;
    public UUID             shooter;

    private Vector3 v0    = Vector3.getNewVectorFromPool();
    private Vector3 v1    = Vector3.getNewVectorFromPool();
    public double   speed = 2;

    private BlockPos tilePos;
    private Block    tile;
    private int      inData;
    private boolean  inGround;
    /** 1 if the player can pick up the arrow */
    public int       canBePickedUp;
    /** Seems to be some sort of timer for animating an arrow. */
    public int       arrowShake;
    /** The owner of this arrow. */
    private int      ticksInGround;

    public EntityPokecube(World world)
    {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.renderDistanceWeight = 200;
        this.isImmuneToFire = true;
    }

    public EntityPokecube(World world, EntityLivingBase shootingEntity, ItemStack entityItem)
    {
        this(world);
        if (shootingEntity != null)
        {
            Vector3 start = Vector3.getNewVectorFromPool().set(shootingEntity, false);
            Vector3 dir = Vector3.getNewVectorFromPool().set(shootingEntity.getLookVec());
            start.addTo(dir).moveEntity(this);
            setVelocity(speed, dir);
            dir.freeVectorFromPool();
            start.freeVectorFromPool();
            shooter = shootingEntity.getPersistentID();
        }
        this.setEntityItemStack(entityItem);
        this.shootingEntity = shootingEntity;
        if (PokecubeManager.isFilled(entityItem)) tilt = -2;
    }

    public EntityPokecube(World world, EntityLivingBase shootingEntity, Entity target, ItemStack entityItem)
    {
        this(world);
        this.setEntityItemStack(entityItem);
        if (shootingEntity != null) shooter = shootingEntity.getPersistentID();

        Vector3 start = Vector3.getNewVectorFromPool().set(shootingEntity, false);
        start.moveEntity(this);
        Vector3 dir = Vector3.getNewVectorFromPool().set(target, false).subtract(start).normalize();
        setVelocity(speed, dir);
        start.freeVectorFromPool();
        dir.freeVectorFromPool();
        this.shootingEntity = shootingEntity;
        if (PokecubeManager.isFilled(entityItem)) tilt = -2;
    }

    public void setVelocity(double speed, Vector3 dir)
    {
        dir = dir.scalarMult(speed);
        dir.setVelocities(this);
    }

    /** Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone) */
    public ItemStack getEntityItem()
    {
        ItemStack itemstack = this.getDataWatcher().getWatchableObjectItemStack(24);
        return itemstack == null ? new ItemStack(Blocks.stone) : itemstack;
    }

    /** Sets the ItemStack for this entity */
    public void setEntityItemStack(ItemStack p_92058_1_)
    {
        this.getDataWatcher().updateObject(24, p_92058_1_);
        this.getDataWatcher().setObjectWatched(24);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        getDataWatcher().addObjectByDataType(24, 5);
        getDataWatcher().addObject(25, Byte.valueOf((byte) 0));
        getDataWatcher().addObject(29, -1);
    }

    public void setReleasing(boolean tag)
    {
        getDataWatcher().updateObject(25, tag ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
    }

    public boolean isReleasing()
    {
        return getDataWatcher().getWatchableObjectByte(25) == 1;
    }

    public void setReleased(Entity entity)
    {
        getDataWatcher().updateObject(29, entity.getEntityId());
    }

    public Entity getReleased()
    {
        int id = getDataWatcher().getWatchableObjectInt(29);
        Entity ret = worldObj.getEntityByID(id);
        return ret;
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
        this.setEntityItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound1));

        ItemStack item = getDataWatcher().getWatchableObjectItemStack(24);

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
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if (entityplayer.getCommandSenderName() == PokecubeManager.getOwner(getEntityItem())
                || entityplayer.getUniqueID().toString() == PokecubeManager.getOwner(getEntityItem()))
        {
            if (shootingEntity == entityplayer
                    && entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1)))
            {
                worldObj.playSoundAtEntity(this, "random.pop", 0.2F,
                        ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                entityplayer.onItemPickup(this, 1);
                setDead();
            }
        }
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

        if (e instanceof EntityLivingBase && e instanceof IPokemob && ((EntityLivingBase) e).getHealth() > 0
                && tilt == -1 && !((IPokemob) e).getPokemonAIState(IPokemob.TAMED))
        {
            IPokemob hitten = (IPokemob) e;
            if (hitten.getPokemonOwner() == shootingEntity) { return; }

            CaptureEvent.Pre capturePre = new Pre(hitten, this);
            MinecraftForge.EVENT_BUS.post(capturePre);

            if (capturePre.isCanceled())
            {

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
                Vector3 v = Vector3.getNewVectorFromPool();
                v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                motionX = 0;
                motionY = 0.1;
                motionZ = 0;
                v.freeVectorFromPool();

            }
        }
        else if (PokecubeManager.isFilled(getEntityItem()))
        {
            IPokemob entity1 = sendOut();
            if (entity1 != null && shootingEntity instanceof EntityLivingBase)
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
                        if (entityHit != null) entity1.setPokemonAIState(IPokemob.SITTING, false);

                        if (entityHit instanceof EntityCreature)
                        {
                            ((EntityCreature) entityHit).setAttackTarget((EntityLiving) entity1);
                        }
                        if (entityHit instanceof IPokemob)
                        {
                            ((IPokemob) entityHit).setPokemonAIState(IPokemob.ANGRY, true);
                            ;
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

    public IPokemob sendOut()
    {
        if (worldObj.isRemote || isReleasing()) { return null; }
        IPokemob entity1 = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
        System.out.println(entity1);
        if (entity1 != null)
        {
            Vector3 v = v0.set(this).addTo(-motionX, -motionY, -motionZ);
            Vector3 dv = v1.set(motionX, motionY, motionZ);
            v = Vector3.getNextSurfacePoint(worldObj, v, dv, Math.max(2, dv.mag()));
            if (v == null) v = v0.set(this);
            v.moveEntity(((Entity) entity1));
            worldObj.spawnEntityInWorld((Entity) entity1);
            ((IMultibox) entity1).setBoxes();
            ((IMultibox) entity1).setOffsets();

            boolean outOfBlock = true;

            if (!outOfBlock)
            {
                System.err.println(
                        String.format("The pokemob %1$s spawn from pokecube has failed to move out of a block. ",
                                entity1.getPokemonDisplayName()));
                if (entity1.getPokemonOwner() != null && entity1.getPokemonOwner() instanceof EntityPlayer)
                {
                    ((EntityPlayer) entity1.getPokemonOwner())
                            .addChatMessage(new ChatComponentText(StatCollector.translateToLocal("pokecube.noroom")));
                }
                entity1.returnToPokecube();
            }
            else
            {
                entity1.setPokemonAIState(IPokemob.ANGRY, false);
                entity1.setPokemonAIState(IPokemob.TAMED, true);
                entity1.setPokemonAIState(IPokemob.EXITINGCUBE, true);
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
        }
        else
        {
            this.setDead();
        }
        return entity1;
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        System.out.println(source);
        return false;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        buffer.writeDouble(motionX);
        buffer.writeDouble(motionY);
        buffer.writeDouble(motionZ);
    }

    @Override
    public void readSpawnData(ByteBuf buffer)
    {
        motionX = buffer.readDouble();
        motionY = buffer.readDouble();
        motionZ = buffer.readDouble();
    }

    @Override
    public void setThrowableHeading(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_,
            float p_70186_8_)
    {
        float f2 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
        p_70186_1_ /= (double) f2;
        p_70186_3_ /= (double) f2;
        p_70186_5_ /= (double) f2;
        p_70186_1_ += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * (double) p_70186_8_;
        p_70186_3_ += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * (double) p_70186_8_;
        p_70186_5_ += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
                * (double) p_70186_8_;
        p_70186_1_ *= (double) p_70186_7_;
        p_70186_3_ *= (double) p_70186_7_;
        p_70186_5_ *= (double) p_70186_7_;
        this.motionX = p_70186_1_;
        this.motionY = p_70186_3_;
        this.motionZ = p_70186_5_;
        float f3 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70186_3_, (double) f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
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

    /** Sets the velocity to the args. Args: x, y, z */
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
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70016_3_, (double) f) * 180.0D
                    / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    /** Called to update the entity's position/logic. */
    public void onUpdate()
    {
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

            int pokedexNumber = PokecubeManager.getPokedexNb(getEntityItem());
            IPokemob mob = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);

            HappinessType.applyHappiness(mob, HappinessType.TRADE);
            if (shootingEntity != null) mob.setPokemonOwner(((EntityPlayer) shootingEntity));
            ItemStack mobStack = PokecubeManager.pokemobToItem(mob);
            this.setEntityItemStack(mobStack);

            CaptureEvent.Post event = new CaptureEvent.Post(this);
            MinecraftForge.EVENT_BUS.post(event);

            if (!event.isCanceled())
            {
                mob = event.caught;
                mobStack = PokecubeManager.pokemobToItem(mob);
                if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
                {
                    String message = StatCollector.translateToLocalFormatted("pokecube.caught",
                            PokecubeMod.core.getTranslatedPokenameFromPokedexNumber(pokedexNumber));
                    ((EntityPlayer) shootingEntity).addChatMessage(new ChatComponentText("\u00a7d" + message));

                    worldObj.playSoundAtEntity(shootingEntity, PokecubeMod.ID + ":pokecube_caught", 0.5F, 1.0F);
                }
                setDead();
            }
            else
            {
                setDead();
            }
            return;
        }
        else if (time < 0 && tilt >= 4)
        {
            if (shootingEntity != null)
            {
                Vector3 here = Vector3.getNewVectorFromPool().set(this);
                Vector3 dir = Vector3.getNewVectorFromPool().set(shootingEntity);
                double dist = dir.distanceTo(here);
                dir.subtractFrom(here);
                dir.scalarMultBy(1 / (dist));
                dir.setVelocities(this);
                here.freeVectorFromPool();
                dir.freeVectorFromPool();

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
                            entity1.getPokemonDisplayName()));
                }

                entity1.setPokemonAIState(IPokemob.ANGRY, true);
                entity1.setPokemonAIState(IPokemob.SITTING, false);
                entity1.setPokemonAIState(IPokemob.TAMED, false);
                entity1.setPokemonOwnerByName("");

                if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
                {
                    ((EntityPlayer) shootingEntity).addChatMessage(
                            new ChatComponentText("\u00a7d" + StatCollector.translateToLocal("pokecube.missed")));
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
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f) * 180.0D
                    / Math.PI);
        }

        BlockPos pos = tilePos == null ? getPosition() : tilePos;
        IBlockState state = worldObj.getBlockState(pos);

        Block block = state.getBlock();

        if (block.getMaterial() != Material.air)
        {
            block.setBlockBoundsBasedOnState(this.worldObj, pos);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(worldObj, pos, state);

            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
                tilePos = pos;
            }
            if (block.getMaterial().isLiquid())
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
            if (tilt < 0)
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
        }

        if (targetEntity != null)
        {
            Vector3 here = Vector3.getNewVectorFromPool().set(this);
            Vector3 dir = Vector3.getNewVectorFromPool().set(targetEntity);
            double dist = dir.distanceTo(here);
            dir.subtractFrom(here);
            dir.scalarMultBy(1 / (dist));
            dir.setVelocities(this);
            here.freeVectorFromPool();
            dir.freeVectorFromPool();
        }

        super.onUpdate();

    }

    @Override
    public boolean interact(EntityPlayer player)
    {

        if (!player.worldObj.isRemote)
        {
            IPokemob pokemob = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
            if ((pokemob != null && pokemob.getPokemonOwner() == player && !isReleasing()) || pokemob == null)
            {
                this.setReleasing(true);
                if (!player.inventory.addItemStackToInventory(getEntityItem()))
                    this.entityDropItem(getEntityItem(), 0.5f);
                this.setDead();
            }
            else if (!isReleasing() && pokemob != null)
            {
                sendOut();
            }
        }

        return super.interact(player);
    }

    protected void doBlockCollisions()
    {
        super.doBlockCollisions();
    }
}
