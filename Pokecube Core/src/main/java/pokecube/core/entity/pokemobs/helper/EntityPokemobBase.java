/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
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
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokemobBodies;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

/** @author Manchou, Thutmose */
public abstract class EntityPokemobBase extends EntityHungryPokemob implements IEntityMultiPart
{

    static String[]             unowns            = { "Unown_A", "Unown_B", "Unown_C", "Unown_D", "Unown_E", "Unown_F",
            "Unown_G", "Unown_H", "Unown_I", "Unown_J", "Unown_K", "Unown_L", "Unown_M", "Unown_N", "Unown_O",
            "Unown_P", "Unown_Q", "Unown_Qu", "Unown_R", "Unown_S", "Unown_T", "Unown_U", "Unown_V", "Unown_W",
            "Unown_X", "Unown_Y", "Unown_Z", "Unown_Ex" };

    public static float         scaleFactor       = 0.075f;
    public static boolean       multibox          = true;

    private int                 uid               = -1;
    protected int               pokecubeId        = 0;
    private int                 despawntimer      = 0;

    protected int               particleIntensity = 0;
    protected int               particleCounter   = 0;
    protected String            particle;
    private float               scale;

    private int[]               flavourAmounts    = new int[5];
    private EntityPokemobPart[] partsArray;

    public Matrix3              mainBox;
    private Vector3             offset            = Vector3.getNewVector();

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
        if (!this.canDespawn() || this.worldObj.isRemote) return;
        Result result = ForgeEventFactory.canEntityDespawn(this);
        if (result == Result.DENY) return;
        SpawnEvent.Despawn evt = new SpawnEvent.Despawn(here, worldObj, this);
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
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(getEntityBoundingBox().minY);
        int k = MathHelper.floor_double(posZ);
        here.set(i, j, k);
        float weight = getBlockPathWeight(worldObj, here);
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
        TextComponentTranslation textcomponentstring = new TextComponentTranslation(
                this.getPokedexEntry().getUnlocalizedName());
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
    protected SoundEvent getHurtSound()
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
    public int getPokecubeId()
    {
        return pokecubeId;
    }

    @Override
    public Team getPokemobTeam()
    {
        return getTeam();
    }

    @Override
    public int getPokemonUID()
    {
        if (uid == -1) this.uid = PokecubeSerializer.getInstance().getNextID();

        return uid;
    }

    @Override
    public float getSize()
    {
        return (float) (scale * PokecubeMod.core.getConfig().scalefactor);
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
        if (getPokemonAIState(SITTING))
        {
            return 20;
        }
        return super.getVerticalFaceSpeed();
    }

