package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

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

        public static class Pre extends ActualMoveUse
        {
            public Pre(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        public static class Post extends ActualMoveUse
        {
            public Post(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
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
         * by an event handler if PreAction is not canceled. The default actions
         * for this will be set to lowest priority, and not recieve canceled, so
         * if you want to interfer, make sure to cancel this event. */
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
