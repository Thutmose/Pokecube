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

public class MegaEvolvePokemobTrigger implements ICriterionTrigger<MegaEvolvePokemobTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "mega_evolve");

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
        private final Set<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, IPokemob pokemob)
        {
            List<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener : this.listeners)
            {
                if (((MegaEvolvePokemobTrigger.Instance) listener.getCriterionInstance()).test(player, pokemob))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, MegaEvolvePokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, MegaEvolvePokemobTrigger.Listeners> newHashMap();

    public MegaEvolvePokemobTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
    {
        MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new MegaEvolvePokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<MegaEvolvePokemobTrigger.Instance> listener)
    {
        MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public MegaEvolvePokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String name = json.has("entry") ? json.get("entry").getAsString() : "";
        return new MegaEvolvePokemobTrigger.Instance(Database.getEntry(name));
    }

    public void trigger(EntityPlayerMP player, IPokemob pokemob)
    {
        MegaEvolvePokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, pokemob);
        }
    }
}
