package pokecube.core.interfaces.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.interfaces.PokecubeMod;

public interface IOngoingAffected extends INBTSerializable<NBTTagList>
{
    public static Map<ResourceLocation, Class<? extends IOngoingEffect>> EFFECTS = Maps.newHashMap();

    public static interface IOngoingEffect extends INBTSerializable<NBTTagCompound>
    {
        public static enum AddType
        {
            DENY, ACCEPT, UPDATED;
        }

        /** Apply whatever the effect needed is, this method is responsible for
         * lowering the duration if needed.
         * 
         * @param target */
        void affectTarget(IOngoingAffected target);

        /** @return Does this effect persist on saving and loading states. */
        default boolean onSavePersistant()
        {
            return true;
        }

        /** Should multiples of this effect be allowed at once. If false, a new
         * effect of the same ID will not be allowed to be added while this one
         * is active.
         * 
         * @return */
        default boolean allowMultiple()
        {
            return false;
        }

        /** if you have an effect that allows multiple in some cases, but not
         * all cases, you can use this to filter whether the effect should be
         * added. This method will only be called if allowMultiple returns true,
         * and will always be called directly before applying affected. This
         * means you can use this to edit this effect and then cancel
         * application of affected. <br>
         * <br>
         * ACCEPT -> add the new effect and return true.<br>
         * DENY -> Do not add the new effect, return false.<br>
         * UPDATED -> Do not add the new effect, return true.
         * 
         * @param affected
         * @return can this effect be added to the mob. */
        default AddType canAdd(IOngoingAffected affected, IOngoingEffect toAdd)
        {
            return AddType.ACCEPT;
        }

        /** @return how many times should affectTarget be called. by default,
         *         this happens once every
         *         {@link pokecube.core.handlers.Config#attackCooldown} ticks,
         *         if this value is less than 0, it will never run out. */
        int getDuration();

        void setDuration(int duration);

        ResourceLocation getID();

        @Override
        default NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("D", getDuration());
            return tag;
        }

        @Override
        default void deserializeNBT(NBTTagCompound nbt)
        {
            setDuration(nbt.getInteger("D"));
        }

    }

    /** @return The Entity to be affected. */
    EntityLivingBase getEntity();

    /** @return a list of effects currently applying to this. */
    List<IOngoingEffect> getEffects();

    boolean addEffect(IOngoingEffect effect);

    void clearEffects();

    Collection<IOngoingEffect> getEffects(ResourceLocation id);

    void removeEffects(ResourceLocation id);

    void removeEffect(IOngoingEffect effect);

    void tick();

    @Override
    default void deserializeNBT(NBTTagList nbt)
    {
        clearEffects();
        for (int i = 0; i < nbt.tagCount(); i++)
        {
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            String key = tag.getString("K");
            NBTTagCompound value = tag.getCompoundTag("V");
            ResourceLocation loc = new ResourceLocation(key);
            Class<? extends IOngoingEffect> effectClass = EFFECTS.get(loc);
            if (effectClass != null)
            {
                try
                {
                    IOngoingEffect effect = effectClass.newInstance();
                    effect.deserializeNBT(value);
                    addEffect(effect);
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error loading effect: " + key + " " + value, e);
                }
            }
        }
    }

    @Override
    default NBTTagList serializeNBT()
    {
        NBTTagList list = new NBTTagList();
        for (IOngoingEffect effect : getEffects())
        {
            if (effect.onSavePersistant())
            {
                NBTTagCompound tag = effect.serializeNBT();
                if (tag != null)
                {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setString("K", effect.getID() + "");
                    nbt.setTag("V", tag);
                    list.appendTag(nbt);
                }
            }
        }
        return list;
    }
}
