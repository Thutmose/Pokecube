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
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class EvolvePokemobTrigger implements ICriterionTrigger<EvolvePokemobTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "evolve");

    public static class Instance extends AbstractCriterionInstance
    {
        final PokedexEntry entry;

        public Instance(PokedexEntry entry)
        {
            super(ID);
            this.entry = entry != null ? entry : Database.missingno;
        }

        public boolean test(EntityPlayerMP player, IPokemob pokemob)
        {
            return (entry == Database.missingno || pokemob.getPokedexEntry() == entry)
                    && pokemob.getPokemonOwner() != player;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                             playerAdvancements;
        private final Set<ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, IPokemob pokemob)
        {
            List<ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener : this.listeners)
            {
                if (((EvolvePokemobTrigger.Instance) listener.getCriterionInstance()).test(player, pokemob))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, EvolvePokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, EvolvePokemobTrigger.Listeners> newHashMap();

    public EvolvePokemobTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener)
    {
        EvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new EvolvePokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<EvolvePokemobTrigger.Instance> listener)
    {
        EvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public EvolvePokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String name = json.has("entry") ? json.get("entry").getAsString() : "";
        return new EvolvePokemobTrigger.Instance(Database.getEntry(name));
    }

    public void trigger(EntityPlayerMP player, IPokemob pokemob)
    {
        EvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, pokemob);
        }
    }
}
