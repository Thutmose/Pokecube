package pokecube.core.moves;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.animations.EntityMoveUse;

public class MoveQueue
{
    public static class MoveQueuer
    {
        Map<World, MoveQueue> queues = Maps.newHashMap();

        public MoveQueuer()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        public void queueMove(EntityMoveUse move)
        {
            MoveQueue queue = queues.get(move.worldObj);
            if (queue == null) throw new NullPointerException("why is world queue null?");
            if (move.getUser() != null) queue.moves.add(move);
        }

        @SubscribeEvent
        public void load(WorldEvent.Load evt)
        {
            queues.put(evt.getWorld(), new MoveQueue(evt.getWorld()));
        }

        @SubscribeEvent
        public void unload(WorldEvent.Unload evt)
        {
            queues.remove(evt.getWorld());
        }

        @SubscribeEvent
        public void tick(WorldTickEvent evt)
        {
            if (evt.phase == Phase.END && evt.side == Side.SERVER)
            {
                MoveQueue queue = queues.get(evt.world);
                if (queue == null) throw new NullPointerException("why is world queue null?");
                queue.executeMoves();
            }
        }
    }

    public List<EntityMoveUse> moves = Lists.newArrayList();
    final World                world;

    public MoveQueue(World world)
    {
        this.world = world;
    }

    public synchronized void executeMoves()
    {
        Collections.sort(moves, new Comparator<EntityMoveUse>()
        {
            @Override
            public int compare(EntityMoveUse o1, EntityMoveUse o2)
            {
                IPokemob user1 = (IPokemob) o1.getUser();
                IPokemob user2 = (IPokemob) o2.getUser();
                int speed1 = user1 == null ? 0 : user1.getStat(Stats.VIT, true);
                int speed2 = user2 == null ? 0 : user2.getStat(Stats.VIT, true);
                // TODO also factor in move priority here.
                return speed1 - speed2;
            }
        });
        for (EntityMoveUse move : moves)
        {
            if (move.getUser() == null) continue;
            boolean toUse = !move.getUser().isDead;
            if (toUse && move.getUser() instanceof EntityLivingBase)
            {
                toUse = ((EntityLivingBase) move.getUser()).getHealth() >= 1;
            }
            if (toUse)
            {
                world.spawnEntityInWorld(move);
                move.getMove().applyHungerCost((IPokemob) move.getUser());
                MovesUtils.displayMoveMessages((IPokemob) move.getUser(), move.getTarget(), move.getMove().name);
            }
        }
        moves.clear();
    }

}
