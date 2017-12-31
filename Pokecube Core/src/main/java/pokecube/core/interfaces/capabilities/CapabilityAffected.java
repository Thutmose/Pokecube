package pokecube.core.interfaces.capabilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.core.events.OngoingTickEvent;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect.AddType;

public class CapabilityAffected
{
    @CapabilityInject(IOngoingAffected.class)
    public static final Capability<IOngoingAffected> AFFECTED_CAP = null;

    public static IOngoingAffected getAffected(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn.hasCapability(AFFECTED_CAP, null)) return entityIn.getCapability(AFFECTED_CAP, null);
        else if (IOngoingAffected.class.isInstance(entityIn)) return IOngoingAffected.class.cast(entityIn);
        return null;
    }

    public static boolean addEffect(Entity mob, IOngoingEffect effect)
    {
        IOngoingAffected affected = getAffected(mob);
        if (affected != null) { return affected.addEffect(effect); }
        return false;
    }

    public static class Storage implements Capability.IStorage<IOngoingAffected>
    {

        @Override
        public NBTBase writeNBT(Capability<IOngoingAffected> capability, IOngoingAffected instance, EnumFacing side)
        {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IOngoingAffected> capability, IOngoingAffected instance, EnumFacing side,
                NBTBase nbt)
        {
            if (nbt instanceof NBTTagList) instance.deserializeNBT((NBTTagList) nbt);
        }

    }

    public static class DefaultAffected implements IOngoingAffected, ICapabilitySerializable<NBTTagList>
    {
        EntityLivingBase                                 entity;
        final List<IOngoingEffect>                       effects = Lists.newArrayList();
        IOngoingEffect[]                                 cachedArray;
        final Map<ResourceLocation, Set<IOngoingEffect>> map     = Maps.newHashMap();

        public DefaultAffected()
        {
        }

        public DefaultAffected(EntityLivingBase entity)
        {
            this.entity = entity;
            for (ResourceLocation id : EFFECTS.keySet())
            {
                map.put(id, Sets.newConcurrentHashSet());
            }
        }

        @Override
        public EntityLivingBase getEntity()
        {
            return entity;
        }

        @Override
        public List<IOngoingEffect> getEffects()
        {
            return effects;
        }

        @Override
        public void clearEffects()
        {
            effects.clear();
            for (Set<IOngoingEffect> set : map.values())
                set.clear();
        }

        @Override
        public boolean addEffect(IOngoingEffect effect)
        {
            if (effect.allowMultiple())
            {
                Collection<IOngoingEffect> set = getEffects(effect.getID());
                for (IOngoingEffect old : set)
                {
                    AddType type = effect.canAdd(this, old);
                    if (type != AddType.ACCEPT)
                    {
                        switch (type)
                        {
                        case UPDATED:
                            return true;
                        default:
                            return false;
                        }
                    }
                }
                effects.add(effect);
                getEffects(effect.getID()).add(effect);
                return true;
            }
            else
            {
                Collection<IOngoingEffect> set = getEffects(effect.getID());
                if (!set.isEmpty()) return false;
                set.add(effect);
                effects.add(effect);
                return true;
            }
        }

        @Override
        public Collection<IOngoingEffect> getEffects(ResourceLocation id)
        {
            return map.get(id);
        }

        @Override
        public void removeEffects(ResourceLocation id)
        {
            Collection<IOngoingEffect> set = getEffects(id);
            effects.removeAll(set);
            set.clear();
        }

        @Override
        public void removeEffect(IOngoingEffect effect)
        {
            Collection<IOngoingEffect> set = getEffects(effect.getID());
            effects.remove(effect);
            set.remove(effect);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == AFFECTED_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return AFFECTED_CAP.cast(this);
            return null;
        }

        @Override
        public void tick()
        {
            Set<IOngoingEffect> stale = Sets.newHashSet();
            cachedArray = effects.toArray(new IOngoingEffect[effects.size()]);
            for (IOngoingEffect effect : cachedArray)
            {
                if (!MinecraftForge.EVENT_BUS.post(new OngoingTickEvent(getEntity(), effect)))
                {
                    int duration = effect.getDuration();
                    if (duration > 0) duration = duration - 1;
                    effect.setDuration(duration);
                    effect.affectTarget(this);
                    if (effect.getDuration() == 0) stale.add(effect);
                }
                else if (effect.getDuration() == 0) stale.add(effect);
            }
            for (IOngoingEffect effect : stale)
                removeEffect(effect);
        }
    }
}
