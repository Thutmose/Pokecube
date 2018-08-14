package pokecube.core.items.pokecubes;

import java.util.UUID;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.logicRunnables.LogicMiscUpdate;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.lib.CompatWrapper;

public class EntityPokecubeBase extends EntityLiving implements IEntityAdditionalSpawnData, IProjectile
{
    public static final String CUBETIMETAG = "lastCubeTime";

    public static boolean canCaptureBasedOnConfigs(IPokemob pokemob)
    {
        if (PokecubeCore.core
                .getConfig().captureDelayTillAttack) { return !pokemob.getCombatState(CombatStates.NOITEMUSE); }
        long lastAttempt = pokemob.getEntity().getEntityData().getLong(CUBETIMETAG);
        boolean capture = lastAttempt <= pokemob.getEntity().getEntityWorld().getTotalWorldTime();
        if (capture) pokemob.getEntity().getEntityData().removeTag(CUBETIMETAG);
        return capture;
    }

    public static void setNoCaptureBasedOnConfigs(IPokemob pokemob)
    {

        if (PokecubeCore.core.getConfig().captureDelayTillAttack)
            pokemob.setCombatState(CombatStates.NOITEMUSE, true);
        else pokemob.getEntity().getEntityData().setLong(CUBETIMETAG,
                pokemob.getEntity().getEntityWorld().getTotalWorldTime()
                        + PokecubeMod.core.getConfig().captureDelayTicks);
    }