    @Override
    public double getWeight()
    {
        return this.getSize() * this.getSize() * this.getSize() * getPokedexEntry().mass;
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        this.getPokedexEntry();

        if (multibox)
        {
            this.noClip = true;
        }

        Random random = new Random(getRNGValue());
        abilityIndex = random.nextInt(100) % 2;
        if (getPokedexEntry().getAbility(abilityIndex, this) == null)
        {
            if (abilityIndex != 0) abilityIndex = 0;
            else abilityIndex = 1;
        }
        setAbility(getPokedexEntry().getAbility(abilityIndex, this));
        if (getAbility() != null) getAbility().init(this);

        setSize(1 + scaleFactor * (float) (random).nextGaussian());
        this.initRidable();
        shiny = random.nextInt(8196) == 0;

        particle = null;
        particleCounter = 0;
        particleIntensity = 80;

        int rand = (random).nextInt(1048576);
        if (rand == 0)
        {
            rgba[0] = 0;
        }
        else if (rand == 1)
        {
            rgba[1] = 0;
        }
        else if (rand == 2)
        {
            rgba[2] = 0;
        }

        isImmuneToFire = isType(PokeType.fire);

        if (getPokedexNb() == 201)
        {
            int num = random.nextInt(unowns.length);
            changeForme(unowns[num]);
        }
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
    public boolean isInWater()
    {
        return super.isInWater();
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
        return new ResourceLocation(domain, args + "S.png");
    }

    List<AxisAlignedBB> aabbs = null;

    public List<AxisAlignedBB> getTileCollsionBoxes()
    {
        if (this.worldObj.isRemote && this.isBeingRidden()
                && (this.getServer() == null || this.getServer().isDedicatedServer())
                && this.getOwner() == PokecubeCore.proxy.getPlayer(null))
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
            mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
            mainBox.addOffsetTo(offset).addOffsetTo(vec);
            AxisAlignedBB box = mainBox.getBoundingBox();
            AxisAlignedBB box1 = box.expand(2 + x, 2 + y, 2 + z);
            box1 = box1.addCoord(motionX, motionY, motionZ);
            aabbs = mainBox.getCollidingBoxes(box1, worldObj, worldObj);
            // Matrix3.mergeAABBs(aabbs, x/2, y/2, z/2);
            Matrix3.expandAABBs(aabbs, box);
            Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
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
        // if (getParts() != null)
        // {
        // Matrix3 box = new Matrix3();
        // box.set(getEntityBoundingBox());
        // float diff = (float) Math.max(width, Math.max(height, length));
        // AxisAlignedBB aabb = getEntityBoundingBox().expandXyz(diff);
        // List<AxisAlignedBB> aabbs = new Matrix3().getCollidingBoxes(aabb,
        // worldObj, world);
        // Matrix3.expandAABBs(aabbs, getEntityBoundingBox());
        // Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        // diffs.set(box.doTileCollision(world, aabbs, this, Vector3.empty,
        // diffs, false));
        // for (EntityPokemobPart e : partsArray)
        // {
        // Vector3 v = Vector3.getNewVector().set(e.offset.x, e.offset.y,
        // e.offset.z);
        // v.scalarMultBy(getSize());
        // Vector3 v0 = v.copy();
        // float sin = MathHelper.sin((float) (this.rotationYaw *
        // 0.017453292F));
        // float cos = MathHelper.cos((float) (this.rotationYaw *
        // 0.017453292F));
        // v.x = v0.x * cos - v0.z * sin;
        // v.z = v0.x * sin + v0.z * cos;
        // e.motionX = motionX;
        // e.motionY = motionY;
        // e.motionZ = motionZ;
        // e.setPosition(posX + v.x, posY + v.y, posZ + v.z);
        // box.set(e.defaultBox.offset(e.posX, e.posY, e.posZ));
        // diffs.set(box.doTileCollision(world, aabbs, e, Vector3.empty, diffs,
        // false));
        // }
        // }
        // else
        {
            mainBox.boxMin().clear();
            mainBox.boxMax().x = getPokedexEntry().width * getSize();
            mainBox.boxMax().z = getPokedexEntry().length * getSize();
            mainBox.boxMax().y = getPokedexEntry().height * getSize();
            float diff = (float) Math.max(mainBox.boxMax().y, Math.max(mainBox.boxMax().x, mainBox.boxMax().z));
            offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
            mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
            Vector3 pos = offset.add(here);
            AxisAlignedBB aabb = mainBox.getBoundingBox().expandXyz(diff);
            List<AxisAlignedBB> aabbs = new Matrix3().getCollidingBoxes(aabb, worldObj, world);
            Matrix3.expandAABBs(aabbs, aabb);
            Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
            diffs.set(mainBox.doTileCollision(world, aabbs, this, pos, diffs, false));
        }
        return diffs.isEmpty();
    }

    /** Tries to moves the entity by the passed in displacement. Args: x, y,
     * z */
    @Override
    public void moveEntity(double x, double y, double z)
    {
        if (!multibox || !this.addedToChunk)
        {
            super.moveEntity(x, y, z);
            return;
        }
        else if (aabbs != null)
        {
            double x0 = x, y0 = y, z0 = z;
            IBlockAccess world = worldObj;
            Vector3 diffs = Vector3.getNewVector();
            diffs.set(x, y, z);
            boolean multi = false;
            if (getParts() != null && multi)
            {
                Matrix3 box = new Matrix3();
                box.set(getEntityBoundingBox());
                aabbs = getTileCollsionBoxes();
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
                mainBox.set(2, mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
                mainBox.addOffsetTo(offset).addOffsetTo(here);
                this.setEntityBoundingBox(mainBox.getBoundingBox());
                aabbs = getTileCollsionBoxes();
                diffs.set(mainBox.doTileCollision(world, aabbs, this, Vector3.empty, diffs, false));
                x = diffs.x;
                y = diffs.y;
                z = diffs.z;
            }

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
            boolean border = worldObj.getWorldBorder().contains(getEntityBoundingBox().offset(dir.x, dir.y, dir.z));
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
            IBlockState state = worldObj.getBlockState(blockpos);
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
                    block1.onEntityCollidedWithBlock(this.worldObj, blockpos, state, this);
                }

                this.distanceWalkedModified = (float) (this.distanceWalkedModified
                        + MathHelper.sqrt_double(d15 * d15 + d17 * d17) * 0.6D);
                this.distanceWalkedOnStepModified = (float) (this.distanceWalkedOnStepModified
                        + MathHelper.sqrt_double(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

                if (this.distanceWalkedOnStepModified > this.nextStepDistance && state.getMaterial() != Material.AIR)
                {
                    this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater() && !swims())
                    {
                        float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D
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

        if (worldObj.isRemote)
        {
            showLivingParticleFX();
        }

        for (int i = 0; i < flavourAmounts.length; i++)
        {
            if (flavourAmounts[i] > 0)
            {
                flavourAmounts[i]--;
            }
        }

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
        long time = System.nanoTime();
        here.set(posX, posY, posZ);
        boolean loaded = worldObj.isAreaLoaded(this.getPosition(), 8);
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
        if (getPokedexNb() == 201 && (this.forme == null || this.forme.isEmpty() || this.forme.equals("unown")
                || this.getPokedexEntry() == this.getPokedexEntry().getBaseForme()))
        {
            int num = rand.nextInt(unowns.length);
            changeForme(unowns[num]);
        }
        super.onUpdate();
        double dt = (System.nanoTime() - time) / 10e3D;
        average = ((average * (ticksExisted - 1)) + dt) / ticksExisted;
        double toolong = 500;
        if (PokecubeMod.core.getConfig().debug && dt > toolong && !worldObj.isRemote)
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
        setPokecubeId(nbttagcompound.getInteger("PokeballId"));
        setSize(nbttagcompound.getFloat("scale"));
        uid = nbttagcompound.getInteger("PokemobUID");
        if (nbttagcompound.hasKey("flavours")) flavourAmounts = nbttagcompound.getIntArray("flavours");
        this.initRidable();
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.pokedexNb = data.readInt();
        setSize(data.readFloat());
        pokecubeId = data.readInt();
        this.uid = data.readInt();
        this.initRidable();
        for (int i = 0; i < 4; i++)
            rgba[i] = data.readByte() + 128;
        this.entityUniqueID = new UUID(data.readLong(), data.readLong());

        super.readSpawnData(data);
    }

    @Override
    public void setPokecubeId(int pokeballId)
    {
        pokecubeId = pokeballId;
    }

    @Override
    public void setPokedexEntry(PokedexEntry newEntry)
    {
        super.setPokedexEntry(newEntry);
        setSize(getSize());
    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb)
    {
        super.setEntityBoundingBox(bb);
    }

    @Override
    public void setSize(float size)
    {
        if (isAncient()) scale = 2;
        else scale = size;
        float a = 1, b = 1, c = 1;
        PokedexEntry entry = getPokedexEntry();
        if (entry != null)
        {
            a = entry.width * getSize();
            b = entry.height * getSize();
            c = entry.length * getSize();
            if (a < 0.01 || b < 0.01 || c < 0.01)
            {
                float min = 0.01f / Math.min(a, Math.min(c, b));
                scale *= min;
                a = entry.width * getSize();
                b = entry.height * getSize();
                c = entry.length * getSize();
            }
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

    void showLivingParticleFX()
    {
        if (flavourAmounts.length != 5) flavourAmounts = new int[5];
        Vector3 particleLoc = here.copy();
        if (flavourAmounts[SWEET] > 0)
        {
            particle = "powder.pink";
        }
        if (flavourAmounts[BITTER] > 0)
        {
            particle = "powder.green";
        }
        if (flavourAmounts[SPICY] > 0)
        {
            particle = "powder.red";
        }
        if (flavourAmounts[DRY] > 0)
        {
            particle = "powder.blue";
        }
        if (flavourAmounts[SOUR] > 0)
        {
            particle = "powder.yellow";
        }
        if (isShadow())
        {
            particle = "portal";
            particleIntensity = 100;
        }
        else if (particle == null && getPokedexEntry().particleData != null)
        {
            particle = getPokedexEntry().particleData[0];
            particleIntensity = Integer.parseInt(getPokedexEntry().particleData[1]);
        }

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_MONTH) == 25 && calendar.get(Calendar.MONTH) == 11)
        {
            float scale = width * 2;
            Vector3 offset = Vector3.getNewVector().set(rand.nextDouble() - 0.5, rand.nextDouble() + height / 2,
                    rand.nextDouble() - 0.5);
            offset.scalarMultBy(scale);
            particleLoc.addTo(offset);
            particle = "aurora";// Merry Xmas
            particleIntensity = 90;
        }
        if (particle != null && particleCounter++ >= 100 - particleIntensity)
        {
            PokecubeMod.core.spawnParticle(particle, particleLoc, null);
            particleCounter = 0;
        }
    }

    @Override
    public void specificSpawnInit()
    {
        super.specificSpawnInit();
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("PokeballId", getPokecubeId());
        nbttagcompound.setFloat("scale", (float) (getSize() / PokecubeMod.core.getConfig().scalefactor));
        nbttagcompound.setInteger("PokemobUID", uid);
        nbttagcompound.setIntArray("flavours", flavourAmounts);
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
        data.writeInt(pokedexNb);
        data.writeFloat((float) (getSize() / PokecubeMod.core.getConfig().scalefactor));
        data.writeInt(pokecubeId);
        data.writeInt(uid);
        byte[] rgbaBytes = { (byte) (rgba[0] - 128), (byte) (rgba[1] - 128), (byte) (rgba[2] - 128),
                (byte) (rgba[3] - 128) };
        data.writeBytes(rgbaBytes);
        data.writeLong(getUniqueID().getMostSignificantBits());
        data.writeLong(getUniqueID().getLeastSignificantBits());

        super.writeSpawnData(data);

    }

    @Override
    public World getWorld()
    {
        return worldObj;
    }

    @Override
    public boolean attackEntityFromPart(EntityDragonPart dragonPart, DamageSource source, float damage)
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
}
