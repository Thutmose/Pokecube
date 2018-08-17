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
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class UseMoveTrigger implements ICriterionTrigger<UseMoveTrigger.Instance>
{
    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "use_move");

    public static class Instance extends AbstractCriterionInstance
    {
        String   attack;
        PokeType type;
        int      power;
        float    damage;

        public Instance(MovePacket move)
        {
            super(ID);
            this.attack = move.attack;
            this.type = move.attackType;
            this.power = move.PWR;
        }

        public boolean test(EntityPlayerMP player, MovePacket packet)
        {
            return packet.attack.equals(attack);
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                       playerAdvancements;
        private final Set<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player, MovePacket packet)
        {
            List<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener : this.listeners)
            {
                if (((UseMoveTrigger.Instance) listener.getCriterionInstance()).test(player, packet))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<UseMoveTrigger.Instance>> newArrayList();
                    }

                    list.add(listener);
                }
            }
            if (list != null)
            {
                for (ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

    private final Map<PlayerAdvancements, UseMoveTrigger.Listeners> listeners = Maps.<PlayerAdvancements, UseMoveTrigger.Listeners> newHashMap();

    public UseMoveTrigger()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
    {
        UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new UseMoveTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn,
            ICriterionTrigger.Listener<UseMoveTrigger.Instance> listener)
    {
        UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
    public UseMoveTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String attack = json.get("move").getAsString();
        // TODO get this done better.
        MovePacket packet = new MovePacket(null, null, MovesUtils.getMoveFromName(attack));
        return new UseMoveTrigger.Instance(packet);
    }

    public void trigger(EntityPlayerMP player, MovePacket packet)
    {
        UseMoveTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.trigger(player, packet);
        }
    }
}