    public static SoundEvent                      POKECUBESOUND;
    static final DataParameter<Integer>           ENTITYID       = EntityDataManager
            .<Integer> createKey(EntityPokecube.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> ITEM           = EntityDataManager
            .<ItemStack> createKey(EntityPokecube.class, DataSerializers.ITEM_STACK);
    static final DataParameter<Boolean>           RELEASING      = EntityDataManager
            .<Boolean> createKey(EntityPokecube.class, DataSerializers.BOOLEAN);

    public static boolean                         SEEKING        = true;

    /** Seems to be some sort of timer for animating an arrow. */
    public int                                    arrowShake;
    /** 1 if the player can pick up the arrow */
    public int                                    canBePickedUp;
    public boolean                                isLoot         = false;
    public ResourceLocation                       lootTable      = null;
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
            if (PokecubeManager.isFilled(getItem()))
            {
                IPokemob mob = CapabilityPokemob.getPokemobFor(this.sendOut());
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
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
        getDataManager().register(RELEASING, false);
        getDataManager().register(ENTITYID, -1);
    }

    /** Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone) */
    public ItemStack getItem()
    {
        ItemStack itemstack = this.getDataManager().get(ITEM);
        return itemstack == null ? new ItemStack(Blocks.STONE) : itemstack;
    }

    // For compatiblity.
    public ItemStack getEntityItem()
    {
        return getItem();
    }

    public Entity getReleased()
    {
        int id = getDataManager().get(ENTITYID);
        Entity ret = getEntityWorld().getEntityByID(id);
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
    public void setItem(ItemStack stack)
    {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    // For compatiblity
    public void setEntityItemStack(ItemStack stack)
    {
        setItem(stack);
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
    public void shoot(double x, double y, double z, float velocity, float inacurracy)
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
        IPokemob entity1 = PokecubeManager.itemToPokemob(getItem(), getEntityWorld());
        if (entity1 != null)
        {
            entity1.getEntity().setLocationAndAngles(posX, posY + 1.0D, posZ, rotationYaw, 0.0F);
            boolean ret = getEntityWorld().spawnEntity(entity1.getEntity());
            if (ret == false)
            {
                PokecubeMod.log(Level.SEVERE, String.format("The pokemob %1$s spawn from pokecube has failed. ",
                        entity1.getPokemonDisplayName().getFormattedText()));
            }
            setNoCaptureBasedOnConfigs(entity1);
            entity1.setCombatState(CombatStates.ANGRY, true);
            entity1.setLogicState(LogicStates.SITTING, false);
            entity1.setGeneralState(GeneralStates.TAMED, false);
            entity1.setPokemonOwner((UUID) null);
            if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
            {
                ITextComponent mess = new TextComponentTranslation("pokecube.missed", entity1.getPokemonDisplayName());
                ((EntityPlayer) shootingEntity).sendMessage(mess);
                entity1.getEntity().setAttackTarget(shootingEntity);
            }
        }
        else
        {
            sendOut();
        }
    }

    protected boolean captureSucceed()
    {
        PokecubeManager.setTilt(getItem(), -1);
        IPokemob mob = PokecubeManager.itemToPokemob(getItem(), getEntityWorld());
        if (mob == null)
        {
            if ((getItem().hasTagCompound() && getItem().getTagCompound().hasKey(TagNames.MOBID)))
            {
                Entity caught = EntityList.createEntityByIDFromName(
                        new ResourceLocation(getItem().getTagCompound().getString(TagNames.MOBID)), getEntityWorld());
                if (caught == null) return false;
                caught.readFromNBT(getItem().getTagCompound().getCompoundTag(TagNames.OTHERMOB));

                if (shootingEntity instanceof EntityPlayer && !(shootingEntity instanceof FakePlayer))
                {
                    if (caught instanceof EntityTameable)
                    {
                        ((EntityTameable) caught).setOwnerId(shootingEntity.getUniqueID());
                    }
                    else if (caught instanceof EntityHorse)
                    {// .1.12 use AbstractHorse instead
                        ((EntityHorse) caught).setOwnerUniqueId(shootingEntity.getUniqueID());
                    }
                    NBTTagCompound tag = new NBTTagCompound();
                    caught.writeToNBT(tag);
                    getItem().getTagCompound().setTag(TagNames.OTHERMOB, tag);
                    getItem().setStackDisplayName(caught.getDisplayName().getFormattedText());
                    ITextComponent mess = new TextComponentTranslation("pokecube.caught", caught.getDisplayName());
                    ((EntityPlayer) shootingEntity).sendMessage(mess);
                    this.setPosition(shootingEntity.posX, shootingEntity.posY, shootingEntity.posZ);
                    this.playSound(POKECUBESOUND, 1, 1);
                }
                return true;
            }
            new NullPointerException("Mob is null").printStackTrace();
            return false;
        }
        HappinessType.applyHappiness(mob, HappinessType.TRADE);
        if (shootingEntity != null && !mob.getGeneralState(GeneralStates.TAMED)) mob.setPokemonOwner((shootingEntity));
        if (mob.getCombatState(CombatStates.MEGAFORME) || mob.getPokedexEntry().isMega)
        {
            mob.setCombatState(CombatStates.MEGAFORME, false);
            IPokemob revert = mob.megaEvolve(mob.getPokedexEntry().getBaseForme());
            if (revert != null) mob = revert;
            if (mob.getEntity().getEntityData().hasKey(TagNames.ABILITY))
                mob.setAbility(AbilityManager.getAbility(mob.getEntity().getEntityData().getString(TagNames.ABILITY)));
        }
        ItemStack mobStack = PokecubeManager.pokemobToItem(mob);
        this.setItem(mobStack);
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
        if (this.getItem() != null)
        {
            nbttagcompound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
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
        this.setItem(new ItemStack(nbttagcompound1));

        ItemStack item = getItem();

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

    public EntityLivingBase sendOut()
    {
        if (getEntityWorld().isRemote || isReleasing()) { return null; }
        IPokemob entity1 = PokecubeManager.itemToPokemob(getItem(), getEntityWorld());
        Config config = PokecubeCore.core.getConfig();
        // Check permissions
        if (config.permsSendOut && shootingEntity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) shootingEntity;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            boolean denied = false;
            if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB, context)) denied = true;
            if (denied)
            {
                Tools.giveItem((EntityPlayer) shootingEntity, getItem());
                this.setDead();
                return null;
            }
        }
        if (entity1 != null)
        {
            // Check permissions
            if (config.permsSendOutSpecific && shootingEntity instanceof EntityPlayer)
            {
                PokedexEntry entry = entity1.getPokedexEntry();
                EntityPlayer player = (EntityPlayer) shootingEntity;
                IPermissionHandler handler = PermissionAPI.getPermissionHandler();
                PlayerContext context = new PlayerContext(player);
                boolean denied = false;
                if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(entry), context))
                    denied = true;
                if (denied)
                {
                    Tools.giveItem((EntityPlayer) shootingEntity, getItem());
                    this.setDead();
                    return null;
                }
            }

            Vector3 v = v0.set(this).addTo(-motionX, -motionY, -motionZ);
            Vector3 dv = v1.set(motionX, motionY, motionZ);
            v = Vector3.getNextSurfacePoint(getEntityWorld(), v, dv, Math.max(2, dv.mag()));
            if (v == null) v = v0.set(this);
            v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
            IBlockState state = v.getBlockState(getEntityWorld());
            if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
            EntityLiving entity = entity1.getEntity();
            entity.fallDistance = 0;
            v.moveEntity(entity);

            SendOut evt = new SendOut.Pre(entity1.getPokedexEntry(), v, getEntityWorld(), entity1);
            if (MinecraftForge.EVENT_BUS.post(evt))
            {
                if (shootingEntity != null && shootingEntity instanceof EntityPlayer)
                {
                    Tools.giveItem((EntityPlayer) shootingEntity, getItem());
                    this.setDead();
                }
                return null;
            }

            getEntityWorld().spawnEntity(entity);
            entity1.popFromPokecube();
            entity1.setGeneralState(GeneralStates.TAMED, true);
            entity1.setGeneralState(GeneralStates.EXITINGCUBE, true);
            entity1.setEvolutionTicks(50 + LogicMiscUpdate.EXITCUBEDURATION);
            Entity owner = entity1.getPokemonOwner();
            if (owner instanceof EntityPlayer)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.sendout", "green",
                        entity1.getPokemonDisplayName());
                entity1.displayMessageToOwner(mess);
            }

