/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.pokemob.PokemobAIHurt;
import pokecube.core.ai.pokemob.PokemobAILook;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.ai.thread.aiRunnables.AICombatMovement;
import pokecube.core.ai.thread.aiRunnables.AIFindTarget;
import pokecube.core.ai.thread.aiRunnables.AIFollowOwner;
import pokecube.core.ai.thread.aiRunnables.AIGatherStuff;
import pokecube.core.ai.thread.aiRunnables.AIGuardEgg;
import pokecube.core.ai.thread.aiRunnables.AIHungry;
import pokecube.core.ai.thread.aiRunnables.AIIdle;
import pokecube.core.ai.thread.aiRunnables.AIMate;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;
import pokecube.core.ai.thread.logicRunnables.LogicCollision;
import pokecube.core.ai.thread.logicRunnables.LogicFloatFlySwim;
import pokecube.core.ai.thread.logicRunnables.LogicInLiquid;
import pokecube.core.ai.thread.logicRunnables.LogicInMaterials;
import pokecube.core.ai.thread.logicRunnables.LogicMiscUpdate;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.ai.thread.logicRunnables.LogicMovesUpdates;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.ai.utils.AISaveHandler.PokemobAI;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.ai.utils.PokemobJumpHelper;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.InitAIEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.ai.ILogicRunnable;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** @author Manchou */
public abstract class EntityAiPokemob extends EntityMountablePokemob
{

    private GuardAI           guardAI;
    private AIStuff           aiStuff;

