package pokecube.core.interfaces.entity.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.EffectEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import thut.core.common.commands.CommandTools;

public class NonPersistantStatusEffect extends BaseEffect
{
    public static final ResourceLocation            ID        = new ResourceLocation(PokecubeMod.ID,
            "non_persistant_status");
    public static final Map<Effect, IEffect>        EFFECTMAP = Maps.newHashMap();
    private static final Int2ObjectArrayMap<Effect> MASKMAP   = new Int2ObjectArrayMap<>();

    public static enum Effect implements IMoveConstants
    {
        CONFUSED(CHANGE_CONFUSED), CURSED(CHANGE_CURSE), FLINCH(CHANGE_FLINCH);
        final byte mask;

        private Effect(byte mask)
        {
            this.mask = mask;
        }

        public byte getMask()
        {
            return mask;
        }

        public static Effect getStatus(byte mask)
        {
            return MASKMAP.get(mask);
        }

        public static void initDefaults()
        {
            for (Effect stat : values())
            {
                EFFECTMAP.put(stat, new DefaultEffects(stat));
            }
        }
    }

    public static interface IEffect
    {
        void affectTarget(IOngoingAffected target, IOngoingEffect effect);

        void setTick(int tick);
    }

    public static class DefaultEffects implements IEffect
    {
        public final Effect status;
        int                 tick;

        public DefaultEffects(Effect status)
        {
            this.status = status;
        }

        @Override
        public void affectTarget(IOngoingAffected target, IOngoingEffect effect)
        {
            EntityLivingBase entity = target.getEntity();
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            switch (status)
            {
            case CONFUSED:
                entity.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("nausea"), 10));
                break;
            case CURSED:
                if (pokemob != null)
                {
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.curse", "red",
                            pokemob.getPokemonDisplayName().getFormattedText());
                    pokemob.displayMessageToOwner(mess);
                }
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
                if (scale <= 0) effect.setDuration(0);
                entity.attackEntityFrom(source, scale * entity.getMaxHealth() * 0.25f);
                break;
            case FLINCH:
                break;
            default:
                break;

            }
        }

        @Override
        public void setTick(int tick)
        {
            this.tick = tick;
        }
    }

    public Effect effect;

    public NonPersistantStatusEffect()
    {
        super(ID);
        // Default duration is -1, the mob should handle removing flinch
        // condition, or removing it when it "runs out"
        this.setDuration(-1);
    }

    public NonPersistantStatusEffect(Effect effect)
    {
        this();
        this.effect = effect;
    }

    @Override
    public boolean onSavePersistant()
    {
        return false;
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(IOngoingAffected affected, IOngoingEffect toAdd)
    {
        if (toAdd instanceof NonPersistantStatusEffect && ((NonPersistantStatusEffect) toAdd).effect == this.effect)
            return AddType.DENY;
        return AddType.ACCEPT;
    }

    @Override
    public void affectTarget(IOngoingAffected target)
    {
        IEffect effect = EFFECTMAP.get(this.effect);
        if (effect != null)
        {
            EffectEvent event = new EffectEvent(target.getEntity(), this.effect);
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
        this.effect = Effect.values()[nbt.getByte("S")];
        super.deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = super.serializeNBT();
        tag.setByte("S", (byte) effect.ordinal());
        return tag;
    }

}
