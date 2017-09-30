/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import javax.annotation.Nullable;

import org.nfunk.jep.JEP;

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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.KillEvent;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityStatsPokemob extends EntityGeneticsPokemob
{
    double      moveSpeed;
    private int killCounter = 0;
    private int resetTick   = 0;

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
        moveSpeed = 0.6f;
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
            this.entityAge = 0;
            IPokemob sourceMob = CapabilityPokemob.getPokemobFor(source.getEntity());
            if (source.isExplosion() && sourceMob != null
                    && pokemobCap.isType(PokeType.getType("ghost"))) { return false; }

            if (!(sourceMob != null || source instanceof PokemobDamageSource)
                    && PokecubeMod.core.getConfig().onlyPokemobsDamagePokemobs)
                return false;

            if (source.getEntity() instanceof EntityPlayer && !(source instanceof PokemobDamageSource))
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
                Entity entity = source.getEntity();

                if (entity != null)
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

                    if (source != DamageSource.drown)
                    {
                        this.setBeenAttacked();
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
                    SoundEvent s1 = this.getHurtSound();

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
        if (PokecubeCore.isOnClientSide()) this.setHealth(getMaxHealth());
        else this.setHealth(0);
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

    /** This method gets called when the entity kills another one. */
    @Override
    public void onKillEntity(EntityLivingBase attacked)
    {
        if (getEntityWorld().isRemote) return;
        IPokemob attacker = pokemobCap;
        IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (PokecubeCore.core.getConfig().nonPokemobExp && attackedMob == null)
        {
            JEP parser = new JEP();
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex();
            parser.addVariable("h", 0);
            parser.addVariable("a", 0);
            parser.parseExpression(PokecubeCore.core.getConfig().nonPokemobExpFunction);
            parser.setVarValue("h", attacked.getMaxHealth());
            parser.setVarValue("a", attacked.getTotalArmorValue());
            int exp = (int) parser.getValue();
            if (parser.hasError()) exp = 0;
            attacker.setExp(attacker.getExp() + exp, true);
            return;
        }
        if (attackedMob != null && attacked.getHealth() <= 0)
        {
            boolean giveExp = !attackedMob.isShadow();
            if (killCounter == 0)
            {
                resetTick = ticksExisted;
            }
            killCounter++;
            if (resetTick < ticksExisted - 100)
            {
                killCounter = 0;
            }
            giveExp = giveExp && killCounter <= 5;
            boolean pvp = attackedMob.getPokemonAIState(IMoveConstants.TAMED)
                    && (attackedMob.getPokemonOwner() instanceof EntityPlayer);
            if (pvp && !PokecubeMod.core.getConfig().pvpExp)
            {
                giveExp = false;
            }
            if ((attackedMob.getPokemonAIState(IMoveConstants.TAMED) && !PokecubeMod.core.getConfig().trainerExp))
            {
                giveExp = false;
            }
            KillEvent event = new KillEvent(attacker, attackedMob, giveExp);
            MinecraftForge.EVENT_BUS.post(event);
            giveExp = event.giveExp;
            if (event.isCanceled())
            {

            }
            else if (giveExp)
            {
                attacker.setExp(attacker.getExp() + Tools.getExp(
                        (float) (pvp ? PokecubeMod.core.getConfig().pvpExpMultiplier
                                : PokecubeCore.core.getConfig().expScaleFactor),
                        attackedMob.getBaseXP(), attackedMob.getLevel()), true);
                byte[] evsToAdd = Pokedex.getInstance().getEntry(attackedMob.getPokedexNb()).getEVs();
                attacker.addEVs(evsToAdd);
            }
            Entity targetOwner = attackedMob.getPokemonOwner();
            pokemobCap.displayMessageToOwner(
                    new TextComponentTranslation("pokemob.action.faint.enemy", attackedMob.getPokemonDisplayName()));
            if (targetOwner instanceof EntityPlayer && attacker.getPokemonOwner() != targetOwner
                    && !PokecubeMod.pokemobsDamagePlayers)
            {
                setAttackTarget((EntityLivingBase) targetOwner);
            }
            else
            {
                setAttackTarget(null);
            }
            if (pokemobCap.getPokedexEntry().isFood(attackedMob.getPokedexEntry())
                    && attacker.getPokemonAIState(IMoveConstants.HUNTING))
            {
                attacker.eat(getAttackTarget());
                if (attacked instanceof EntityPokemob) ((EntityPokemob) attacked).wasEaten = true;
                attacker.setPokemonAIState(IMoveConstants.HUNTING, false);
                getNavigator().clearPathEntity();
            }
        }
        else
        {
            setAttackTarget(null);
        }
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