    private PokeNavigator     navi;
    private PokemobMoveHelper mover;
    private PokemobAI         aiObject;
    private boolean           isAFish = false;

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
        return (getType1() == PokeType.getType("water") || getType2() == PokeType.getType("water")
                || getPokedexEntry().shouldDive || getPokedexEntry().swims());
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
        return pokemobCap.getDirectionPitch();
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
        return pokemobCap.getMovementSpeed();
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
        return pokemobCap.getPokemonAIState(state);
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
        if (getEntityWorld() != null) initAI(getPokedexEntry());
    }

    protected void initAI(PokedexEntry entry)
    {
        pokemobCap.navi = navi = new PokeNavigator(this, worldObj);
        pokemobCap.mover = mover = new PokemobMoveHelper(this);
        jumpHelper = new PokemobJumpHelper(this);
        pokemobCap.aiStuff = aiStuff = new AIStuff(this);

        float moveSpeed = 0.5f;
        float speedFactor = (float) (1 + Math.sqrt(entry.getStatVIT()) / (100F));
        moveSpeed *= speedFactor;
        if (entry.flys()) moveSpeed /= 1.25f;

        this.getNavigator().setSpeed(moveSpeed);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);

        // Add in the vanilla like AI methods.
        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(5, pokemobCap.guardAI = guardAI);
        this.tasks.addTask(5, pokemobCap.utilMoveAI = new PokemobAIUtilityMove(this));
        this.tasks.addTask(8, new PokemobAILook(this, EntityPlayer.class, 8.0F, 1f));
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

        // Add in the various logic AIs that are needed on both client and
        // server.
        aiStuff.addAILogic(new LogicInLiquid(this));
        aiStuff.addAILogic(new LogicCollision(this));
        aiStuff.addAILogic(new LogicMovesUpdates(this));
        aiStuff.addAILogic(new LogicInMaterials(this));
        aiStuff.addAILogic(new LogicFloatFlySwim(this));
        aiStuff.addAILogic(new LogicMiscUpdate(this));

        // Controller is done separately for ease of locating it for controls.
        aiStuff.addAILogic(pokemobCap.controller = new LogicMountedControl(this));

        if (worldObj.isRemote) return;

        // Add in the Custom type of AI tasks.
        aiStuff.addAITask(new AIAttack(this).setPriority(200));
        aiStuff.addAITask(new AICombatMovement(this).setPriority(250));
        if (!entry.isStationary)
        {
            aiStuff.addAITask(new AIFollowOwner(this, 2 + this.width + this.length, 2 + this.width + this.length)
                    .setPriority(400));
        }
        aiStuff.addAITask(new AIGuardEgg(this).setPriority(250));
        aiStuff.addAITask(new AIMate(this).setPriority(300));
        aiStuff.addAITask(new AIHungry(this, new EntityItem(worldObj), 16).setPriority(300));
        AIStoreStuff ai = new AIStoreStuff(this);
        aiStuff.addAITask(ai.setPriority(350));
        aiStuff.addAITask(new AIGatherStuff(this, 32, ai).setPriority(400));
        aiStuff.addAITask(new AIIdle(this).setPriority(500));
        aiStuff.addAITask(new AIFindTarget(this).setPriority(400));

        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        MinecraftForge.EVENT_BUS.post(new InitAIEvent(this));
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
        boolean ladder = this.isOnLadder();
        if (!ladder && !this.isInWater() && !this.isInLava())
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
            this.motionY += ladder ? 0.1 : 0.03999999910593033D;
        }
    }

    @Override
    /** Moves the entity based on the specified heading. Args: strafe,
     * forward */
    public void moveEntityWithHeading(float f, float f1)
    {
        double d0;
        if (isServerWorld())
        {
            PokedexEntry entry = getPokedexEntry();
            IPokemob transformed = CapabilityPokemob.getPokemobFor(getTransformedTo());
            if (transformed != null)
            {
                entry = transformed.getPokedexEntry();
            }
            int aiState = getTotalAIState();
            boolean isAbleToFly = entry.floats() || entry.flys();
            boolean isWaterMob = entry.swims();
            boolean shouldGoDown = false;
            boolean shouldGoUp = false;
            PathPoint p = null;
            if (!getNavigator().noPath() && !getNavigator().getPath().isFinished())
            {
                p = getNavigator().getPath().getPathPointFromIndex(getNavigator().getPath().getCurrentPathIndex());
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
                CompatWrapper.moveEntitySelf(this, this.motionX, this.motionY, this.motionZ);
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
                CompatWrapper.moveEntitySelf(this, this.motionX, this.motionY, this.motionZ);
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
                    if (!shouldGoUp)
                    {
                        this.motionY -= 0.05;
                    }

                }
                CompatWrapper.moveEntitySelf(this, this.motionX, this.motionY, this.motionZ);

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
            if (damageSource instanceof PokemobDamageSource)
            {
                ((PokemobDamageSource) damageSource).getActualEntity().onKillEntity(this);
            }
            else entity.onKillEntity(this);
        }
        else if (getAttackTarget() != null)
        {
            getAttackTarget().onKillEntity(this);
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

                if (recentlyHit > 0 && !getPokemonAIState(IPokemob.TAMED))
                {
                    int i1 = this.getExperiencePoints(this.attackingPlayer);
                    i1 = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.attackingPlayer, i1);
                    while (i1 > 0)
                    {
                        int j = EntityXPOrb.getXPSplit(i1);
                        i1 -= j;
                        this.worldObj
                                .spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
                    }
                }
            }
            captureDrops = false;
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
        if (!PokecubeCore.isOnClientSide() && getPokemonAIState(IMoveConstants.TAMED))
        {
            HappinessType.applyHappiness(this, HappinessType.FAINT);
            ITextComponent mess = new TextComponentTranslation("pokemob.action.faint.own", getPokemonDisplayName());
            displayMessageToOwner(mess);
            returnToPokecube();
        }
        if (!getPokemonAIState(IMoveConstants.TAMED))
        {
            AISaveHandler.instance().removeAI(this);
            if (this.getHeldItemMainhand() != CompatWrapper.nullStack) PokecubeItems.deValidate(getHeldItemMainhand());
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
        if (isServerWorld() && isPokemonShaking && !isPokemonWet && !hasPath() && onGround)
        {
            isPokemonWet = true;
            timePokemonIsShaking = 0.0F;
            prevTimePokemonIsShaking = 0.0F;
            worldObj.setEntityState(this, (byte) 8);
        }
    }

    //////////////// Jumping related//////////////////////////

    @Override
    public void onUpdate()
    {
        if (getPokedexEntry().floats() || getPokedexEntry().flys()) fallDistance = 0;
        dimension = worldObj.provider.getDimension();
        super.onUpdate();
        for (ILogicRunnable logic : aiStuff.aiLogic)
        {
            logic.doServerTick(worldObj);
        }
        if (egg != null && egg.isDead)
        {
            egg = null;
        }
        headRotationOld = headRotation;
        if (looksWithInterest)
        {
            headRotation = headRotation + (1.0F - headRotation) * 0.4F;
        }
        else
        {
            headRotation = headRotation + (0.0F - headRotation) * 0.4F;
        }
        if (looksWithInterest)
        {

        }
        if (isWet() && !(this.canUseSurf()))
        {
            isPokemonShaking = true;
            isPokemonWet = false;
            timePokemonIsShaking = 0.0F;
            prevTimePokemonIsShaking = 0.0F;
        }
        else if ((isPokemonShaking || isPokemonWet) && isPokemonWet)
        {
            prevTimePokemonIsShaking = timePokemonIsShaking;
            timePokemonIsShaking += 0.05F;
            if (prevTimePokemonIsShaking >= 2.0F)
            {
                isPokemonShaking = false;
                isPokemonWet = false;
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
        if (getEntityWorld().isRemote) return;
        this.playSound(this.getSound(), 0.5f, 1);
        if (this.isShiny())
        {
            Vector3 particleLoc = Vector3.getNewVector();
            for (int i = 0; i < 20; ++i)
            {
                particleLoc.set(posX + rand.nextFloat() * width * 2.0F - width, posY + 0.5D + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2.0F - width);
                PokecubeMod.core.spawnParticle(getEntityWorld(), EnumParticleTypes.VILLAGER_HAPPY.getParticleName(),
                        particleLoc, null);
            }
        }
    }

    /////////////////////////// Interaction
    /////////////////////////// logic/////////////////////////////////////////////////////
    // 1.11
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return processInteract(player, hand, player.getHeldItem(hand));
    }

    // 1.10
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack held)
    {
        return false;
    }

    @Override
    public void onSetTarget(EntityLivingBase entity)
    {
        pokemobCap.onSetTarget(entity);
    }

    @Override
    public void setAttackTarget(EntityLivingBase entity)
    {
        if (entity != null && entity.equals(this.getPokemonOwner())) { return; }
        if (entity != null && entity.equals(this)) { return; }
        super.setAttackTarget(entity);
    }

    @Override
    public void setDead()
    {
        if (addedToChunk)
        {
            PokecubeSerializer.getInstance().removePokemob(this);
            AISaveHandler.instance().removeAI(this);
            if (getAbility() != null)
            {
                getAbility().destroy();
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
        }
        super.setDead();
    }

    @Override
    public void setDirectionPitch(float pitch)
    {
        pokemobCap.setDirectionPitch(pitch);
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
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        pokemobCap.setPokemonAIState(state, flag);
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
        AIThreadManager.scheduleAITick(aiStuff);
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
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.endSection();
    }

    @Override
    public AIStuff getAI()
    {
        return pokemobCap.getAI();
    }

    @Override
    public boolean selfManaged()
    {
        return pokemobCap.selfManaged();
    }

    @Override
    public int getTotalAIState()
    {
        return pokemobCap.getTotalAIState();
    }

    @Override
    public void setTotalAIState(int state)
    {
        pokemobCap.setTotalAIState(state);
    }

    @Override
    public int getTargetID()
    {
        return pokemobCap.getTargetID();
    }

    @Override
    public void setTargetID(int id)
    {
        pokemobCap.setTargetID(id);
    }
}
