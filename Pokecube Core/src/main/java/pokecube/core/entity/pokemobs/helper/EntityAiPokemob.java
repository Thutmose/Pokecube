/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.ai.pokemob.PokemobAIDodge;
import pokecube.core.ai.pokemob.PokemobAIFollowOwner;
import pokecube.core.ai.pokemob.PokemobAIHurt;
import pokecube.core.ai.pokemob.PokemobAILeapAtTarget;
import pokecube.core.ai.pokemob.PokemobAILook;
import pokecube.core.ai.pokemob.PokemobAISwimming;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.ai.thread.aiRunnables.AIFindTarget;
import pokecube.core.ai.thread.aiRunnables.AIGatherStuff;
import pokecube.core.ai.thread.aiRunnables.AIHungry;
import pokecube.core.ai.thread.aiRunnables.AIIdle;
import pokecube.core.ai.thread.aiRunnables.AIMate;
import pokecube.core.ai.thread.logicRunnables.LogicInLiquid;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.ai.utils.AISaveHandler.PokemobAI;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod.Type;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou */
public abstract class EntityAiPokemob extends EntityMountablePokemob
{

    public GuardAI guardAI;

    private int lastHadTargetTime = 0;

    private PokeNavigator     navi;
    private PokemobMoveHelper mover;
    boolean                   initAI  = true;
    boolean                   popped  = false;
    private PokemobAI         aiObject;
    boolean                   isAFish = false;

    public TerrainSegment currentTerrain = null;

    public EntityAiPokemob(World world)
    {
        super(world);
        here = Vector3.getNewVectorFromPool();
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        // if(isAIEnabled())
        // initAI(getPokedexEntry());
    }

    ///////////////////////////////////////// Init
    ///////////////////////////////////////// things///////////////////////////////

    protected void initAI(PokedexEntry entry)
    {
        initAI = false;
        navi = new PokeNavigator(this, worldObj);
        mover = new PokemobMoveHelper(this);

        float moveSpeed = 0.5f;
        float speedFactor = (float) (1 + Math.sqrt(entry.getStatVIT()) / (100F));
        moveSpeed *= speedFactor;

        if (entry.flys()) moveSpeed /= 1.25f;

        this.getNavigator().setSpeed(moveSpeed);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(moveSpeed);

        this.tasks.addTask(1, new PokemobAISwimming(this));
        this.tasks.addTask(1, new PokemobAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(1, new PokemobAIDodge(this));
        this.tasks.addTask(4, this.aiSit);

        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(5, guardAI);
        this.tasks.addTask(5, new PokemobAIUtilityMove(this));

        if (!entry.isStationary) this.tasks.addTask(6, new PokemobAIFollowOwner(this, 8.0F, 4.0F));
        this.tasks.addTask(8, new PokemobAILook(this, EntityPlayer.class, 8.0F, 1f));

        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new PokemobAIHurt(this, entry.isSocial));

        for (int xy = 0; xy < entry.species.length; xy++)
        {

            if (entry.species[xy].equalsIgnoreCase("FISH") || entry.species[xy].equalsIgnoreCase("JELLYFISH")
                    || entry.species[xy].equalsIgnoreCase("WHALE") || entry.species[xy].equalsIgnoreCase("echinoderm")
                    || entry.species[xy].equalsIgnoreCase("gastropoda"))
            {
                isAFish = true;
                break;
            }

        }
        if (worldObj.isRemote) return;
        AIMate mate;
        PokemobAIThread.addAI(this, new AIAttack(this).setPriority(200));
        PokemobAIThread.addAI(this, (mate = new AIMate(this)).setPriority(300));
        males = mate.males;
        PokemobAIThread.addAI(this, new AIHungry(this, new EntityItem(worldObj), 16).setPriority(300));
        PokemobAIThread.addAI(this, new AIGatherStuff(this, 32).setPriority(400));
        PokemobAIThread.addAI(this, new AIIdle(this).setPriority(500));
        PokemobAIThread.addAI(this, new AIFindTarget(this).setPriority(400));

        PokemobAIThread.addLogic(this, new LogicInLiquid(this));

    }

    ///////////////////////////////////// AI
    ///////////////////////////////////// States///////////////////////////////

    @Override
    public boolean getPokemonAIState(int state)
    {

        if (state == SADDLED)
        {
            handleArmourAndSaddle();
        }

        return (dataWatcher.getWatchableObjectInt(AIACTIONSTATESDW) & state) != 0;
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        int byte0 = dataWatcher.getWatchableObjectInt(AIACTIONSTATESDW);

        if (state == STAYING)
        {
            here.set(this);
            setHome(here.intX(), here.intY(), here.intZ(), 16);
        }

        if (flag)
        {
            dataWatcher.updateObject(AIACTIONSTATESDW, Integer.valueOf((byte0 | state)));
        }
        else
        {
            dataWatcher.updateObject(AIACTIONSTATESDW, Integer.valueOf((byte0 & -state - 1)));
        }
        if ((state & SITTING) > 0)
        {
            aiSit.setSitting(flag);
            super.setSitting(flag);
        }
    }

