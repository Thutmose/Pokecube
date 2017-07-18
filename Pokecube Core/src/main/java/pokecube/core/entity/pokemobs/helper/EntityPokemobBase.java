/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokemobBodies;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** @author Manchou, Thutmose */
public abstract class EntityPokemobBase extends EntityHungryPokemob implements IEntityMultiPart, TagNames
{
    public static boolean       multibox       = true;

    private int                 uid            = -1;
    private ItemStack           pokecube       = CompatWrapper.nullStack;
    private int                 despawntimer   = 0;

    private int[]               flavourAmounts = new int[5];
    private EntityPokemobPart[] partsArray;

    public Matrix3              mainBox;
    private Vector3             offset         = Vector3.getNewVector();

    private float               nextStepDistance;

    public EntityPokemobBase(World world)
    {
        super(world);
        this.setSize(1, 1);
        this.width = 1;
        this.height = 1;
        nextStepDistance = 1;
    }

    /** Returns true if other Entities should be prevented from moving through
     * this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isRiding();
    }

    /** Returns true if this entity should push and be pushed by other entities
     * when colliding. */
    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    protected boolean canDespawn()
    {
        boolean canDespawn = getHungerTime() > PokecubeMod.core.getConfig().pokemobLifeSpan;
        boolean checks = getPokemonAIState(IMoveConstants.TAMED) || this.getPokemonOwner() != null
                || getPokemonAIState(ANGRY) || getAttackTarget() != null || this.hasCustomName() || isAncient()
                || isNoDespawnRequired();
        despawntimer--;
        if (checks) return false;

        boolean player = Tools.isAnyPlayerInRange(PokecubeMod.core.getConfig().cullDistance, this);
        boolean cull = PokecubeMod.core.getConfig().cull && !player;
        if (cull && despawntimer < 0)
        {
            despawntimer = 80;
            cull = false;
        }
        else if (cull && despawntimer > 0)
        {
            cull = false;
        }
        return canDespawn || cull;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    /** Makes the entity despawn if requirements are reached */
    @Override
    protected void despawnEntity()
    {
        if (!this.canDespawn() || this.world.isRemote) return;
        Result result = ForgeEventFactory.canEntityDespawn(this);
        if (result == Result.DENY) return;
        SpawnEvent.Despawn evt = new SpawnEvent.Despawn(here, world, this);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled()) return;
        this.setDead();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public SoundEvent getAmbientSound()
    {
        return getPokedexEntry().getSoundEvent();
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    @Override
    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor(posX);
        int j = MathHelper.floor(getEntityBoundingBox().minY);
        int k = MathHelper.floor(posZ);
        here.set(i, j, k);
        float weight = getBlockPathWeight(world, here);
        return weight >= 0.0F && weight <= 100;
    }

    /** returns the bounding box for this entity */
    @Override
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return null;// boundingBox;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return getAmbientSound();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        ITextComponent textcomponentstring = getPokemonDisplayName();
        textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
        textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
        return textcomponentstring;
    }

    @Override
    public float getEyeHeight()
    {
        return height * 0.8F;
    }

    @Override
    public EntityAIBase getGuardAI()
    {
        return guardAI;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return getAmbientSound();
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 8;
    }

    /** Hopefully this will fix the mod.pokemon kill messages */
    @Override
    public String getName()
    {
        return this.getPokedexEntry().getName();
    }

    @Override
    public ItemStack getPokecube()
    {
        return pokecube;
    }

    @Override
    public int getPokemonUID()
    {
        if (uid == -1) this.uid = PokecubeSerializer.getInstance().getNextID();
        return uid;
    }

