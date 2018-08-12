package pokecube.core.interfaces.entity.impl;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.events.StatusEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import thut.api.maths.Vector3;

public class PersistantStatusEffect extends BaseEffect
{
    public static final ResourceLocation            ID        = new ResourceLocation(PokecubeMod.ID,
            "persistant_status");
    public static final Map<Status, IStatusEffect>  EFFECTMAP = Maps.newHashMap();
    private static final Int2ObjectArrayMap<Status> MASKMAP   = new Int2ObjectArrayMap<>();

    public static enum Status implements IMoveConstants
    {
        SLEEP(STATUS_SLP), FREEZE(STATUS_FRZ), PARALYSIS(STATUS_PAR), BURN(STATUS_BRN), POISON(STATUS_PSN), BADPOISON(
                STATUS_PSN2);

        final byte mask;

        private Status(byte mask)
        {
            this.mask = mask;
            MASKMAP.put(mask, this);
        }

        public byte getMask()
        {
            return mask;
        }

        public static Status getStatus(byte mask)
        {
            return MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (Status stat : values())
            {
                EFFECTMAP.put(stat, new DefaultEffects(stat));
            }
        }
    }

    public static interface IStatusEffect
    {
        void affectTarget(IOngoingAffected target, IOngoingEffect effect);

        void setTick(int tick);
    }

    public static class DefaultEffects implements IStatusEffect
    {
        final Status status;
        int          tick;

        public DefaultEffects(Status status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(IOngoingAffected target, IOngoingEffect effect)
        {
            EntityLivingBase entity = target.getEntity();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null && status != Status.BADPOISON)
            {
                pokemob.getMoveStats().TOXIC_COUNTER = 0;
            }

            boolean toRemove = pokemob != null ? false : Math.random() > 0.8;
            if (effect.getDuration() == 0) toRemove = true;
            int duration = PokecubeMod.core.getConfig().attackCooldown + 10;

            EntityLivingBase targetM = entity.getAttackingEntity();
            if (targetM == null) targetM = entity.getRevengeTarget();
            if (targetM == null) targetM = entity.getLastAttackedEntity();
            if (targetM == null) targetM = entity;
            float scale = 1;
            IPokemob user = CapabilityPokemob.getPokemobFor(targetM);
            DamageSource source = user != null && user.getPokemonOwner() != null
                    ? DamageSource.causeIndirectDamage(targetM, user.getPokemonOwner())
                    : targetM != null ? DamageSource.causeMobDamage(targetM) : new DamageSource("generic");

            if (pokemob != null)
            {
                source.setDamageIsAbsolute();
                source.setDamageBypassesArmor();
            }
            else
            {
                if (entity instanceof EntityPlayer)
                {
                    scale = (float) (user != null && user.isPlayerOwned()
                            ? PokecubeMod.core.getConfig().ownedPlayerDamageRatio
                            : PokecubeMod.core.getConfig().wildPlayerDamageRatio);
                }
                else
                {
                    scale = (float) (entity instanceof INpc ? PokecubeMod.core.getConfig().pokemobToNPCDamageRatio
                            : PokecubeMod.core.getConfig().pokemobToOtherMobDamageRatio);
                }
            }
            if (scale <= 0) toRemove = true;

            switch (status)
            {
            case BADPOISON:
                if (pokemob != null)
                {
                    entity.attackEntityFrom(source,
                            scale * (pokemob.getMoveStats().TOXIC_COUNTER + 1) * entity.getMaxHealth() / 16f);
                    spawnPoisonParticle(entity);
                    spawnPoisonParticle(entity);
                    pokemob.getMoveStats().TOXIC_COUNTER++;
                }
                else
                {
                    entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 8f);
                    spawnPoisonParticle(entity);
                }
                break;
            case BURN:
                if (scale > 0) entity.setFire(duration);
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 16f);
                break;
            case FREEZE:
                if (Math.random() > 0.9)
                {
                    toRemove = true;
                }
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("weakness"), duration, 100));
                break;
            case PARALYSIS:
                break;
            case POISON:
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() / 8f);
                spawnPoisonParticle(entity);
                break;
            case SLEEP:
                if (Math.random() > 0.9)
                {
                    toRemove = true;
                }
                else
                {
                    entity.addPotionEffect(
                            new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration, 100));
                    entity.addPotionEffect(
                            new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration, 100));
                    entity.addPotionEffect(
                            new PotionEffect(Potion.getPotionFromResourceLocation("weakness"), duration, 100));
                    spawnSleepParticle(entity);
                }
                break;
            default:
                toRemove = true;
                break;
            }
            if (toRemove)
            {
                if (pokemob != null) pokemob.healStatus();
                effect.setDuration(0);
            }
        }

        @Override
        public void setTick(int tick)
        {
            this.tick = tick;
        }

        protected void spawnSleepParticle(Entity entity)
        {
            Random rand = new Random();
            Vector3 particleLoc = Vector3.getNewVector();
            Vector3 vel = Vector3.getNewVector();
            for (int i = 0; i < 3; ++i)
            {
                particleLoc.set(entity.posX, entity.posY + 0.5D + rand.nextFloat() * entity.height, entity.posZ);
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(), "mobSpell", particleLoc, vel);
            }
        }

        protected void spawnPoisonParticle(Entity entity)
        {
            Random rand = new Random();
            Vector3 particleLoc = Vector3.getNewVector();
            int i = 0xFFFF00FF;
            double d0 = (i >> 16 & 255) / 255.0D;
            double d1 = (i >> 8 & 255) / 255.0D;
            double d2 = (i >> 0 & 255) / 255.0D;
            Vector3 vel = Vector3.getNewVector().set(d0, d1, d2);
            for (i = 0; i < 3; ++i)
            {
                particleLoc.set(entity.posX, entity.posY + 0.5D + rand.nextFloat() * entity.height, entity.posZ);
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(), "mobSpell", particleLoc, vel);
            }
        }
    }

    private Status status;

    public PersistantStatusEffect()
    {
        super(ID);
    }

    public PersistantStatusEffect(byte status, int timer)
    {
        super(ID);
        this.status = Status.getStatus(status);
        if (this.status == null)
        {
            PokecubeMod.log(Level.WARNING, "Error setting of status. " + status, new IllegalArgumentException());
        }
        this.setDuration(timer);
    }

    @Override
    public void affectTarget(IOngoingAffected target)
    {
        if (status == null)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(target.getEntity());
            if (pokemob == null || pokemob.getStatus() == IMoveConstants.STATUS_NON)
            {
                setDuration(0);
            }
            else if (pokemob != null)
            {
                status = Status.getStatus(pokemob.getStatus());
            }
        }
        IStatusEffect effect = EFFECTMAP.get(status);
        if (effect != null)
        {
            StatusEvent event = new StatusEvent(target.getEntity(), status);
            if (!PokecubeCore.MOVE_BUS.post(event))
            {
                effect.setTick(getDuration());
                effect.affectTarget(target, this);
            }
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("S")) this.status = Status.values()[nbt.getByte("S")];
        else this.setDuration(0);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = super.serializeNBT();
        if (status != null) tag.setByte("S", (byte) status.ordinal());
        return tag;
    }

}
