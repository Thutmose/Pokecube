package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class MoveWorldAction extends Event
{
    private final IPokemob  user;
    private final Vector3   location;
    private final Move_Base move;

    MoveWorldAction(Move_Base move, IPokemob user, Vector3 location)
    {
        this.user = user;
        this.location = location;
        this.move = move;
    }

    public Move_Base getMove()
    {
        return move;
    }

    public IPokemob getUser()
    {
        return user;
    }

    public Vector3 getLocation()
    {
        return location;
    }

    /** This event is called to actually do the world action, it is handled by
     * an event handler if PreAction is not canceled. The default actions for
     * this will be set to lowest priority, and not recieve canceled, so if you
     * want to interfer, make sure to cancel this event. */
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
