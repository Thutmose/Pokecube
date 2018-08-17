package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

public class FirstPokemobTrigger implements ICriterionTrigger<FirstPokemobTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "get_first_pokemob");

    public static class Instance extends AbstractCriterionInstance
    {
        public Instance()
        {
            super(ID);
        }

        public boolean test()
        {
            return true;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player)
        {
            List<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener : this.listeners)
            {
                if (((FirstPokemobTrigger.Instance) listener.getCriterionInstance()).test())
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<FirstPokemobTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, FirstPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, FirstPokemobTrigger.Listeners> newHashMap();

    public FirstPokemobTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
    {
        FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new FirstPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<FirstPokemobTrigger.Instance> listener)
    {
        FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty())
            {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    /** Deserialize a ICriterionInstance of this trigger from the data in the
     * JSON. */
    @Override
    public FirstPokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        return new FirstPokemobTrigger.Instance();
    }

    public void trigger(EntityPlayerMP player)
    {
        FirstPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player);
        }
    }
}
