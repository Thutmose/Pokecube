package pokecube.core.handlers.playerdata.advancements.criteria;

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

public class CatchPokemobTrigger implements ICriterionTrigger<CatchPokemobTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "catch");

    public static class Instance extends AbstractCriterionInstance
    {
        final PokedexEntry entry;

        public Instance(PokedexEntry entry)
        {
            super(ID);
            this.entry = entry;
        }

        public boolean test(EntityPlayerMP player, IPokemob pokemob)
        {
            return pokemob.getPokedexEntry() == entry && pokemob.getPokemonOwner() == player;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, IPokemob pokemob)
        {
            List<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener : this.listeners)
            {
                if (((CatchPokemobTrigger.Instance) listener.getCriterionInstance()).test(player, pokemob))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, CatchPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, CatchPokemobTrigger.Listeners> newHashMap();

    public CatchPokemobTrigger()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new CatchPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public CatchPokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String name = json.get("entry").getAsString();
        return new CatchPokemobTrigger.Instance(Database.getEntry(name));
    }

    public void trigger(EntityPlayerMP player, IPokemob pokemob)
    {
        CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, pokemob);
        }
    }
}
