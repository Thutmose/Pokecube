package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class AttackEvent extends Event
{
    public final MovePacket moveInfo;

    public AttackEvent(MovePacket moveInfo)
    {
        this.moveInfo = moveInfo;
    }
}
