/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ReportedException;
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
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
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

/** @author Manchou, Thutmose */
public abstract class EntityPokemobBase extends EntityHungryPokemob implements IEntityMultiPart, TagNames
{
    public static boolean       multibox     = true;

    private int                 despawntimer = 0;

    private EntityPokemobPart[] partsArray;

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
        boolean canDespawn = pokemobCap.getHungerTime() > PokecubeMod.core.getConfig().pokemobLifeSpan;
        boolean checks = pokemobCap.getPokemonAIState(IMoveConstants.TAMED) || pokemobCap.getPokemonOwner() != null
                || pokemobCap.getPokemonAIState(IMoveConstants.ANGRY) || getAttackTarget() != null
                || this.hasCustomName() || isNoDespawnRequired();
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
        if (!this.canDespawn() || this.getEntityWorld().isRemote) return;
        Result result = ForgeEventFactory.canEntityDespawn(this);
        if (result == Result.DENY) return;
        SpawnEvent.Despawn evt = new SpawnEvent.Despawn(here, getEntityWorld(), pokemobCap);
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
        return pokemobCap.getPokedexEntry().getSoundEvent();
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
        float weight = pokemobCap.getBlockPathWeight(getEntityWorld(), here);
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
        ITextComponent textcomponentstring = pokemobCap.getPokemonDisplayName();
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
        return pokemobCap.getPokedexEntry().getName();
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.15F;
    }

    @Override
    public int getVerticalFaceSpeed()
    {
        if (pokemobCap.getPokemonAIState(IMoveConstants.SITTING)) { return 20; }
        return super.getVerticalFaceSpeed();
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        this.getDisplayName();

        if (multibox)
        {
            this.noClip = true;
        }

        isImmuneToFire = pokemobCap.isType(PokeType.getType("fire"));
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
        return pokemobCap.getPokemonAIState(IMoveConstants.INLAVA);
    }

    List<AxisAlignedBB> aabbs = null;

    public List<AxisAlignedBB> getTileCollsionBoxes()
    {
        if (this.getEntityWorld().isRemote && this.isBeingRidden()
                && (this.getServer() == null || this.getServer().isDedicatedServer())
                && pokemobCap.getOwner() == PokecubeCore.proxy.getPlayer((String) null))
        {
            Vector3 vec = Vector3.getNewVector();
            Vector3 vec2 = Vector3.getNewVector();
            double x = pokemobCap.getPokedexEntry().width * pokemobCap.getSize();
            double z = pokemobCap.getPokedexEntry().length * pokemobCap.getSize();
            double y = pokemobCap.getPokedexEntry().height * pokemobCap.getSize();
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
            AxisAlignedBB box1 = box.expand(2 + x, 2 + y, 2 + z);
            box1 = box1.addCoord(motionX, motionY, motionZ);
            aabbs = mainBox.getCollidingBoxes(box1, getEntityWorld(), getEntityWorld());
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

    /** Tries to moves the entity by the passed in displacement. Args: x, y,
     * z */
    @Override
    public void moveEntity(double x, double y, double z)
    {
        if (!this.addedToChunk) return;
        boolean normalSize = this.height > 0.5 && this.width < 1 && this.width > 0.25 && this.length < 1
                && this.length > 0.25;
        if (pokemobCap.mainBox == null) pokemobCap.setSize(pokemobCap.getSize());
        if (!multibox || normalSize || pokemobCap.mainBox == null)
        {
            this.noClip = false;
            super.moveEntity(x, y, z);
            return;
        }
        else if (aabbs != null)
        {
            double x0 = x, y0 = y, z0 = z;
            IBlockAccess world = getEntityWorld();
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
                    v.scalarMultBy(pokemobCap.getSize());
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
                pokemobCap.mainBox.boxMin().clear();
                pokemobCap.mainBox.boxMax().x = pokemobCap.getPokedexEntry().width * pokemobCap.getSize();
                pokemobCap.mainBox.boxMax().z = pokemobCap.getPokedexEntry().length * pokemobCap.getSize();
                pokemobCap.mainBox.boxMax().y = pokemobCap.getPokedexEntry().height * pokemobCap.getSize();
                pokemobCap.offset.set(-pokemobCap.mainBox.boxMax().x / 2, 0, -pokemobCap.mainBox.boxMax().z / 2);
                double ar = pokemobCap.mainBox.boxMax().x / pokemobCap.mainBox.boxMax().z;
                if (ar > 2 || ar < 0.5)
                    pokemobCap.mainBox.set(2, pokemobCap.mainBox.rows[2].set(0, 0, (-rotationYaw) * Math.PI / 180));
                pokemobCap.mainBox.addOffsetTo(pokemobCap.offset).addOffsetTo(here);
                this.setEntityBoundingBox(pokemobCap.mainBox.getBoundingBox());
                getTileCollsionBoxes();
                diffs.set(pokemobCap.mainBox.doTileCollision(world, aabbs, this, Vector3.empty, diffs, false));
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
            double size = Math.max(pokemobCap.getSize() * pokemobCap.getPokedexEntry().length, 3);
            Vector3 dir = Vector3.getNewVector().set(x, newY, z).norm().scalarMult(size);
            boolean border = getEntityWorld().getWorldBorder()
                    .contains(getEntityBoundingBox().offset(dir.x, dir.y, dir.z));
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
            IBlockState state = getEntityWorld().getBlockState(blockpos);
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
                    block1.onEntityCollidedWithBlock(this.getEntityWorld(), blockpos, state, this);
                }

                this.distanceWalkedModified = (float) (this.distanceWalkedModified
                        + MathHelper.sqrt_double(d15 * d15 + d17 * d17) * 0.6D);
                this.distanceWalkedOnStepModified = (float) (this.distanceWalkedOnStepModified
                        + MathHelper.sqrt_double(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

                if (this.distanceWalkedOnStepModified > this.nextStepDistance && state.getMaterial() != Material.AIR)
                {
                    this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater() && !pokemobCap.swims())
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
            PokemobBodies.initBody(pokemobCap);
        }
        if (getParts() != null)
        {
            for (Entity e : getParts())
            {
                e.onUpdate();
            }
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
        if (!Pokedex.getInstance().isRegistered(pokemobCap.getPokedexEntry())) return;

        long time = System.nanoTime();
        here.set(posX, posY, posZ);
        BlockPos pos = new BlockPos(posX, 1, posZ);
        boolean loaded = getEntityWorld().isAreaLoaded(pos, 8);
        if (loaded && !(pokemobCap.getPokemonAIState(IMoveConstants.STAYING)
                || pokemobCap.getPokemonAIState(IMoveConstants.GUARDING)
                || pokemobCap.getPokemonAIState(IMoveConstants.ANGRY) || getAttackTarget() != null))
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
        if (PokecubeMod.debug && dt > toolong && !getEntityWorld().isRemote)
        {
            here.set(here.getPos());
            String toLog = "%3$s took %2$s\u00B5s to tick, it is located at %1$s, the average has been %4$s\u00B5s";
            PokecubeMod.log(String.format(toLog, here, (int) dt,
                    pokemobCap.getPokemonDisplayName().getUnformattedComponentText(), ((int) (average * 100)) / 100d));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey(POKEMOBTAG))
        {
            NBTTagCompound pokemobTag = nbttagcompound.getCompoundTag(POKEMOBTAG);
            pokemobCap.readPokemobData(pokemobTag);
            // readPokemobData(pokemobTag);
            return;
        }
        GeneticsManager.handleLoad(pokemobCap);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
    {
        GeneticsManager.handleEpigenetics(pokemobCap);
        return super.writeToNBT(nbttagcompound);
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        PokecubeSerializer.getInstance().addPokemob(pokemobCap);
        super.writeSpawnData(data);
    }

    @Override
    public World getWorld()
    {
        return getEntityWorld();
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
    public void addEntityCrashInfo(CrashReportCategory category)
    {
        super.addEntityCrashInfo(category);
        category.addCrashSection("World:", getEntityWorld() == null ? "NULL" : getEntityWorld().toString());
        category.addCrashSection("Owner:",
                pokemobCap.getPokemonOwnerID() == null ? "NULL" : pokemobCap.getPokemonOwnerID().toString());
        Thread.dumpStack();
    }
}
