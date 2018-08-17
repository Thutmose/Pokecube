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

public class BeatLeaderTrigger implements ICriterionTrigger<BeatLeaderTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeAdv.ID, "beat_leader");

    public static class Instance extends AbstractCriterionInstance
    {
        public Instance()
        {
            super(ID);
        }

        public boolean test(EntityPlayerMP player, EntityTrainer defeated)
        {
            return (defeated instanceof EntityLeader);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<ICriterionTrigger.Listener<BeatLeaderTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<BeatLeaderTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, EntityTrainer defeated)
        {
            List<ICriterionTrigger.Listener<BeatLeaderTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener : this.listeners)
            {
                if (((BeatLeaderTrigger.Instance) listener.getCriterionInstance()).test(player, defeated))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<BeatLeaderTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, BeatLeaderTrigger.Listeners> listeners = Maps.<PlayerAdvancements, BeatLeaderTrigger.Listeners> newHashMap();

    public BeatLeaderTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
    {
        BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new BeatLeaderTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<BeatLeaderTrigger.Instance> listener)
    {
        BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public BeatLeaderTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        return new BeatLeaderTrigger.Instance();
    }

    public void trigger(EntityPlayerMP player, EntityTrainer defeated)
    {
        BeatLeaderTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, defeated);
        }
    }
}
