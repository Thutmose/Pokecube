package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

/** These events are fired on the
 * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
public class MoveUse extends Event
{
    final IPokemob  user;
    final Move_Base move;

    public MoveUse(IPokemob user, Move_Base move)
    {
        this.user = user;
        this.move = move;
    }

    public IPokemob getUser()
    {
        return user;
    }

    public Move_Base getMove()
    {
        return move;
    }

    public static class ActualMoveUse extends MoveUse
    {
        final Entity target;

        public ActualMoveUse(IPokemob user, Move_Base move, Entity target)
        {
            super(user, move);
            this.target = target;
        }

        public Entity getTarget()
        {
            return target;
        }

        @Cancelable
        /** This is called when the move entity is made to start using the move.
         * Cancelling this prevents the move from occurring.<br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        public static class Init extends ActualMoveUse
        {
            public Init(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        /** This is called during the pre move use method of the move
         * calculations <br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        public static class Pre extends ActualMoveUse
        {
            public Pre(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        /** This is called after the post move use.<br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        public static class Post extends ActualMoveUse
        {
            public Post(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }
    }

    public static class DuringUse extends MoveUse
    {
        private final boolean    fromUser;
        private final MovePacket packet;

        public DuringUse(MovePacket packet, boolean fromUser)
        {
            super(packet.attacker, packet.getMove());
            this.fromUser = fromUser;
            this.packet = packet;
        }

        public boolean isFromUser()
        {
            return fromUser;
        }

        public MovePacket getPacket()
        {
            return packet;
        }

        @Cancelable
        /** Cancelling this event prevents the default implementation from being
         * applied. <br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        public static class Pre extends DuringUse
        {
            public Pre(MovePacket packet, boolean fromUser)
            {
                super(packet, fromUser);
            }
        }

        @Cancelable
        /** Cancelling this event prevents the default implementation from being
         * applied. <br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        public static class Post extends DuringUse
        {
            public Post(MovePacket packet, boolean fromUser)
            {
                super(packet, fromUser);
            }
        }
    }

    public static class MoveWorldAction extends MoveUse
    {
        private final Vector3 location;

        MoveWorldAction(Move_Base move, IPokemob user, Vector3 location)
        {
            super(user, move);
            this.location = location;
        }

        public Vector3 getLocation()
        {
            return location;
        }

        /** This event is called to actually do the world action, it is handled
         * by an event handler if PreAction is not cancelled. The default
         * actions for this will be set to lowest priority, and not receive
         * cancelled, so if you want to interfere, make sure to cancel this
         * event.<br>
         * <br>
         * this is fired on the
         * {@link pokecube.core.interfaces.PokecubeMod#MOVE_BUS} */
        @Cancelable
        public static class OnAction extends MoveWorldAction
        {
            public OnAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        @Cancelable
        public static class PreAction extends MoveWorldAction
        {

            public PreAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }

        }

        public static class PostAction extends MoveWorldAction
        {
            public PostAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }
    }
}
