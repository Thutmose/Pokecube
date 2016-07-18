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
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.pokemob.PokemobAIDodge;
import pokecube.core.ai.pokemob.PokemobAIHurt;
import pokecube.core.ai.pokemob.PokemobAILeapAtTarget;
import pokecube.core.ai.pokemob.PokemobAILook;
import pokecube.core.ai.pokemob.PokemobAISwimming;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.ILogicRunnable;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.ai.thread.PokemobAIThread.AIStuff;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.ai.thread.aiRunnables.AICombatMovement;
import pokecube.core.ai.thread.aiRunnables.AIFindTarget;
import pokecube.core.ai.thread.aiRunnables.AIFollowOwner;
import pokecube.core.ai.thread.aiRunnables.AIGatherStuff;
import pokecube.core.ai.thread.aiRunnables.AIHungry;
import pokecube.core.ai.thread.aiRunnables.AIIdle;
import pokecube.core.ai.thread.aiRunnables.AIMate;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;
import pokecube.core.ai.thread.logicRunnables.LogicFloatFlySwim;
import pokecube.core.ai.thread.logicRunnables.LogicInMaterials;
import pokecube.core.ai.thread.logicRunnables.LogicMovesUpdates;
import pokecube.core.ai.thread.logicRunnables.LogicCollision;
import pokecube.core.ai.thread.logicRunnables.LogicInLiquid;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.ai.utils.AISaveHandler.PokemobAI;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou */
public abstract class EntityAiPokemob extends EntityMountablePokemob
{

    public GuardAI              guardAI;
    public PokemobAIUtilityMove utilMoveAI;
    private AIStuff             aiStuff;

    private int                 lastHadTargetTime = 0;

    private PokeNavigator       navi;
    private PokemobMoveHelper   mover;
    boolean                     initAI            = true;
    boolean                     popped            = false;
    private PokemobAI           aiObject;
    boolean                     isAFish           = false;

    public TerrainSegment       currentTerrain    = null;

    float                       moveF;

    public EntityAiPokemob(World world)
    {
        super(world);
        here = Vector3.getNewVector();
    }

