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

public class BreedPokemobTrigger implements ICriterionTrigger<BreedPokemobTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "breed");

    public static class Instance extends AbstractCriterionInstance
    {
        final PokedexEntry mate1;
        final PokedexEntry mate2;

        public Instance(PokedexEntry mate1, PokedexEntry mate2)
        {
            super(ID);
            this.mate1 = mate1 != null ? mate1 : Database.missingno;
            this.mate2 = mate2 != null ? mate2 : Database.missingno;
        }

        public boolean test(EntityPlayerMP player, IPokemob first, IPokemob second)
        {
            if (!(first.getPokemonOwner() == player || second.getPokemonOwner() == player)) return false;

            IPokemob firstmate = null;
            IPokemob secondmate = null;

            if (first.getPokedexEntry() == mate1)
            {
                firstmate = first;
                secondmate = second;
            }
            else if (first.getPokedexEntry() == mate2)
            {
                firstmate = second;
                secondmate = first;
            }

            boolean firstMatch = firstmate.getPokedexEntry() == mate1 || mate1 == Database.missingno;
            boolean secondMatch = secondmate.getPokedexEntry() == mate2 || mate2 == Database.missingno;

            return firstMatch && secondMatch;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, IPokemob first, IPokemob second)
        {
            List<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener : this.listeners)
            {
                if (((BreedPokemobTrigger.Instance) listener.getCriterionInstance()).test(player, first, second))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<BreedPokemobTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, BreedPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BreedPokemobTrigger.Listeners> newHashMap();

    public BreedPokemobTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
    {
        BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BreedPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BreedPokemobTrigger.Instance> listener)
    {
        BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public BreedPokemobTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String mate1 = json.has("mate1") ? json.get("mate1").getAsString() : "";
        String mate2 = json.has("mate2") ? json.get("mate2").getAsString() : "";
        return new BreedPokemobTrigger.Instance(Database.getEntry(mate1), Database.getEntry(mate2));
    }

    public void trigger(EntityPlayerMP player, IPokemob first, IPokemob second)
    {
        BreedPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, first, second);
        }
    }
}
