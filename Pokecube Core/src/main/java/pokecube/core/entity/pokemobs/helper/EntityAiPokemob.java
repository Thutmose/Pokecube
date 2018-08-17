/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.pokemob.PokemobAILookAt;
import pokecube.core.ai.pokemob.PokemobAILookIdle;
import pokecube.core.ai.utils.PokemobJumpHelper;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;
import thut.api.entity.ai.ILogicRunnable;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityAiPokemob extends EntityMountablePokemob
{
    public EntityAiPokemob(World world)
    {
        super(world);
        here = Vector3.getNewVector();
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase p_82196_1_, float p_82196_2_)
    {
        // TODO decide if I want to do something here?
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return (pokemobCap.isType(PokeType.getType("water")) || pokemobCap.getPokedexEntry().shouldDive
                || pokemobCap.getPokedexEntry().swims());
    }

    @Override
    public void fall(float distance, float damageMultiplier)
    {
        boolean canFloat = pokemobCap.floats() || pokemobCap.flys() || pokemobCap.canUseFly();
        if (distance > 4 + height) distance = 0;
        if (distance < 5) damageMultiplier = 0;
        if (!canFloat) super.fall(distance, damageMultiplier);
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    @Override
    public boolean getCanSpawnHere()
    {
        return this.getEntityWorld().checkNoEntityCollision(this.getEntityBoundingBox());
    }

    @Override
    public EntityMoveHelper getMoveHelper()
    {
        if (pokemobCap.mover != null) return pokemobCap.mover;
        return super.getMoveHelper();
    }

    ////////////////////////// Death Related things////////////////////////

    @Override
    public PathNavigate getNavigator()
    {
        if (pokemobCap.navi != null) return pokemobCap.navi;
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
        return ItemPokemobEgg.getEggStack(pokemobCap);
    }

    /////////////////// Movement related things///////////////////////////

    ////////////////////////////// Misc////////////////////////////////////////////////////////////////
    @Override
    /** Get number of ticks, at least during which the living entity will be
     * silent. */
    public int getTalkInterval()
    {// TODO config option for this maybe?
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
                if (!pokemobCap.swims())
                {
                    float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D
                            + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;

                    if (f > 1.0F)
                    {
                        f = 1.0F;
                    }

                    this.playSound(SoundEvents.ENTITY_GENERIC_SWIM, f,
                            1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    float f1 = MathHelper.floor(this.getEntityBoundingBox().minY);
                    int i;
                    float f2;
                    float f3;

                    for (i = 0; i < 1.0F + this.width * 20.0F; ++i)
                    {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.getEntityWorld().spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + f2, f1 + 1.0F,
                                this.posZ + f3, this.motionX, this.motionY - this.rand.nextFloat() * 0.2F,
                                this.motionZ);
                    }

                    for (i = 0; i < 1.0F + this.width * 20.0F; ++i)
                    {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.getEntityWorld().spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + f2, f1 + 1.0F,
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
        if (getEntityWorld() != null) initAI(pokemobCap.getPokedexEntry());
    }

    protected void initAI(PokedexEntry entry)
    {
        jumpHelper = new PokemobJumpHelper(this);

        float moveSpeed = 0.5f;
        float speedFactor = (float) (1 + Math.sqrt(entry.getStatVIT()) / (100F));
        moveSpeed *= speedFactor;

        this.getNavigator().setSpeed(moveSpeed);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);

        // Add in the vanilla like AI methods.
        // Look at playerAI
        this.tasks.addTask(8, new PokemobAILookAt(this, EntityPlayer.class, 8.0F, 1f));
        // Look randomly around AI.
        this.tasks.addTask(9, new PokemobAILookIdle(this, 8, 0.01f));
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
        return pokemobCap.getLogicState(LogicStates.INWATER);
    }

    @Override
    public void jump()
    {
        if (getEntityWorld().isRemote) return;
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
            this.motionY += 0.5D + factor * 1 / pokemobCap.getPokedexEntry().height;

            Potion jump = Potion.getPotionFromResourceLocation("jump_boost");

            if (this.isPotionActive(jump))
            {
                this.motionY += (this.getActivePotionEffect(jump).getAmplifier() + 1) * 0.1F;
            }
            if (pokemobCap.getGeneralState(GeneralStates.CONTROLLED))
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
            if (ladder) this.motionY = Math.min(this.motionY, 0.5);
        }
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        if (PokecubeCore.core.getConfig().pokemobCollisions)
        {
            super.collideWithNearbyEntities();
        }
    }

    @Override
    /** Moves the entity based on the specified heading. Args: strafe, up,
     * forward */// TODO fix minor bugs here.
    public void travel(float strafe, float up, float forward)
    {
        PokedexEntry entry = pokemobCap.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemobCap.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        boolean isAbleToFly = pokemobCap.floats() || pokemobCap.flys();
        boolean isWaterMob = entry.swims();
        if (isAbleToFly
                && !(pokemobCap.getLogicState(LogicStates.SLEEPING) || pokemobCap.getLogicState(LogicStates.SITTING)))
            this.setNoGravity(true);
        else this.setNoGravity(false);

        if (!(isAbleToFly || (isWaterMob && isInWater())))
        {
            super.travel(strafe, up, forward);
            return;
        }

        if (this.isServerWorld() || this.canPassengerSteer())
        {
            if (!this.isInWater())
            {
                if (!this.isInLava())
                {
                    float f6 = 0.91F;
                    BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos
                            .retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);

                    if (this.onGround)
                    {
                        IBlockState state = this.world.getBlockState(blockpos$pooledmutableblockpos);
                        f6 = state.getBlock().getSlipperiness(state, world, blockpos$pooledmutableblockpos, this)
                                * 0.91F;
                    }

                    float f7 = 0.16277136F / (f6 * f6 * f6);
                    float f8;

                    if (this.onGround || isAbleToFly)
                    {
                        f8 = this.getAIMoveSpeed() * f7;
                    }
                    else
                    {
                        f8 = this.jumpMovementFactor;
                    }

                    this.moveRelative(strafe, up, forward, f8);
                    f6 = 0.91F;

                    if (this.onGround)
                    {
                        IBlockState state = this.world.getBlockState(blockpos$pooledmutableblockpos.setPos(this.posX,
                                this.getEntityBoundingBox().minY - 1.0D, this.posZ));
                        f6 = state.getBlock().getSlipperiness(state, world, blockpos$pooledmutableblockpos, this)
                                * 0.91F;
                    }

                    if (this.isOnLadder())
                    {
                        this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
                        this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
                        this.fallDistance = 0.0F;

                        if (this.motionY < -0.15D)
                        {
                            this.motionY = -0.15D;
                        }
                    }

                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

                    if (this.collidedHorizontally && this.isOnLadder())
                    {
                        this.motionY = 0.2D;
                    }

                    if (this.isPotionActive(MobEffects.LEVITATION))
                    {
                        this.motionY += (0.05D
                                * (double) (this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1)
                                - this.motionY) * 0.2D;
                    }
                    else
                    {
                        blockpos$pooledmutableblockpos.setPos(this.posX, 0.0D, this.posZ);

                        if (!this.world.isRemote || this.world.isBlockLoaded(blockpos$pooledmutableblockpos)
                                && this.world.getChunkFromBlockCoords(blockpos$pooledmutableblockpos).isLoaded())
                        {
                            if (!this.hasNoGravity())
                            {
                                this.motionY -= 0.08D;
                            }
                        }
                        else if (this.posY > 0.0D)
                        {
                            this.motionY = -0.1D;
                        }
                        else
                        {
                            this.motionY = 0.0D;
                        }
                    }

                    this.motionY *= isAbleToFly ? f6 : 0.9800000190734863D;
                    this.motionX *= (double) f6;
                    this.motionZ *= (double) f6;
                    blockpos$pooledmutableblockpos.release();
                }
                else
                {
                    double d4 = this.posY;
                    this.moveRelative(strafe, up, forward, 0.02F);
                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                    this.motionX *= 0.5D;
                    this.motionY *= 0.5D;
                    this.motionZ *= 0.5D;

                    if (!this.hasNoGravity())
                    {
                        this.motionY -= 0.02D;
                    }

                    if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
                            this.motionY + 0.6000000238418579D - this.posY + d4, this.motionZ))
                    {
                        this.motionY = 0.30000001192092896D;
                    }
                }
            }
            else
            {
                double d0 = this.posY;
                float f1 = this.getWaterSlowDown();
                float f2 = 0.02F;
                float f3 = (float) EnchantmentHelper.getDepthStriderModifier(this);
                if (isWaterMob) f3 *= 2.5;

                if (f3 > 3.0F)
                {
                    f3 = 3.0F;
                }

                if (!this.onGround)
                {
                    f3 *= 0.5F;
                }

                if (f3 > 0.0F)
                {
                    f1 += (0.54600006F - f1) * f3 / 3.0F;
                    f2 += (this.getAIMoveSpeed() - f2) * f3 / 3.0F;
                }
                this.moveRelative(strafe, up, forward, f2);
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                this.motionX *= (double) f1;
                this.motionY *= 0.800000011920929D;
                this.motionZ *= (double) f1;

                if (!this.hasNoGravity() && !isWaterMob)
                {
                    this.motionY -= 0.02D;
                }

                if (!isWaterMob && this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
                        this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
                {
                    this.motionY = 0.30000001192092896D;
                }
            }
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d5 = this.posX - this.prevPosX;
        double d7 = this.posZ - this.prevPosZ;
        double d9 = isAbleToFly ? this.posY - this.prevPosY : 0.0D;
        float f10 = MathHelper.sqrt(d5 * d5 + d9 * d9 + d7 * d7) * 4.0F;

        if (f10 > 1.0F)
        {
            f10 = 1.0F;
        }

        this.limbSwingAmount += (f10 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    protected void onDeathUpdate()
    {
        if (isServerWorld())
        {
            if (pokemobCap.getGeneralState(GeneralStates.TAMED))
            {
                HappinessType.applyHappiness(pokemobCap, HappinessType.FAINT);
                ITextComponent mess = new TextComponentTranslation("pokemob.action.faint.own",
                        pokemobCap.getPokemonDisplayName());
                pokemobCap.displayMessageToOwner(mess);
                pokemobCap.returnToPokecube();
            }
            else
            {
                if (this.getHeldItemMainhand() != ItemStack.EMPTY) PokecubeItems.deValidate(getHeldItemMainhand());
            }
        }
        super.onDeathUpdate();
    }

    @Override
    /** interpolated look vector */
    public Vec3d getLook(float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
        }
        else
        {
            float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float f1 = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
            return this.getVectorForRotation(f, f1);
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
        if (isServerWorld() && isPokemonShaking && !isPokemonWet && !hasPath() && onGround)
        {
            isPokemonWet = true;
            timePokemonIsShaking = 0.0F;
            prevTimePokemonIsShaking = 0.0F;
            getEntityWorld().setEntityState(this, (byte) 8);
        }
    }

    //////////////// Jumping related//////////////////////////

    @Override
    public void onUpdate()
    {
        if (pokemobCap.floats() || pokemobCap.flys()) fallDistance = 0;
        dimension = getEntityWorld().provider.getDimension();
        // Ensure that these use the pokecube ones instead of vanilla
        this.navigator = getNavigator();
        this.moveHelper = getMoveHelper();
        this.jumpHelper = getJumpHelper();
        super.onUpdate();
        if (pokemobCap.selfManaged()) for (ILogicRunnable logic : pokemobCap.getAI().aiLogic)
        {
            logic.doServerTick(getEntityWorld());
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
        if (isWet() && !(pokemobCap.canUseSurf()))
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
            if (timePokemonIsShaking > 0.4F && !pokemobCap.swims())
            {
                float f = (float) posY;
                int i = (int) (MathHelper.sin((timePokemonIsShaking - 0.4F) * (float) Math.PI) * 7F);

                for (int j = 0; j < i; j++)
                {
                    float f1 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    float f2 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    getEntityWorld().spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + f1, f + 0.8F, posZ + f2,
                            motionX, motionY, motionZ);
                }
            }
        }
    }

    @Override
    public void setDead()
    {
        if (addedToChunk)
        {
            if (PokecubeMod.debug && pokemobCap.isPlayerOwned())
            {
                PokecubeMod.log("Setting Dead: " + this);
            }
            if (pokemobCap.getHome() != null && pokemobCap.getHome().getY() > 0
                    && getEntityWorld().isAreaLoaded(pokemobCap.getHome(), 2))
            {
                TileEntity te = getEntityWorld().getTileEntity(pokemobCap.getHome());
                if (te != null && te instanceof TileEntityNest)
                {
                    TileEntityNest nest = (TileEntityNest) te;
                    nest.removeResident(pokemobCap);
                }
            }
        }
        super.setDead();
    }

    @Override
    public void setJumping(boolean jump)
    {
        if (!getEntityWorld().isRemote)
        {
            pokemobCap.setLogicState(LogicStates.JUMPING, jump);
        }
        else
        {
            isJumping = pokemobCap.getLogicState(LogicStates.JUMPING);
        }
    }

    @Override
    public void setSwingingArms(boolean swingingArms)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setJumpPower(int jumpPowerIn)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canJump()
    {
        return true;
    }

    @Override
    public void handleStartJump(int p_184775_1_)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleStopJump()
    {
        // TODO Auto-generated method stub

    }

    /** Entity won't drop items or experience points if this returns false */
    @Override
    protected boolean canDropLoot()
    {
        return !pokemobCap.getGeneralState(GeneralStates.TAMED);
    }
}