    ///////////////////////////////////////// Init
    ///////////////////////////////////////// things///////////////////////////////

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        return super.attackEntityAsMob(par1Entity);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase p_82196_1_, float p_82196_2_)
    {
    }

    ///////////////////////////////////// AI
    ///////////////////////////////////// States///////////////////////////////

    @Override
    public boolean canBreatheUnderwater()
    {
        return (getType1() == PokeType.water || getType2() == PokeType.water);
    }

    @Override
    public void fall(float distance, float damageMultiplier)
    {
        PokedexEntry entry = getPokedexEntry();
        boolean canFloat = entry.floats() || entry.flys() || canUseFly();
        if (distance > 4 + height) distance = 0;
        if (distance < 5) damageMultiplier = 0;
        if (!canFloat) super.fall(distance, damageMultiplier);
    }

    ////////////////// Things which happen every tick///////////////////////////

    /** @return the aiObject */
    public PokemobAI getAiObject()
    {
        if (aiObject == null)
        {
            aiObject = AISaveHandler.instance().getAI(this);
        }
        return aiObject;
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    @Override
    public boolean getCanSpawnHere()
    {
        return this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox());
    }

    @Override
    public float getDirectionPitch()
    {
        return dataManager.get(DIRECTIONPITCHDW);
    }

    @Override
    public EntityMoveHelper getMoveHelper()
    {
        if (mover != null) return mover;
        return super.getMoveHelper();
    }

    @Override
    public double getMovementSpeed()
    {
        return this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
    }
    ////////////////////////// Death Related things////////////////////////

    @Override
    public PathNavigate getNavigator()
    {
        if (navi != null) return navi;

        return super.getNavigator();
    }

    /////////////////////// Target related
    /////////////////////// things//////////////////////////////////

    @Override
    /** Called when a user uses the creative pick block button on this entity.
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    public ItemStack getPickedResult(RayTraceResult target)
    {
        return ItemPokemobEgg.getEggStack(this);
    }

    @Override
    public boolean getPokemonAIState(int state)
    {

        if (state == SADDLED)
        {
            handleArmourAndSaddle();
        }

        return (dataManager.get(AIACTIONSTATESDW) & state) != 0;
    }

    /////////////////// Movement related things///////////////////////////

    ////////////////////////////// Misc////////////////////////////////////////////////////////////////
    @Override
    /** Get number of ticks, at least during which the living entity will be
     * silent. */
    public int getTalkInterval()
    {
        return 400;
    }

    /*
     * Override to fix bad detection of isInWater for little mobs and to skip
     * the handle of water movement on water mobs
     */
    @Override
    public boolean handleWaterMovement()
    {
        if (isInWater())
        {
            if (!this.inWater)
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

                    this.playSound(SoundEvents.ENTITY_GENERIC_SWIM, f,
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
    public void init(int nb)
    {
        super.init(nb);
    }

    protected void initAI(PokedexEntry entry)
    {
        initAI = false;
        navi = new PokeNavigator(this, worldObj);
        mover = new PokemobMoveHelper(this);
        aiStuff = new AIStuff(this);

        float moveSpeed = 0.5f;
        float speedFactor = (float) (1 + Math.sqrt(entry.getStatVIT()) / (100F));
        moveSpeed *= speedFactor;
        if (entry.flys()) moveSpeed /= 1.25f;

        this.getNavigator().setSpeed(moveSpeed);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);

        // aiStuff.addAILogic(new LogicCollision(this));
        // if (true) return;
        this.tasks.addTask(1, new PokemobAISwimming(this));
        this.tasks.addTask(1, new PokemobAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(1, new PokemobAIDodge(this));
        this.tasks.addTask(4, this.aiSit = new EntityAISit(this));

        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(5, guardAI);
        this.tasks.addTask(5, utilMoveAI = new PokemobAIUtilityMove(this));

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

        aiStuff.addAITask(new AIAttack(this).setPriority(200));
        aiStuff.addAITask(new AICombatMovement(this).setPriority(600));
        if (!entry.isStationary)
        {
            aiStuff.addAITask(new AIFollowOwner(this, 2 + this.width + this.length, 2 + this.width + this.length)
                    .setPriority(400));
        }

        aiStuff.addAITask(new AIMate(this).setPriority(300));
        aiStuff.addAITask(new AIHungry(this, new EntityItem(worldObj), 16).setPriority(300));
        AIStoreStuff ai = new AIStoreStuff(this);
        aiStuff.addAITask(ai.setPriority(350));
        aiStuff.addAITask(new AIGatherStuff(this, 32, ai).setPriority(400));
        aiStuff.addAITask(new AIIdle(this).setPriority(500));
        aiStuff.addAITask(new AIFindTarget(this).setPriority(400));

        aiStuff.addAILogic(new LogicInLiquid(this));
        aiStuff.addAILogic(new LogicCollision(this));
        aiStuff.addAILogic(new LogicMovesUpdates(this));
        aiStuff.addAILogic(new LogicInMaterials(this));
        aiStuff.addAILogic(new LogicFloatFlySwim(this));

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

        d0 = d0 * 64.0D * 10;
        return distance < d0 * d0;
    }

    /** Checks if this entity is inside water (if inWater field is true as a
     * result of handleWaterMovement() returning true) */
    @Override
    public boolean isInWater()
    {
        return getPokemonAIState(INWATER);
    }

    @Override
    public void jump()
    {
        if (worldObj.isRemote) return;

        if (!this.isInWater() && !this.isInLava())
        {
            if (!this.onGround) return;

            boolean pathing = !this.getNavigator().noPath();
            double factor = 0.1;
            if (pathing)
            {
                Path path = this.getNavigator().getPath();
                Vector3 point = Vector3.getNewVector().set(path.getPathPointFromIndex(path.getCurrentPathIndex()));
                factor = 0.05 * point.distTo(here);
                factor = Math.max(0.2, factor);
            }
            // The extra factor fixes tiny pokemon being unable to jump up one
            // block.
            this.motionY += 0.5D + factor * 1 / getPokedexEntry().height;

            Potion jump = Potion.getPotionFromResourceLocation("jump_boost");

            if (this.isPotionActive(jump))
            {
                this.motionY += (this.getActivePotionEffect(jump).getAmplifier() + 1) * 0.1F;
            }
            if (isBeingRidden())
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

    @Override
    /** Moves the entity based on the specified heading. Args: strafe,
     * forward */
    public void moveEntityWithHeading(float f, float f1)
    {
        double d0;
        if (isBeingRidden())
        {
            super.moveEntityWithHeading(f, f1);
            return;
        }
        if (isServerWorld())
        {
            PokedexEntry entry = getPokedexEntry();
            if (getTransformedTo() instanceof IPokemob)
            {
                entry = ((IPokemob) getTransformedTo()).getPokedexEntry();
            }
            int aiState = dataManager.get(AIACTIONSTATESDW);
            boolean isAbleToFly = entry.floats() || entry.flys();
            boolean isWaterMob = entry.swims();
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

                this.moveRelative(f, f1, f4);
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
                this.moveRelative(f, f1, 0.02F);
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

                this.moveRelative(f, f1, f4);
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
    public void moveRelative(float strafe, float forward, float speed)
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

    @Override
    public void onDeath(DamageSource damageSource)
    {
        if (ForgeHooks.onLivingDeath(this, damageSource)) return;
        Entity entity = damageSource.getEntity();
        EntityLivingBase entitylivingbase = this.getAttackingEntity();

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

                if (this.recentlyHit > 0 && this.rand.nextFloat() < 0.025F + i * 0.01F)
                {
                    // this.addRandomDrop();
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

    @Override
    protected void onDeathUpdate()
    {
        if (!PokecubeCore.isOnClientSide())
        {
            HappinessType.applyHappiness(this, HappinessType.FAINT);
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.faint.own", "red",
                    getPokemonDisplayName().getFormattedText());
            displayMessageToOwner(mess);
            returnToPokecube();
        }
        if (!getPokemonAIState(IMoveConstants.TAMED))
        {
            AISaveHandler.instance().removeAI(this);
            if (this.getHeldItemMainhand() != null) PokecubeItems.deValidate(getHeldItemMainhand());
        }
        super.onDeathUpdate();
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

        if (getPokemonAIState(IMoveConstants.ANGRY) && getAttackTarget() == null)
        {
            this.setPokemonAIState(ANGRY, false);
        }

        if (!getPokemonAIState(IMoveConstants.ANGRY))
        {
            lastHadTargetTime--;
            if (lastHadTargetTime == 0)
            {
                this.setModifiers(PokecubeSerializer.intAsModifierArray(1717986918));
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

    //////////////// Jumping related//////////////////////////

    @Override
    public void onUpdate()
    {
        if (initAI)
        {
            initAI(getPokedexEntry());
        }
        if (popped && traded)
        {
            evolve(true, false);
            popped = false;
        }
        if (getPokedexEntry().floats() || getPokedexEntry().flys()) fallDistance = 0;
        dimension = worldObj.provider.getDimension();
        super.onUpdate();
        if (worldObj.isRemote)
        {
            int id = dataManager.get(ATTACKTARGETIDDW);
            if (id >= 0 && getAttackTarget() == null)
            {
                setAttackTarget((EntityLivingBase) PokecubeMod.core.getEntityProvider().getEntity(worldObj, id, false));
            }
            if (id < 0 && getAttackTarget() != null)
            {
                setAttackTarget(null);
            }
        }

        for (ILogicRunnable logic : aiStuff.aiLogic)
        {
            logic.doServerTick(worldObj);
        }

        String s;
        int state = dataManager.get(AIACTIONSTATESDW);
        if (getAIState(IMoveConstants.TAMED, state) && ((s = getPokemonOwnerName()) == null || s.isEmpty()))
        {
            setPokemonAIState(IMoveConstants.TAMED, false);
        }

        if (inLove > 600)
        {
            resetLoveStatus();
        }
        if (getAIState(HELD, state))
        {
            if (!isRiding() || (getRidingEntity() instanceof EntityPlayer
                    && ((EntityPlayer) getRidingEntity()).getHeldItemMainhand() != null))
            {
                setPokemonAIState(HELD, false);
                if (getRidingEntity() != null)
                {
                    this.dismountRidingEntity();
                }
            }
        }
        if (getAIState(SHOULDER, state) && (getRidingEntity() == null || !getAIState(SITTING, state)))
        {
            setPokemonAIState(SHOULDER, false);
            if (getRidingEntity() != null)
            {
                this.dismountRidingEntity();
            }
        }
        if (ticksExisted > EXITCUBEDURATION && getAIState(EXITINGCUBE, state))
        {
            setPokemonAIState(EXITINGCUBE, false);
        }
        if (this.getPokemonAIState(IMoveConstants.SITTING) && !this.getNavigator().noPath())
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

        if (isWet() && !(this.canUseSurf()))
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
    public void popFromPokecube()
    {
        super.popFromPokecube();
        popped = true;
    }

    /////////////////////////// Interaction
    /////////////////////////// logic/////////////////////////////////////////////////////

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack held)
    {
        if (hand != player.getActiveHand()) return false;
        ItemStack itemstack = player.getHeldItem(hand);
        ItemStack key = new ItemStack(Items.SHEARS, 1, Short.MAX_VALUE);
        // Check shearable interaction.
        if (getPokedexEntry().interact(key) && held != null && Tools.isSameStack(key, held)) { return false; }
        // Check Pokedex Entry defined Interaction for player.
        if (getPokedexEntry().interact(player, this, true)) return true;
        Item torch = Item.getItemFromBlock(Blocks.TORCH);
        boolean isOwner = false;
        if (getPokemonAIState(IMoveConstants.TAMED) && getOwner() != null)
        {
            isOwner = getOwner().getEntityId() == player.getEntityId();
        }
        // Either push pokemob around, or if sneaking, make it try to climb on
        // shoulder
        if (isOwner && itemstack != null && (itemstack.getItem() == Items.STICK || itemstack.getItem() == torch))
        {
            if (player.isSneaking())
            {
                if (getPokedexEntry().canSitShoulder)
                {
                    this.setPokemonAIState(SHOULDER, true);
                    this.setPokemonAIState(SITTING, true);
                    this.startRiding(player);
                }
                return true;
            }
            Vector3 look = Vector3.getNewVector().set(player.getLookVec()).scalarMultBy(5);
            look.y = 0.2;
            this.motionX += look.x;
            this.motionY += look.y;
            this.motionZ += look.z;
            return false;
        }
        // Debug thing to maximize happiness
        if (isOwner && itemstack != null && itemstack.getItem() == Items.APPLE)
        {
            if (player.capabilities.isCreativeMode && player.isSneaking())
            {
                this.addHappiness(255);
            }
        }
        // Debug thing to increase hunger time
        if (isOwner && itemstack != null && itemstack.getItem() == Items.GOLDEN_HOE)
        {
            if (player.capabilities.isCreativeMode && player.isSneaking())
            {
                this.setHungerTime(this.getHungerTime() + 1000);
            }
        }
        // Use shiny charm to make shiny
        if (isOwner && itemstack != null
                && ItemStack.areItemStackTagsEqual(itemstack, PokecubeItems.getStack("shiny_charm")))
        {
            if (player.isSneaking())
            {
                this.setShiny(!this.isShiny());
                held.splitStack(1);
            }
            return true;
        }

        // is Dyeable
        if (held != null && getPokedexEntry().dyeable)
        {
            if (held.getItem() == Items.DYE)
            {
                setSpecialInfo(held.getItemDamage());
                System.out.println(getSpecialInfo());
                held.stackSize--;
                return true;
            }
            else if (held.getItem() == Items.SHEARS) { return false; }
        }

        // Check saddle for riding.
        if (getPokemonAIState(SADDLED) && !player.isSneaking() && isOwner
                && (itemstack == null || itemstack.getItem() != PokecubeItems.pokedex))
        {
            if (!handleHmAndSaddle(player, new ItemStack(Items.SADDLE)))
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
        if (this.addedToChunk && !worldObj.isRemote && !getPokemonAIState(SADDLED) && player.isSneaking()
                && held == null && getWeight() < 40)
        {

            boolean isHeld = getPokemonAIState(HELD);

            if ((!getPokemonAIState(IMoveConstants.TAMED) || getHeldItemMainhand() == null)
                    && !getPokemonAIState(STAYING))
            {
                if (!isHeld)
                {
                    this.startRiding(player);
                    setPokemonAIState(HELD, true);
                    setPokemonAIState(SITTING, false);
                }
                else
                {
                    this.dismountRidingEntity();
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
            if (PokecubeCore.isOnClientSide() && !player.isSneaking())
            {
                player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, worldObj, (int) posX, (int) posY,
                        (int) posZ);
            }
            return true;
        }

        // Owner only interactions.
        if (isOwner && !PokecubeCore.isOnClientSide())
        {
            if (itemstack != null)
            {
                // Check if it should evolve from item, do so if yes.
                if (PokecubeItems.isValidEvoItem(itemstack) && canEvolve(itemstack))
                {
                    IPokemob evolution = evolve(true, false, itemstack);

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

                // Check if gold apple for breeding.
                if (itemstack.getItem() == Items.GOLDEN_APPLE)
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        --itemstack.stackSize;

                        if (itemstack.stackSize <= 0)
                        {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
                        }
                    }
                    this.inLove = 10;
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
                    ItemStack heldItem = getHeldItemMainhand();
                    if (heldItem != null)
                    {
                        dropItem();
                    }
                    ItemStack toSet = itemstack.copy();
                    toSet.stackSize = 1;
                    setHeldItem(toSet);
                    itemstack.stackSize--;

                    if (itemstack.stackSize <= 0)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                    return true;
                }
            }
            // Open Gui
            if (!PokecubeCore.isOnClientSide() && isOwner)
            {
                openGUI(player);
                return true;
            }
        }

        return false;
    }

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
            dataManager.set(ATTACKTARGETIDDW, Integer.valueOf(entity.getEntityId()));
        }
        if (entity == null && !worldObj.isRemote)
        {
            dataManager.set(ATTACKTARGETIDDW, Integer.valueOf(-1));
        }
        if (entity != getAttackTarget() && getAbility() != null)
        {
            getAbility().onAgress(this, entity);
        }
        super.setAttackTarget(entity);
    }

    @Override
    public void setDead()
    {
        PokecubeSerializer.getInstance().removePokemob(this);
        PokemobAIThread.removeEntity(this);
        if (getAbility() != null)
        {
            getAbility().destroy();
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
        if (!getPokemonAIState(IMoveConstants.TAMED))
        {
            AISaveHandler.instance().removeAI(this);
        }
        super.setDead();
    }

    @Override
    public void setDirectionPitch(float pitch)
    {
        dataManager.set(DIRECTIONPITCHDW, pitch);
    }

    @Override
    public void setJumping(boolean jump)
    {
        if (!worldObj.isRemote)
        {
            setPokemonAIState(JUMPING, jump);
        }
        else
        {
            isJumping = getPokemonAIState(JUMPING);
        }
    }

    @Override
    public void setMoveForward(float forward)
    {
        this.moveForward = forward;
        moveF = forward;
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        int byte0 = dataManager.get(AIACTIONSTATESDW);

        if (state == STAYING)
        {
            here.set(this);
            setHome(here.intX(), here.intY(), here.intZ(), 16);
        }

        if (flag)
        {
            dataManager.set(AIACTIONSTATESDW, Integer.valueOf((byte0 | state)));
        }
        else
        {
            dataManager.set(AIACTIONSTATESDW, Integer.valueOf((byte0 & -state - 1)));
        }
        if ((state & SITTING) > 0)
        {
            if (aiSit != null) aiSit.setSitting(flag);
            super.setSitting(flag);
        }
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
        // Run last tick's results from AI stuff
        this.aiStuff.runServerThreadTasks(worldObj);
        // Schedule AIStuff to tick for next tick.
        PokemobAIThread.scheduleAITick(aiStuff);
        this.updateAITasks();
        this.updateAITick();
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
}
