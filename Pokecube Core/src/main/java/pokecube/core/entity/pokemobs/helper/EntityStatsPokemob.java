/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityStatsPokemob extends EntityGeneticsPokemob
{
    public EntityStatsPokemob(World world)
    {
        super(world);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
        // Follow Range - default 32.0D - min 0.0D - max 2048.0D
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32);
        // Knockback Resistance - default 0.0D - min 0.0D - max 1.0D
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(10);
        // Movement Speed - default 0.699D - min 0.0D - max Double.MAX_VALUE
        float moveSpeed = 0.6f;
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);
        // Attack Damage - default 2.0D - min 0.0D - max Doubt.MAX_VALUE
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, amount)) return false;
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (this.getEntityWorld().isRemote)
        {
            return false;
        }
        else
        {
            this.idleTime = 0;
            IPokemob sourceMob = CapabilityPokemob.getPokemobFor(source.getTrueSource());
            // Don't apply the damage from the burning when on fire, the status
            // should handle that.
            if (source.isFireDamage() && (pokemobCap.getStatus() & IMoveConstants.STATUS_BRN) > 0) { return false; }

            if (source.isExplosion() && sourceMob != null
                    && pokemobCap.isType(PokeType.getType("ghost"))) { return false; }

            if (!(sourceMob != null || source instanceof PokemobDamageSource)
                    && PokecubeMod.core.getConfig().onlyPokemobsDamagePokemobs)
                return false;

            if (source.getTrueSource() instanceof EntityPlayer && !(source instanceof PokemobDamageSource))
                amount *= PokecubeMod.core.getConfig().playerToPokemobDamageScale;

            if (this.getHealth() <= 0.0F)
            {
                return false;
            }
            else if (source.isFireDamage()
                    && this.isPotionActive(Potion.getPotionFromResourceLocation("fire_resistance")))
            {
                return false;
            }
            else
            {
                this.limbSwingAmount = 1.5F;
                boolean flag = true;

                if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0F)
                {
                    if (amount <= this.lastDamage) { return false; }

                    this.damageEntity(source, amount - this.lastDamage);
                    this.lastDamage = amount;
                    flag = false;
                }
                else
                {
                    this.lastDamage = amount;
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    this.damageEntity(source, amount);
                    this.hurtTime = this.maxHurtTime = 10;
                }

                this.attackedAtYaw = 0.0F;
                Entity entity = source.getTrueSource();

                if (entity != null && source.getTrueSource() != this.getOwner())
                {
                    if (entity instanceof EntityLivingBase)
                    {
                        this.setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer)
                    {
                        this.recentlyHit = 100;
                        this.attackingPlayer = (EntityPlayer) entity;
                    }
                    else if (entity instanceof IEntityOwnable)
                    {
                        IEntityOwnable entitywolf = (IEntityOwnable) entity;

                        if (entitywolf.getOwner() != null)
                        {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag)
                {
                    this.getEntityWorld().setEntityState(this, (byte) 2);

                    if (source != DamageSource.DROWN)
                    {
                        this.markVelocityChanged();
                    }

                    if (entity != null)
                    {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1
                                + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D)
                        {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.attackedAtYaw = (float) (MathHelper.atan2(d0, d1) * 180.0D / Math.PI - this.rotationYaw);
                        // Reduces knockback from distanced moves
                        if (source instanceof PokemobDamageSource)
                        {
                            if (!source.isProjectile())
                            {
                                this.knockBack(entity, amount, d1, d0);
                            }
                        }
                        else
                        {
                            this.knockBack(entity, amount, d1, d0);
                        }
                    }
                    else
                    {
                        this.attackedAtYaw = (int) (Math.random() * 2.0D) * 180;
                    }
                }

                if (this.getHealth() <= 0.0F)
                {
                    SoundEvent s = this.getDeathSound();

                    if (flag && s != null)
                    {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    this.onDeath(source);
                }
                else
                {
                    SoundEvent s1 = this.getHurtSound(source);

                    if (flag && s1 != null)
                    {
                        this.playSound(s1, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        pokemobCap.getPokedexEntry();
        pokemobCap.setRNGValue(rand.nextInt());
        this.setHealth(getMaxHealth());
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source)
    {
        if (source instanceof PokemobDamageSource)
        {
            Move_Base move = ((PokemobDamageSource) source).move;
            return PokeType.getAttackEfficiency(move.getType(((PokemobDamageSource) source).user),
                    pokemobCap.getType1(), pokemobCap.getType2()) <= 0;
        }

        return super.isEntityInvulnerable(source);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {

        getEntityData().setBoolean("initSpawn", true);
        IPokemob pokemob = pokemobCap.specificSpawnInit();
        SpawnEvent.Post evt = new SpawnEvent.Post(pokemob.getPokedexEntry(), Vector3.getNewVector().set(this),
                getEntityWorld(), pokemob);
        MinecraftForge.EVENT_BUS.post(evt);
        return super.onInitialSpawn(difficulty, livingdata);
    }

}