            if (entity.getHealth() <= 0)
            {
                // notify the mob is dead
                this.getEntityWorld().setEntityState(entity, (byte) 3);
            }
            setReleased(entity);
            motionX = motionY = motionZ = 0;
            time = 10;
            setReleasing(true);
            this.setItem(entity1.getPokecube());
            evt = new SendOut.Post(entity1.getPokedexEntry(), v, getEntityWorld(), entity1);
            MinecraftForge.EVENT_BUS.post(evt);
        }
        else
        {
            NBTTagCompound tag;
            if (getItem().hasTagCompound() && (tag = getItem().getTagCompound()).hasKey(TagNames.MOBID))
            {
                NBTTagCompound mobTag = tag.getCompoundTag(TagNames.OTHERMOB);
                ResourceLocation id = new ResourceLocation(tag.getString(TagNames.MOBID));
                Entity newMob = EntityList.createEntityByIDFromName(id, getEntityWorld());
                if (newMob != null && newMob instanceof EntityLivingBase)
                {
                    newMob.readFromNBT(mobTag);
                    Vector3 v = v0.set(this).addTo(-motionX, -motionY, -motionZ);
                    Vector3 dv = v1.set(motionX, motionY, motionZ);
                    v = Vector3.getNextSurfacePoint(getEntityWorld(), v, dv, Math.max(2, dv.mag()));
                    if (v == null) v = v0.set(this);
                    v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
                    IBlockState state = v.getBlockState(getEntityWorld());
                    if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
                    v.moveEntity(newMob);
                    getEntityWorld().spawnEntity(newMob);
                    tag.removeTag(TagNames.MOBID);
                    tag.removeTag(TagNames.OTHERMOB);
                    tag.removeTag("display");
                    tag.removeTag("tilt");
                    if (tag.hasNoTags()) getItem().setTagCompound(null);
                    entityDropItem(getItem(), 0.5f);
                    setReleased(newMob);
                    motionX = motionY = motionZ = 0;
                    time = 10;
                    setReleasing(true);
                    return (EntityLivingBase) newMob;
                }
            }
            System.err.println("Send out no pokemob?");
            Thread.dumpStack();
            this.entityDropItem(getItem(), 0.5f);
            this.setDead();
        }
        if (entity1 == null) return null;
        return entity1.getEntity();
    }
}