    ////////////////// Things which happen every tick///////////////////////////

    @Override
    public void onUpdate()
    {
        if (initAI)
        {
            initAI(getPokedexEntry());
        }
        if (popped && traded)
        {
            evolve(true);
            popped = false;
        }
        if (getPokedexEntry().floats() || getPokedexEntry().flys()) fallDistance = 0;
        dimension = worldObj.provider.getDimensionId();
        super.onUpdate();

        if (worldObj.isRemote)
        {
            int id = dataWatcher.getWatchableObjectInt(ATTACKTARGETIDDW);
            if (id >= 0 && getAttackTarget() == null)
            {
                setAttackTarget((EntityLivingBase) worldObj.getEntityByID(id));
            }
            if (id < 0 && getAttackTarget() != null)
            {
                setAttackTarget(null);
            }
        }
        String s;
        int state = dataWatcher.getWatchableObjectInt(AIACTIONSTATESDW);
        if (getAIState(TAMED, state) && ((s = getPokemonOwnerName()) == null || s.isEmpty()))
        {
            setPokemonAIState(TAMED, false);
        }

        if (inLove > 600)
        {
            resetLoveStatus();
        }

        PokedexEntry entry = getPokedexEntry();

        if (getAIState(HELD, state) && ridingEntity == null)
        {
            setPokemonAIState(HELD, false);
        }
        if (getAIState(SHOULDER, state) && ridingEntity == null)
        {
            setPokemonAIState(SHOULDER, false);
        }
        if (getAIState(GUARDING, state) && getAIState(SITTING, state))
        {
            setPokemonAIState(SITTING, false);
        }
        if (ticksExisted > EXITCUBEDURATION && getAIState(EXITINGCUBE, state))
        {
            setPokemonAIState(EXITINGCUBE, false);
        }
        boolean canFloat = entry.mobType == Type.FLOATING;

        if (canFloat && !getAIState(INWATER, state))
        {
            float floatHeight = (float) entry.preferedHeight;
            if (here == null)
            {
                here = Vector3.getNewVectorFromPool();
                here.set(this);
            }
            Vector3 down = Vector3.getNextSurfacePoint(worldObj, here.set(this), Vector3.secondAxisNeg, floatHeight);
            if (down != null) here.set(down);

            Block b;
            if (!(b = here.getBlock(worldObj)).isReplaceable(worldObj, here.getPos()) // &&
                                                                                      // getAttackTarget()
                                                                                      // ==
                                                                                      // null
                    && !getAIState(SLEEPING, state) || b.getMaterial().isLiquid())
            {
                motionY += 0.01;
            }
            else motionY -= 0.01;
            if (down == null || getPokemonAIState(SITTING))
            {
                motionY -= 0.02;
            }
        }
        canFloat = entry.mobType == Type.FLYING;
        if (canFloat && here.getBlock(worldObj, EnumFacing.DOWN).getMaterial().isLiquid())
        {
            if (motionY < -0.1) motionY = 0;
            motionY += 0.05;
        }
        if (this.getPokemonAIState(IPokemob.SITTING) && !this.getNavigator().noPath())
        {
            this.getNavigator().clearPathEntity();
        }
        TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(this);
        if (!t.equals(currentTerrain))
        {
            if (currentTerrain != null)
            {
                PokemobTerrainEffects effect = (PokemobTerrainEffects) currentTerrain.geTerrainEffect("pokemobEffects");
                if (effect == null)
                {
                    currentTerrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
                }
                effect.removePokemon(this);
            }
            currentTerrain = t;
            PokemobTerrainEffects effect = (PokemobTerrainEffects) currentTerrain.geTerrainEffect("pokemobEffects");
            if (effect == null)
            {
                currentTerrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
            }
            effect.addPokemon(this);
            effect.doEntryEffect(this);
        }

        if (egg != null && egg.isDead)
        {
            egg = null;
        }

        field_25054_c = field_25048_b;

        if (looksWithInterest)
        {
            field_25048_b = field_25048_b + (1.0F - field_25048_b) * 0.4F;
        }
        else
        {
            field_25048_b = field_25048_b + (0.0F - field_25048_b) * 0.4F;
        }

        if (looksWithInterest)
        {

        }

        if (isWet() && !(this.canSurf))
        {
            isPokemonShaking = true;
            field_25052_g = false;
            timePokemonIsShaking = 0.0F;
            prevTimePokemonIsShaking = 0.0F;
        }
        else if ((isPokemonShaking || field_25052_g) && field_25052_g)
        {
            prevTimePokemonIsShaking = timePokemonIsShaking;
            timePokemonIsShaking += 0.05F;

            if (prevTimePokemonIsShaking >= 2.0F)
            {
                isPokemonShaking = false;
                field_25052_g = false;
                prevTimePokemonIsShaking = 0.0F;
                timePokemonIsShaking = 0.0F;
            }

            if (timePokemonIsShaking > 0.4F && !swims())
            {
                float f = (float) posY;
                int i = (int) (MathHelper.sin((timePokemonIsShaking - 0.4F) * (float) Math.PI) * 7F);

                for (int j = 0; j < i; j++)
                {
                    float f1 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    float f2 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + f1, f + 0.8F, posZ + f2, motionX,
                            motionY, motionZ);
                }
            }
        }
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.isOnLadder())
        {
            onGround = true;
        }
        super.onLivingUpdate();

        if (ticksExisted % 20 == 0)
        {
            this.isShearable(null, worldObj, here.getPos());
        }

        if (getPokemonAIState(IPokemob.ANGRY) && getAttackTarget() == null)
        {
            this.setPokemonAIState(ANGRY, false);
        }

        if (!getPokemonAIState(IPokemob.ANGRY))
        {
            lastHadTargetTime--;
            if (lastHadTargetTime == 0)
            {
                this.setModifiers(PokecubeSerializer.intAsModifierArray(1717986918));
            }
        }
        if (getPokedexEntry().hatedMaterial != null)
        {
            String material = getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("light"))
            {
                float value = 0.5f;
                if (worldObj.isDaytime() && !worldObj.isRemote && getPokemonAIState(TAMED) == false)
                {

                    value = Float.parseFloat(getPokedexEntry().hatedMaterial[1]);
                    String action = getPokedexEntry().hatedMaterial[2];
                    float f = getBrightness(1.0F);
                    if (f > value && worldObj.canSeeSky(getPosition()))
                    {
                        if (action.equalsIgnoreCase("despawn"))
                        {
                            this.setDead();
                        }
                        else if (action.equalsIgnoreCase("hurt") && Math.random() < 0.1)
                        {
                            damageEntity(DamageSource.onFire, 1);
                        }
                    }
                }
            }
            else if (material.equalsIgnoreCase("water"))
            {
                if (isInWater() && rand.nextInt(10) == 0)
                {
                    damageEntity(DamageSource.cactus, 1);
                }
            }
        }

        if (getPokemonAIState(MATING))
        {
            if (ticksExisted % 5 == 0)
            {
                double d = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle(EnumParticleTypes.HEART, (posX + rand.nextFloat() * width * 2.0F) - width,
                        posY + 0.5D + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d,
                        d1, d2);
            }
        }

        if (isServerWorld() && isPokemonShaking && !field_25052_g && !hasPath() && onGround)
        {
            field_25052_g = true;
            timePokemonIsShaking = 0.0F;
            prevTimePokemonIsShaking = 0.0F;
            worldObj.setEntityState(this, (byte) 8);
        }
    }

    float moveF;

    @Override
    protected void updateEntityActionState()
    {
        ++this.entityAge;
        navi.refreshCache();
        this.worldObj.theProfiler.startSection("checkDespawn");
        this.despawnEntity();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("sensing");
        this.senses.clearSensingCache();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("targetSelector");
        this.targetTasks.onUpdateTasks();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("goalSelector");
        this.tasks.onUpdateTasks();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("navigation");
        this.getNavigator().onUpdateNavigation();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("mob tick");
        this.updateAITasks();
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("controls");
        this.worldObj.theProfiler.startSection("move");
        this.getMoveHelper().onUpdateMoveHelper();
        this.worldObj.theProfiler.endStartSection("look");
        this.getLookHelper().onUpdateLook();
        this.worldObj.theProfiler.endStartSection("jump");
        this.getJumpHelper().doJump();
        if (getPokemonAIState(JUMPING))
        {
            jump();
        }
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.endSection();
    }

    @Override
    protected void updateAITasks()
    {
        super.updateAITasks();
    }

    @Override
    /** main AI tick function, replaces updateEntityActionState */
    protected void updateAITick()
    {
        super.updateAITick();
    }

    /////////////////////// Target related
    /////////////////////// things//////////////////////////////////

    @Override
    public void setAttackTarget(EntityLivingBase entity)
    {
        if (entity == null)
        {
            lastHadTargetTime = 100;
        }
        if (entity != null && entity.equals(this.getPokemonOwner())) { return; }
        if (entity != null && entity.equals(this)) { return; }
        if (entity != null) setPokemonAIState(SITTING, false);
        if (entity != null && !worldObj.isRemote)
        {
            dataWatcher.updateObject(ATTACKTARGETIDDW, Integer.valueOf(entity.getEntityId()));
        }
        if (entity == null && !worldObj.isRemote)
        {
            dataWatcher.updateObject(ATTACKTARGETIDDW, Integer.valueOf(-1));
        }
        if (entity != getAttackTarget() && getMoveStats().ability != null)
        {
            getMoveStats().ability.onAgress(this, entity);
        }
        super.setAttackTarget(entity);
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        return super.attackEntityAsMob(par1Entity);
    }

    /////////////////// Movement related things///////////////////////////

    @Override
    public void fall(float f, float f1)
    {
        PokedexEntry entry = getPokedexEntry();
        boolean canFloat = entry.mobType == Type.FLOATING || entry.mobType == Type.FLYING;
        if (!canFloat) super.fall(f, f1);
    }

    @Override
    public PathNavigate getNavigator()
    {
        if (navi != null) return navi;

        return super.getNavigator();
    }

    @Override
    public EntityMoveHelper getMoveHelper()
    {
        if (mover != null) return mover;
        return super.getMoveHelper();
    }

    /** Checks if this entity is inside water (if inWater field is true as a
     * result of handleWaterMovement() returning true) */
    public boolean isInWater()
    {
        return getPokemonAIState(INWATER);
    }

    /*
     * Override to fix bad detection of isInWater for little mobs and to skip
     * the handle of water movement on water mobs
     */
    @Override
    public boolean handleWaterMovement()
    {
        if (isInWater()) // (this.worldObj.handleMaterialAcceleration(this.boundingBox.contract(0.001D,
                         // 0.001D, 0.001D), Material.water, this))
        {
            // original code with the bounding box expand make the smaller mobs
            // can't swim!
            // if (this.worldObj.handleMaterialAcceleration(this.worldObj,
            // this.boundingBox.expand(0.0D, -0.4000000059604645D,
            // 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this))
            // {
            if (!this.inWater) // && !this.firstUpdate)
            {
                if (!swims())
                {
                    float f = MathHelper
                            .sqrt_double(this.motionX * this.motionX * 0.20000000298023224D
                                    + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D)
                            * 0.2F;

                    if (f > 1.0F)
                    {
                        f = 1.0F;
                    }

                    this.playSound("game.neutral.swim", f,
                            1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    float f1 = MathHelper.floor_double(this.getEntityBoundingBox().minY);
                    int i;
                    float f2;
                    float f3;

                    for (i = 0; i < 1.0F + this.width * 20.0F; ++i)
                    {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + f2, f1 + 1.0F,
                                this.posZ + f3, this.motionX, this.motionY - this.rand.nextFloat() * 0.2F,
                                this.motionZ);
                    }

                    for (i = 0; i < 1.0F + this.width * 20.0F; ++i)
                    {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + f2, f1 + 1.0F,
                                this.posZ + f3, this.motionX, this.motionY, this.motionZ);
                    }
                }
            }

            this.fallDistance = 0.0F;
            this.inWater = true;
            extinguish();
        }
        else
        {
            this.inWater = false;
        }

        return isInWater();
    }

    @Override
    /** Moves the entity based on the specified heading. Args: strafe,
     * forward */
    public void moveEntityWithHeading(float f, float f1)
    {
        // new Exception().printStackTrace();
        double d0;
        if (this.riddenByEntity != null) // &&
                                         // !getPokemonAIState(EXECUTINGMOVE))
        {
            super.moveEntityWithHeading(f, f1);
            return;
        }
        if (isServerWorld())
        {
            PokedexEntry entry = getPokedexEntry();
            int aiState = dataWatcher.getWatchableObjectInt(AIACTIONSTATESDW);
            boolean isAbleToFly = entry.mobType == Type.FLOATING || entry.mobType == Type.FLYING;
            boolean isWaterMob = entry.mobType == Type.WATER;
            boolean shouldGoDown = false;
            boolean shouldGoUp = false;
            PathPoint p = null;
            if (!getNavigator().noPath() && !getNavigator().getPath().isFinished())
            {
                p = getNavigator().getPath().getPathPointFromIndex(getNavigator().getPath().getCurrentPathIndex());
                if (getNavigator().getPath().getCurrentPathIndex() < getNavigator().getPath().getCurrentPathLength()
                        - 1)
                {
                }
                shouldGoDown = p.yCoord < posY - stepHeight;
                shouldGoUp = p.yCoord > posY + stepHeight;
                if (isAbleToFly)
                {
                    shouldGoUp = p.yCoord > posY - stepHeight;
                    shouldGoDown = !shouldGoUp;
                }
            }
            if (!(shouldGoDown || shouldGoUp) && entry.floats())
            {
                setDirectionPitch(0);
            }
            if (!(shouldGoDown || shouldGoUp) && entry.flys())
            {
                setDirectionPitch(0);
            }
            if (!(shouldGoDown || shouldGoUp) && entry.swims())
            {
                setDirectionPitch(0);
            }
            if ((getAIState(SLEEPING, aiState) || getStatus() == STATUS_SLP || getStatus() == STATUS_FRZ)
                    && isAbleToFly)
                shouldGoDown = true;

            if (this.isInWater())
            {
                d0 = this.posY;
                float f2 = 0.1F;
                float f6 = swims() ? 2.5f : 1;
                float f4;
                float f3 = f2 * f6;

                f4 = Math.min(f1 * f3, f1);

                this.moveFlying(f, f1, f4);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.800000011920929D;
                this.motionY *= 0.800000011920929D;
                this.motionZ *= 0.800000011920929D;

                if (!isWaterMob)
                {
                    this.motionY -= 0.02D;
                }
                if (!isWaterMob && this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
                        this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
                {
                    this.motionY = 0.30000001192092896D;
                }
            }
            else if (this.isInLava())
            {
                d0 = this.posY;
                this.moveFlying(f, f1, 0.02F);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.5D;
                this.motionY *= 0.5D;
                this.motionZ *= 0.5D;
                this.motionY -= 0.02D;

                if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
                        this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
                {
                    this.motionY = 0.30000001192092896D;
                }
            }
            else
            {
                float f2 = 0.91F;
                float f6 = isAFish ? 0.15f : 1;

                Block b = this.worldObj.getBlockState(getPosition().down()).getBlock();
                if (this.onGround)
                {
                    f2 = b.slipperiness * 0.91F;
                }
                else if (isAbleToFly)
                {
                    f2 = 0.35f;
                }

                float f3 = 0.16277136F * f6 / (f2 * f2 * f2);
                float f4;

                if (this.onGround || isAbleToFly)
                {
                    f4 = Math.min(this.getAIMoveSpeed() * f3, getAIMoveSpeed());
                    if (!onGround)
                    {
                        f4 *= 4;
                    }
                }
                else
                {
                    f4 = this.jumpMovementFactor;
                }
                this.moveFlying(f, f1, f4);
                f2 = 0.91F;

                if (this.onGround)
                {
                    f2 = b.slipperiness * 0.91F;
                }

                if (this.isOnLadder())
                {
                    float f5 = 0.05F;
                    this.onGround = true;
                    if (this.motionX < (-f5))
                    {
                        this.motionX = (-f5);
                    }

                    if (this.motionX > f5)
                    {
                        this.motionX = f5;
                    }

                    if (this.motionZ < (-f5))
                    {
                        this.motionZ = (-f5);
                    }

                    if (this.motionZ > f5)
                    {
                        this.motionZ = f5;
                    }

                    this.fallDistance = 0.0F;

                    if (this.motionY < -0.05D)
                    {
                        this.motionY = -0.05D;
                    }

                }
                this.moveEntity(this.motionX, this.motionY, this.motionZ);

                if (this.isCollidedHorizontally && this.isOnLadder())
                {
                    this.motionY = 0.2D;
                }

                if (this.worldObj.isRemote && (!this.worldObj.isAreaLoaded(getPosition(), 10)
                        || !this.worldObj.getChunkFromBlockCoords(getPosition()).isLoaded()))
                {
                    if (this.posY > 0.0D)
                    {
                        this.motionY = -0.1D;
                    }
                    else
                    {
                        this.motionY = 0.0D;
                    }
                }
                else if (!isAbleToFly || this.getAIState(SITTING, aiState) || this.getAIState(SLEEPING, aiState))
                {
                    this.motionY -= 0.08D;
                }
                else if (!(shouldGoUp || shouldGoDown))
                {
                }

                else
                {
                    this.motionY *= 0.1;
                }

                if (isAbleToFly)
                {
                    this.motionY *= f2;
                    f2 *= 0.75;
                }
                else
                {
                    this.motionY *= 0.9100000190734863D;
                }
                this.motionX *= f2;
                this.motionZ *= f2;
            }
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d2 = this.posX - this.prevPosX;
        double d3 = this.posZ - this.prevPosZ;
        float f7 = MathHelper.sqrt_double(d2 * d2 + d3 * d3) * 4.0F;

        if (f7 > 1.0F)
        {
            f7 = 1.0F;
        }

        this.limbSwingAmount += (f7 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    /** Used in both water and by flying objects */
    public void moveFlying(float strafe, float forward, float speed)
    {
        float f3 = strafe * strafe + forward * forward;
        if (f3 >= 0F)
        {
            f3 = MathHelper.sqrt_float(f3);

            if (f3 < 1.0F)
            {
                f3 = 1.0F;
            }
            f3 = speed / f3;
            strafe *= f3;
            forward *= f3;
            float f4 = MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F)
                    * MathHelper.cos(this.rotationPitch * (float) Math.PI / 180.0F);
            float f5 = MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F)
                    * MathHelper.cos(this.rotationPitch * (float) Math.PI / 180.0F);
            float f6 = -MathHelper.sin(getDirectionPitch() * (float) Math.PI / 180.0F);
            this.motionX += strafe * f5 - forward * f4;
            this.motionZ += forward * f5 + strafe * f4;
            this.motionY += (f6 * getMovementSpeed());
        }
    }

    public double getMovementSpeed()
    {
        return this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }
    ////////////////////////// Death Related things////////////////////////

    @Override
    public void setDead()
    {
        PokecubeSerializer.getInstance().removePokemob(this);
        PokemobAIThread.removeEntity(this);
        if (getMoveStats().ability != null)
        {
            getMoveStats().ability.destroy();
        }
        if (currentTerrain != null)
        {
            PokemobTerrainEffects effect = (PokemobTerrainEffects) currentTerrain.geTerrainEffect("pokemobEffects");
            if (effect == null)
            {
                currentTerrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
            }
            effect.removePokemon(this);
        }
        if (getHome() != null && getHome().getY() > 0 && worldObj.isAreaLoaded(getHome(), 2))
        {
            TileEntity te = worldObj.getTileEntity(getHome());
            if (te != null && te instanceof TileEntityNest)
            {
                TileEntityNest nest = (TileEntityNest) te;
                nest.removeResident(this);
            }
        }
        if (!getPokemonAIState(TAMED))
        {
            AISaveHandler.instance().removeAI(this);
        }
        super.setDead();
    }

    @Override
    protected void onDeathUpdate()
    {
        if (!mod_Pokecube.isOnClientSide() && getPokemonAIState(TAMED))
        {
            HappinessType.applyHappiness(this, HappinessType.FAINT);
            String mess = StatCollector.translateToLocalFormatted("pokemob.action.faint", getPokemonDisplayName());
            displayMessageToOwner("\u00a7c" + mess);
            returnToPokecube();
        }
        if (!getPokemonAIState(TAMED))
        {
            AISaveHandler.instance().removeAI(this);
            if (this.getHeldItem() != null) PokecubeItems.deValidate(getHeldItem());
        }
        super.onDeathUpdate();
    }

    @Override
    public void onDeath(DamageSource damageSource)
    {

        if (ForgeHooks.onLivingDeath(this, damageSource)) return;
        Entity entity = damageSource.getEntity();
        EntityLivingBase entitylivingbase = this.func_94060_bK();

        if (this.scoreValue >= 0 && entitylivingbase != null)
        {
            entitylivingbase.addToPlayerScore(this, this.scoreValue);
        }

        if (entity != null)
        {
            entity.onKillEntity(this);
        }

        this.dead = true;
        this.getCombatTracker().reset();

        if (!this.worldObj.isRemote)
        {
            int i = 0;

            if (entity instanceof EntityPlayer)
            {
                i = EnchantmentHelper.getLootingModifier((EntityLivingBase) entity);
            }

            captureDrops = true;
            capturedDrops.clear();

            boolean shadowDrop = (this.isShadow() && this.getLevel() < 40);

            if (this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot") && !shadowDrop)
            {
                this.dropFewItems(this.recentlyHit > 0, i);
                this.dropEquipment(this.recentlyHit > 0, i);

                if (this.recentlyHit > 0 && this.rand.nextFloat() < 0.025F + (float) i * 0.01F)
                {
                    this.addRandomDrop();
                }
            }

            captureDrops = false;
            if (this.isAncient())
            {

                ItemStack eggItemStack = ItemPokemobEgg.getEggStack(pokedexNb);
                Entity eggItem = new EntityPokemobEgg(worldObj, posX, posY, posZ, eggItemStack, this);
                EggEvent.Lay event = new EggEvent.Lay(eggItem);
                MinecraftForge.EVENT_BUS.post(event);

                if (!event.isCanceled())
                {
                    egg = eggItem;
                    worldObj.spawnEntityInWorld(egg);
                }
            }
            if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(this, damageSource, capturedDrops, i,
                    recentlyHit > 0))
            {
                for (EntityItem item : capturedDrops)
                {
                    worldObj.spawnEntityInWorld(item);
                }
            }
        }

        this.worldObj.setEntityState(this, (byte) 3);
    }

    /////////////////////////// Interaction
    /////////////////////////// logic/////////////////////////////////////////////////////

    @Override
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();
        ItemStack key = new ItemStack(Items.shears);
        // Check shearable interaction.
        if (getPokedexEntry().interact(key) && player.getHeldItem() != null
                && player.getHeldItem().isItemEqual(key)) { return false; }

        // Check Pokedex Entry defined Interaction for player.
        if (getPokedexEntry().interact(player, this, true)) return true;
        Item torch = Item.getItemFromBlock(Blocks.torch);
        // Either push pokemob around, or if sneaking, make it try to climb on
        // shoulder
        if (player == getPokemonOwner() && itemstack != null
                && (itemstack.getItem() == Items.stick || itemstack.getItem() == torch))
        {
            if (player.isSneaking())
            {
                if (getPokedexEntry().canSitShoulder)
                {
                    this.setPokemonAIState(SHOULDER, true);
                    this.setPokemonAIState(SITTING, true);
                    mountEntity(player);
                }
                return true;
            }
            Vector3 look = Vector3.getNewVectorFromPool().set(player.getLookVec()).scalarMultBy(0.1);
            this.motionX += look.x;
            this.motionY += look.y;
            this.motionZ += look.z;
            look.freeVectorFromPool();
            return false;
        }
        // Debug thing to maximize happiness
        if (player == getPokemonOwner() && itemstack != null && itemstack.getItem() == Items.apple)
        {
            if (player.capabilities.isCreativeMode && player.isSneaking())
            {
                this.addHappiness(255);
            }
        }
        // Debug thing to increase hunger time
        if (player == getPokemonOwner() && itemstack != null && itemstack.getItem() == Items.golden_hoe)
        {
            if (player.capabilities.isCreativeMode && player.isSneaking())
            {
                this.setHungerTime(this.getHungerTime() + 1000);
            }
        }

        // is Dyeable
        if (player.getHeldItem() != null && getPokedexEntry().hasSpecialTextures[4])
        {
            if (player.getHeldItem().getItem() == Items.dye)
            {
                setSpecialInfo(player.getHeldItem().getItemDamage());
                System.out.println(getSpecialInfo());
                player.getHeldItem().stackSize--;
                return true;
            }
            else if (player.getHeldItem().getItem() == Items.shears) { return false; }
        }

        // Check saddle for riding.
        if (getPokemonAIState(SADDLED) && !player.isSneaking() && player == getPokemonOwner()
                && (itemstack == null || itemstack.getItem() != PokecubeItems.pokedex))
        {
            if (!handleHmAndSaddle(player, new ItemStack(Items.saddle)))
            {
                this.setJumping(false);
                return false;
            }
            else
            {
                return true;
            }
        }

        // Attempt to pick the pokemob up.
        if (!worldObj.isRemote && !getPokemonAIState(SADDLED) && player.isSneaking() && player.getHeldItem() == null
                && getWeight() < 40)
        {

            boolean held = getPokemonAIState(HELD);

            if ((!getPokemonAIState(TAMED) || getHeldItem() == null) && !getPokemonAIState(GUARDING))
            {
                if (!held)
                {
                    mountEntity(player);
                    setPokemonAIState(HELD, true);
                    setPokemonAIState(SITTING, false);
                }
                else
                {
                    mountEntity(null);
                    setPokemonAIState(HELD, false);

                    if (player != getPokemonOwner())
                    {
                        setPokemonAIState(ANGRY, true);
                        setAttackTarget(player);
                    }
                }
                return true;
            }

        }
        if (getPokemonAIState(HELD)) return false;

        // Open Pokedex Gui
        if (itemstack != null && itemstack.getItem() == PokecubeItems.pokedex)
        {
            if (mod_Pokecube.isOnClientSide() && !player.isSneaking())
            {
                player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUIPOKEDEX_ID, worldObj, (int) posX,
                        (int) posY, (int) posZ);
            }
            return true;
        }

        // Owner only interactions.
        if (getPokemonAIState(TAMED) && player == getOwner() && !mod_Pokecube.isOnClientSide())
        {
            if (itemstack != null)
            {
                // Check if it should evolve from item, do so if yes.
                if (PokecubeItems.isValidEvoItem(itemstack) && canEvolve(itemstack))
                {
                    IPokemob evolution = evolve(true, itemstack);

                    if (evolution != null)
                    {
                        itemstack.stackSize--;

                        if (itemstack.stackSize <= 0)
                        {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }
                    }

                    return true;
                }
                // Check if it is stew for happiness test. Report if yes.
                else if (itemstack.isItemEqual(new ItemStack(Items.mushroom_stew)))
                {
                    int happiness = getHappiness();
                    String message = "";
                    if (happiness == 0)
                    {
                        message = "Your pokemon is very unhappy";
                    }
                    if (happiness > 0)
                    {
                        message = "Your pokemon is unhappy";
                    }
                    if (happiness > 49)
                    {
                        message = "Your pokemon is feeling fine";
                    }
                    if (happiness > 99)
                    {
                        message = "Your pokemon is content";
                    }
                    if (happiness > 149)
                    {
                        message = "Your pokemon is happy";
                    }
                    if (happiness > 199)
                    {
                        message = "Your pokemon is very happy";
                    }
                    if (happiness > 254)
                    {
                        message = "Your pokemon is ecstatic";
                    }

                    player.addChatMessage(new ChatComponentText(message));

                }
                // Check if gold apple for breeding.
                if (itemstack.getItem() == Items.golden_apple)
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        --itemstack.stackSize;

                        if (itemstack.stackSize <= 0)
                        {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
                        }
                    }
                    this.inLove = 600;
                    this.setAttackTarget(null);
                    this.worldObj.setEntityState(this, (byte) 18);
                    return true;
                }
                // Otherwise check if useable item.
                if (itemstack.getItem() instanceof IPokemobUseable)
                {
                    boolean used = ((IPokemobUseable) itemstack.getItem()).itemUse(itemstack, this, player);

                    if (used)
                    {
                        itemstack.splitStack(1);
                        return true;
                    }
                }
                // Try to hold the item.
                if (canBeHeld(itemstack))
                {
                    ItemStack heldItem = getHeldItem();

                    if (heldItem != null)
                    {
                        dropItem();
                    }

                    setHeldItem(new ItemStack(itemstack.getItem(), 1, itemstack.getItemDamage()));
                    itemstack.stackSize--;

                    if (itemstack.stackSize <= 0)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                    return true;
                }
            }
            // Open Gui
            if (!mod_Pokecube.isOnClientSide() && getPokemonOwner() == player && itemstack == null)
            {
                openGUI(player);
                return true;
            }
        }

        return false;
    }

    //////////////// Jumping related//////////////////////////

    @Override
    public void jump()
    {
        // if(true)
        // {
        // super.jump();
        // return;
        // }

        if (worldObj.isRemote) return;

        if (!this.isInWater() && !this.isInLava())
        {
            if (!this.onGround) return;
            // The extra factor fixes tiny pokemon being unable to jump up one
            // block.
            this.motionY += 0.41999998688697815D + 0.1 * 1 / getPokedexEntry().height;

            if (this.isPotionActive(Potion.jump))
            {
                this.motionY += (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
            }
            if (riddenByEntity != null)
            {
                motionY += 0.3;
            }

            if (this.isSprinting())
            {
                float f = this.rotationYaw * 0.017453292F;
                this.motionX -= MathHelper.sin(f) * 0.2F;
                this.motionZ += MathHelper.cos(f) * 0.2F;
            }

            this.isAirBorne = true;
            ForgeHooks.onLivingJump(this);
        }
        else
        {
            this.motionY += 0.03999999910593033D;
        }
    }

    public void setJumping(boolean jump)
    {
        if (!worldObj.isRemote)
        {
            setPokemonAIState(JUMPING, jump);
            // super.setJumping(p_70637_1_);
        }
        else
        {
            isJumping = getPokemonAIState(JUMPING);
        }
    }

    ////////////////////////////// Misc////////////////////////////////////////////////////////////////
    @Override
    /** Get number of ticks, at least during which the living entity will be
     * silent. */
    public int getTalkInterval()
    {
        return 400;
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    @Override
    public boolean getCanSpawnHere()
    {
        return this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox());
    }

    /** @return the aiObject */
    public PokemobAI getAiObject()
    {
        if (aiObject == null)
        {
            aiObject = AISaveHandler.instance().getAI(this);
        }
        return aiObject;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return (getType1() == PokeType.water || getType2() == PokeType.water);
    }

    @Override
    public void popFromPokecube()
    {
        super.popFromPokecube();
        popped = true;
    }

    @Override
    public void setMoveForward(float forward)
    {
        this.moveForward = forward;
        moveF = forward;
    }

    @Override
    public void setDirectionPitch(float pitch)
    {
        dataWatcher.updateObject(DIRECTIONPITCHDW, pitch);
    }

    @Override
    public float getDirectionPitch()
    {
        return dataWatcher.getWatchableObjectFloat(DIRECTIONPITCHDW);
    }

    @Override
    /** Called when a user uses the creative pick block button on this entity.
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    public ItemStack getPickedResult(MovingObjectPosition target)
    {
        return ItemPokemobEgg.getEggStack(this);
    }

    @Override
    /** Checks if the entity is in range to render by using the past in distance
     * and comparing it to its average edge length * 64 * renderDistanceWeight
     * Args: distance */
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }
        d0 = Math.max(1, d0);

        d0 = d0 * 64.0D * this.renderDistanceWeight;
        return distance < d0 * d0;
    }
}
