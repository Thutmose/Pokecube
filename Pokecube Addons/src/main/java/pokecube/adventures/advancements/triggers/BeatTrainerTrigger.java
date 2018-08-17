package pokecube.adventures.advancements.triggers;

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
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;

public class BeatTrainerTrigger implements ICriterionTrigger<BeatTrainerTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.ID, "beat_trainer");

    public static class Instance extends AbstractCriterionInstance
    {
        public Instance()
        {
            super(ID);
        }

        public boolean test(EntityPlayerMP player, EntityTrainer defeated)
        {
            return !(defeated instanceof EntityLeader);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                           playerAdvancements;
        private final Set<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, EntityTrainer defeated)
        {
            List<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener : this.listeners)
            {
                if (((BeatTrainerTrigger.Instance) listener.getCriterionInstance()).test(player, defeated))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<BeatTrainerTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, BeatTrainerTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BeatTrainerTrigger.Listeners> newHashMap();

    public BeatTrainerTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
    {
        BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BeatTrainerTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BeatTrainerTrigger.Instance> listener)
    {
        BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public BeatTrainerTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        return new BeatTrainerTrigger.Instance();
    }

    public void trigger(EntityPlayerMP player, EntityTrainer defeated)
    {
        BeatTrainerTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, defeated);
        }
    }
}