    @Override
    public SoundEvent getSound()
    {
        return getPokedexEntry().getSoundEvent();
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.15F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getTexture()
    {
        return modifyTexture(null);
    }

    @Override
    public EntityAIBase getUtilityMoveAI()
    {
        return utilMoveAI;
    }

    @Override
    public int getVerticalFaceSpeed()
    {
        if (getPokemonAIState(SITTING)) { return 20; }
        return super.getVerticalFaceSpeed();
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        this.getPokedexEntry();
        this.getDisplayName();

        if (multibox)
        {
            this.noClip = true;
        }

        this.initRidable();

        isImmuneToFire = isType(PokeType.getType("fire"));
    }

    /** Checks if this entity is inside of an opaque block */
    @Override
    public boolean isEntityInsideOpaqueBlock()
    {
        return false;
    }

    /** Whether or not the current entity is in lava */
    @Override
    public boolean isInLava()
    {
        return getPokemonAIState(INLAVA);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        String domain = texture == null ? getPokedexEntry().getModId() : texture.getResourceDomain();
        String texName = texture == null ? null : texture.getResourcePath();
        texName = this.getPokedexEntry().getTexture(texName, this.getSexe(), this.ticksExisted);
        texture = new ResourceLocation(domain, texName);
        if (!isShiny()) return texture;
        String args = texName.substring(0, texName.length() - 4);
        return new ResourceLocation(domain, args + "s.png");
    }

    List<AxisAlignedBB> aabbs = null;

    public List<AxisAlignedBB> getTileCollsionBoxes()
    {
        if (this.world.isRemote && this.isBeingRidden()
                && (this.getServer() == null || this.getServer().isDedicatedServer())
                && this.getOwner() == PokecubeCore.proxy.getPlayer((String) null))
        {
            Vector3 vec = Vector3.getNewVector();
            Vector3 vec2 = Vector3.getNewVector();
            double x = getPokedexEntry().width * getSize();
            double z = getPokedexEntry().length * getSize();
            double y = getPokedexEntry().height * getSize();
            double v = vec.setToVelocity(this).mag();
            vec.set(this);
            vec2.set(x + v, y + v, z + v);
            Matrix3 mainBox = new Matrix3();
            Vector3 offset = Vector3.getNewVector();
            mainBox.boxMin().clear();
            mainBox.boxMax().x = x;
            mainBox.boxMax().z = y;
            mainBox.boxMax().y = z;
            offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
            double ar = mainBox.boxMax().x / mainBox.boxMax().z;
            if (ar > 2 || ar < 0.5) mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
            mainBox.addOffsetTo(offset).addOffsetTo(vec);
            AxisAlignedBB box = mainBox.getBoundingBox();
            AxisAlignedBB box1 = box.grow(2 + x, 2 + y, 2 + z);
            box1 = box1.grow(motionX, motionY, motionZ);
            aabbs = mainBox.getCollidingBoxes(box1, world, world);
            // Matrix3.mergeAABBs(aabbs, x/2, y/2, z/2);
            Matrix3.expandAABBs(aabbs, box);
            if (box.getAverageEdgeLength() < 3) Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        }
        return aabbs;
    }

    public void setTileCollsionBoxes(List<AxisAlignedBB> list)
    {
        aabbs = list;
    }

    @Override
    public boolean fits(IBlockAccess world, Vector3 location, @Nullable Vector3 directionFrom)
    {
        Vector3 diffs = Vector3.getNewVector();
        mainBox.boxMin().clear();
        mainBox.boxMax().x = getPokedexEntry().width * getSize();
        mainBox.boxMax().z = getPokedexEntry().length * getSize();
        mainBox.boxMax().y = getPokedexEntry().height * getSize();
        float diff = (float) Math.max(mainBox.boxMax().y, Math.max(mainBox.boxMax().x, mainBox.boxMax().z));
        offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
        double ar = mainBox.boxMax().x / mainBox.boxMax().z;
        if (ar > 2 || ar < 0.5) mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
        mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
        Vector3 pos = offset.add(location);
        AxisAlignedBB aabb = mainBox.addOffsetTo(pos).getBoundingBox();
        if (aabb.maxX - aabb.minX > 3)
        {
            double meanX = aabb.minX + (aabb.maxX - aabb.minX) / 2;
            aabb = new AxisAlignedBB(meanX - 3, aabb.minY, aabb.minZ, meanX + 3, aabb.maxY, aabb.maxZ);
        }
        if (aabb.maxZ - aabb.minZ > 3)
        {
            double meanZ = aabb.minZ + (aabb.maxZ - aabb.minZ) / 2;
            aabb = new AxisAlignedBB(aabb.minX, aabb.minY, meanZ - 3, aabb.maxX, aabb.maxY, meanZ + 3);
        }
        if (aabb.maxY - aabb.minY > 3)
        {
            aabb = aabb.setMaxY(aabb.minY + 3);
        }
        aabb = aabb.grow(Math.min(3, diff));
        List<AxisAlignedBB> aabbs = new Matrix3().getCollidingBoxes(aabb, this.world, world);
        Matrix3.expandAABBs(aabbs, aabb);
        if (aabb.getAverageEdgeLength() < 3) Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        boolean collides = mainBox.doTileCollision(world, aabbs, Vector3.empty, this, diffs);
        return !collides;
    }

    /** Tries to moves the entity by the passed in displacement. Args: x, y,
     * z */
    @Override
    public void move(MoverType type, double x, double y, double z)
    {
        if (!this.addedToChunk) return;
        boolean normalSize = this.height > 0.5 && this.width < 1 && this.width > 0.25 && this.length < 1
                && this.length > 0.25;
        float max = Math.max(width, length);
        float min = Math.min(width, length);
        if (max / min < 2) normalSize = true;
        if (!multibox || normalSize || mainBox == null)
        {
            this.noClip = false;
            super.move(type, x, y, z);
            return;
        }
        else if (aabbs != null)
        {
            double x0 = x, y0 = y, z0 = z;
            Vector3 diffs = Vector3.getNewVector();
            diffs.set(x, y, z);
            boolean multi = false;
            if (getParts() != null && multi)
            {
                Matrix3 box = new Matrix3();
                box.set(getEntityBoundingBox());
                getTileCollsionBoxes();
                diffs.set(box.doTileCollision(world, aabbs, this, Vector3.empty, diffs, false));
                for (EntityPokemobPart e : partsArray)
                {
                    Vector3 v = Vector3.getNewVector().set(e.offset.x, e.offset.y, e.offset.z);
                    v.scalarMultBy(getSize());
                    Vector3 v0 = v.copy();
                    float sin = MathHelper.sin(this.rotationYaw * 0.017453292F);
                    float cos = MathHelper.cos(this.rotationYaw * 0.017453292F);
                    v.x = v0.x * cos - v0.z * sin;
                    v.z = v0.x * sin + v0.z * cos;
                    e.motionX = motionX;
                    e.motionY = motionY;
                    e.motionZ = motionZ;
                    e.setPosition(posX + v.x, posY + v.y, posZ + v.z);
                    box.set(e.defaultBox.offset(e.posX, e.posY, e.posZ));
                    diffs.set(box.doTileCollision(world, aabbs, e, Vector3.empty, diffs, false));
                }
                x = diffs.x;
                y = diffs.y;
                z = diffs.z;
            }
            else
            {
                mainBox.boxMin().clear();
                mainBox.boxMax().x = getPokedexEntry().width * getSize();
                mainBox.boxMax().z = getPokedexEntry().length * getSize();
                mainBox.boxMax().y = getPokedexEntry().height * getSize();
                offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
                double ar = mainBox.boxMax().x / mainBox.boxMax().z;
                if (ar > 2 || ar < 0.5) mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
                mainBox.addOffsetTo(offset).addOffsetTo(here);
                this.setEntityBoundingBox(mainBox.getBoundingBox());
                getTileCollsionBoxes();
                diffs.set(mainBox.doTileCollision(world, aabbs, this, Vector3.empty, diffs, false));
                x = diffs.x;
                y = diffs.y;
                z = diffs.z;
            }

            // TODO implement stepping upwards

            this.posX = here.x;
            this.posY = here.y;
            this.posZ = here.z;

            x = diffs.x;
            y = diffs.y;
            z = diffs.z;

            double dy = 0;
            double yOff = this.yOffset;
            double newY = y + yOff + dy;
            double size = Math.max(this.getSize() * getPokedexEntry().length, 3);
            Vector3 dir = Vector3.getNewVector().set(x, newY, z).norm().scalarMult(size);
            boolean border = this.world.getWorldBorder().contains(getEntityBoundingBox().offset(dir.x, dir.y, dir.z));
            if (!border)
            {
                x = newY = z = 0;
            }

            if (newY == 0)
            {
                motionY = 0;
            }
            this.posX += x;
            this.posY += newY;
            this.posZ += z;
            this.setPosition(posX, posY, posZ);

            this.isCollidedHorizontally = x0 != x || z0 != z;
            this.isCollidedVertically = y0 != y;
            this.onGround = y0 != y && y0 <= 0.0D;
            this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
            BlockPos blockpos = getPosition().down();
            IBlockState state = world.getBlockState(blockpos);
            Block block1 = state.getBlock();

            this.updateFallState(y, this.onGround, state, blockpos);

            if (this.canTriggerWalking() && this.getRidingEntity() == null)
            {
                double d15 = this.posX;
                double d16 = this.posY;
                double d17 = this.posZ;

                if (block1 != Blocks.LADDER)
                {
                    d16 = 0.0D;
                }

                if (block1 != null && this.onGround)
                {
                    block1.onEntityCollidedWithBlock(this.world, blockpos, state, this);
                }

                this.distanceWalkedModified = (float) (this.distanceWalkedModified
                        + MathHelper.sqrt(d15 * d15 + d17 * d17) * 0.6D);
                this.distanceWalkedOnStepModified = (float) (this.distanceWalkedOnStepModified
                        + MathHelper.sqrt(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

                if (this.distanceWalkedOnStepModified > this.nextStepDistance && state.getMaterial() != Material.AIR)
                {
                    this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater() && !swims())
                    {
                        float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D
                                + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D)
                                * 0.35F;

                        if (f > 1.0F)
                        {
                            f = 1.0F;
                        }

                        this.playSound(this.getSwimSound(), f,
                                1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    }
                }
            }

            try
            {
                this.doBlockCollisions();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport
                        .makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }

    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (getParts() == null)
        {
            PokemobBodies.initBody(this);
        }
        if (getParts() != null)
        {
            for (Entity e : getParts())
            {
                e.onUpdate();
            }
        }
        if (uid == -1) this.uid = PokecubeSerializer.getInstance().getNextID();
        if (isAncient())
        {
            // BossStatus.setBossStatus(this, true);
            // BossStatus.bossName = getPokemonDisplayName();
            // TODO Boss Stuff
        }
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt entitylightningbolt)
    {
        // do nothing
    }

    double average = 0;

    @Override
    public void onUpdate()
    {
        if (!Pokedex.getInstance().isRegistered(getPokedexEntry())) return;

        long time = System.nanoTime();
        here.set(posX, posY, posZ);
        BlockPos pos = new BlockPos(posX, 1, posZ);
        boolean loaded = world.isAreaLoaded(pos, 8);
        if (loaded && !(getPokemonAIState(STAYING) || getPokemonAIState(GUARDING) || getPokemonAIState(ANGRY)
                || getAttackTarget() != null))
        {
            loaded = Tools.isAnyPlayerInRange(PokecubeMod.core.getConfig().aiDisableDistance, this);
        }
        if (!loaded)
        {
            despawnEntity();
            return;
        }
        super.onUpdate();
        double dt = (System.nanoTime() - time) / 10e3D;
        average = ((average * (ticksExisted - 1)) + dt) / ticksExisted;
        double toolong = 500;
        if (PokecubeMod.core.getConfig().debug && dt > toolong && !world.isRemote)
        {
            here.set(here.getPos());
            String toLog = "%3$s took %2$s\u00B5s to tick, it is located at %1$s, the average has been %4$s\u00B5s";
            PokecubeMod.log(String.format(toLog, here, (int) dt, getPokemonDisplayName().getUnformattedComponentText(),
                    ((int) (average * 100)) / 100d));
        }
    }

    @Override
    public void popFromPokecube()
    {
        super.popFromPokecube();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey(POKEMOBTAG))
        {
            NBTTagCompound pokemobTag = nbttagcompound.getCompoundTag(POKEMOBTAG);
            readPokemobData(pokemobTag);
            return;
        }
        GeneticsManager.handleLoad(this);
    }

    @Override
    public void readPokemobData(NBTTagCompound tag)
    {
        NBTTagCompound ownerShipTag = tag.getCompoundTag(OWNERSHIPTAG);
        NBTTagCompound statsTag = tag.getCompoundTag(STATSTAG);
        NBTTagCompound movesTag = tag.getCompoundTag(MOVESTAG);
        NBTTagCompound inventoryTag = tag.getCompoundTag(INVENTORYTAG);
        NBTTagCompound breedingTag = tag.getCompoundTag(BREEDINGTAG);
        NBTTagCompound visualsTag = tag.getCompoundTag(VISUALSTAG);
        NBTTagCompound aiTag = tag.getCompoundTag(AITAG);
        NBTTagCompound miscTag = tag.getCompoundTag(MISCTAG);
        // Read Ownership Tag
        if (!ownerShipTag.hasNoTags())
        {
            this.setPokemobTeam(ownerShipTag.getString(TEAM));
            this.setPokemonNickname(ownerShipTag.getString(NICKNAME));
            this.players = ownerShipTag.getBoolean(PLAYERS);
            try
            {
                if (ownerShipTag.hasKey(OT)) this.setOriginalOwnerUUID(UUID.fromString(ownerShipTag.getString(OT)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                if (ownerShipTag.hasKey(OWNER)) this.setPokemonOwner(UUID.fromString(ownerShipTag.getString(OWNER)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            this.setTraded(ownerShipTag.getBoolean(ISTRADED));
        }
        // Read stats tag
        if (!statsTag.hasNoTags())
        {
            this.setExp(statsTag.getInteger(EXP), false);
            this.setStatus(statsTag.getByte(STATUS));
            addHappiness(statsTag.getInteger(HAPPY));
        }
        // Read moves tag
        if (!movesTag.hasNoTags())
        {
            getMoveStats().newMoves = movesTag.getByte(NUMNEWMOVES);
            this.setMoveIndex(movesTag.getInteger(MOVEINDEX));
            this.getDataManager().set(LASTMOVE, movesTag.getString(LASTUSED));
            this.setAttackCooldown(movesTag.getInteger(COOLDOWN));
        }
        // Read Inventory tag
        if (!inventoryTag.hasNoTags())
        {
            NBTTagList nbttaglist = inventoryTag.getTagList(ITEMS, 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;
                if (j < this.getPokemobInventory().getSizeInventory())
                {
                    this.getPokemobInventory().setInventorySlotContents(j, CompatWrapper.fromTag(nbttagcompound1));
                }
                dataManager.set(HELDITEM, this.getPokemobInventory().getStackInSlot(1));
            }
            handleArmourAndSaddle();
        }
        // Read Breeding tag
        if (!breedingTag.hasNoTags())
        {
            this.setSexe(breedingTag.getByte(SEXE));
            this.loveTimer = breedingTag.getInteger(SEXETIME);
        }
        // Read visuals tag
        if (!visualsTag.hasNoTags())
        {
            this.setSpecialInfo(visualsTag.getInteger(SPECIALTAG));
            setSize(getSize());
            this.initRidable();
            flavourAmounts = visualsTag.getIntArray(FLAVOURSTAG);
            if (visualsTag.hasKey(POKECUBE))
            {
                NBTTagCompound pokecubeTag = visualsTag.getCompoundTag(POKECUBE);
                this.setPokecube(CompatWrapper.fromTag(pokecubeTag));
            }
        }

        // Read AI
        if (!aiTag.hasNoTags())
        {
            dataManager.set(AIACTIONSTATESDW, aiTag.getInteger(AISTATE));
            setHungerTime(aiTag.getInteger(HUNGER));
            int[] home = aiTag.getIntArray(HOME);
            if (home.length == 4)
            {
                setHome(home[0], home[1], home[2], home[3]);
            }
        }
        // Read Misc other
        if (!miscTag.hasNoTags())
        {
            this.setRNGValue(miscTag.getInteger(RNGVAL));
            this.uid = miscTag.getInteger(UID);
            this.setAncient(miscTag.getBoolean(ANCIENT));
            this.wasShadow = miscTag.getBoolean(WASSHADOW);
        }
    }

    @Override
    public void setPokecube(ItemStack pokeballId)
    {
        if (CompatWrapper.isValid(pokeballId))
        {
            pokeballId = pokeballId.copy();
            CompatWrapper.setStackSize(pokeballId, 1);
            if (pokeballId.hasTagCompound() && pokeballId.getTagCompound().hasKey("Pokemob"))
                pokeballId.getTagCompound().removeTag("Pokemob");
        }
        pokecube = pokeballId;
    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb)
    {
        super.setEntityBoundingBox(bb);
    }

    @Override
    public void setSize(float size)
    {
        super.setSize(size);
        float a = 1, b = 1, c = 1;
        PokedexEntry entry = getPokedexEntry();
        if (entry != null)
        {
            a = entry.width * getSize();
            b = entry.height * getSize();
            c = entry.length * getSize();
        }

        this.width = a;
        this.height = b;
        this.length = c;

        if (a > 3 || b > 3 || c > 3)
        {
            this.ignoreFrustumCheck = true;
        }
        this.setSize(width, height);
        this.setEntityBoundingBox(new AxisAlignedBB(this.getEntityBoundingBox().minX, this.getEntityBoundingBox().minY,
                this.getEntityBoundingBox().minZ, this.getEntityBoundingBox().minX + this.width,
                this.getEntityBoundingBox().minY + this.height, this.getEntityBoundingBox().minZ + this.width));
        double max = Math.max(Math.max(a, b), c);
        World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, max);
        mainBox = new Matrix3(a, b, c);
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        super.collideWithNearbyEntities();
    }

    @Override
    public void specificSpawnInit()
    {
        super.specificSpawnInit();
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
    {
        GeneticsManager.handleEpigenetics(this);
        return super.writeToNBT(nbttagcompound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        NBTTagCompound pokemobTag = writePokemobData();
        nbttagcompound.setTag(POKEMOBTAG, pokemobTag);
    }

    @Override
    public NBTTagCompound writePokemobData()
    {
        NBTTagCompound pokemobTag = new NBTTagCompound();
        pokemobTag.setInteger(VERSION, 1);
        // Write Ownership tag
        NBTTagCompound ownerShipTag = new NBTTagCompound();
        // This is still written for pokecubes to read from. Actual number is
        // stored in genes.
        ownerShipTag.setInteger(POKEDEXNB, this.getPokedexNb());
        ownerShipTag.setString(NICKNAME, getPokemonNickname());
        ownerShipTag.setBoolean(PLAYERS, isPlayerOwned());
        ownerShipTag.setString(TEAM, getPokemobTeam());
        if (getOriginalOwnerUUID() != null) ownerShipTag.setString(OT, getOriginalOwnerUUID().toString());
        if (getPokemonOwnerID() != null) ownerShipTag.setString(OWNER, getPokemonOwnerID().toString());
        ownerShipTag.setBoolean(ISTRADED, getPokemonAIState(TRADED));

        // Write stats tag
        NBTTagCompound statsTag = new NBTTagCompound();
        statsTag.setInteger(EXP, getExp());
        statsTag.setByte(STATUS, getStatus());
        statsTag.setInteger(HAPPY, bonusHappiness);

        // Write moves tag
        NBTTagCompound movesTag = new NBTTagCompound();
        movesTag.setInteger(MOVEINDEX, getMoveIndex());
        movesTag.setByte(NUMNEWMOVES, (byte) getMoveStats().newMoves);
        movesTag.setString(LASTUSED, getDataManager().get(LASTMOVE));
        movesTag.setInteger(COOLDOWN, getAttackCooldown());

        // Write Inventory tag
        NBTTagCompound inventoryTag = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        this.getPokemobInventory().setInventorySlotContents(1, this.getHeldItemMainhand());
        for (int i = 0; i < this.getPokemobInventory().getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.getPokemobInventory().getStackInSlot(i);
            if (CompatWrapper.isValid(itemstack))
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        inventoryTag.setTag(ITEMS, nbttaglist);

        // Write Breeding tag
        NBTTagCompound breedingTag = new NBTTagCompound();
        breedingTag.setByte(SEXE, getSexe());
        breedingTag.setInteger(SEXETIME, loveTimer);

        // Write visuals tag
        NBTTagCompound visualsTag = new NBTTagCompound();

        // This is still written for pokecubes to read from. Actual form is
        // stored in genes.
        visualsTag.setString(FORME, getPokedexEntry().getName());
        visualsTag.setInteger(SPECIALTAG, getSpecialInfo());
        visualsTag.setIntArray(FLAVOURSTAG, flavourAmounts);
        if (getPokecube() != CompatWrapper.nullStack)
        {
            NBTTagCompound pokecubeTag = pokecube.writeToNBT(new NBTTagCompound());
            visualsTag.setTag(POKECUBE, pokecubeTag);
        }
        // Misc AI
        NBTTagCompound aiTag = new NBTTagCompound();
        aiTag.setInteger(AISTATE, dataManager.get(AIACTIONSTATESDW));
        aiTag.setInteger(HUNGER, getHungerTime());
        aiTag.setIntArray(HOME,
                new int[] { getHome().getX(), getHome().getY(), getHome().getZ(), (int) getHomeDistance() });

        // Misc other
        NBTTagCompound miscTag = new NBTTagCompound();
        miscTag.setInteger(RNGVAL, getRNGValue());
        miscTag.setInteger(UID, uid);
        miscTag.setBoolean(ANCIENT, isAncient());
        miscTag.setBoolean(WASSHADOW, wasShadow);

        // Set tags to the pokemob tag.
        pokemobTag.setTag(OWNERSHIPTAG, ownerShipTag);
        pokemobTag.setTag(STATSTAG, statsTag);
        pokemobTag.setTag(MOVESTAG, movesTag);
        pokemobTag.setTag(INVENTORYTAG, inventoryTag);
        pokemobTag.setTag(BREEDINGTAG, breedingTag);
        pokemobTag.setTag(VISUALSTAG, visualsTag);
        pokemobTag.setTag(AITAG, aiTag);
        pokemobTag.setTag(MISCTAG, miscTag);
        return pokemobTag;
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        if (uid == -1)
        {
            uid = PokecubeSerializer.getInstance().getNextID();
        }
        PokecubeSerializer.getInstance().addPokemob(this);
        super.writeSpawnData(data);

    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Override
    public boolean attackEntityFromPart(MultiPartEntityPart dragonPart, DamageSource source, float damage)
    {
        return false;
    }

    @Override
    public Entity[] getParts()
    {
        return partsArray;
    }

    @Override
    public void setSubParts(EntityPokemobPart[] subParts)
    {
        this.partsArray = subParts;
    }

    @Override
    public void addEntityCrashInfo(CrashReportCategory category)
    {
        super.addEntityCrashInfo(category);
        category.addCrashSection("World:", world == null ? "NULL" : world.toString());
        category.addCrashSection("Owner:", getPokemonOwnerID() == null ? "NULL" : getPokemonOwnerID().toString());
        Thread.dumpStack();
    }
}
