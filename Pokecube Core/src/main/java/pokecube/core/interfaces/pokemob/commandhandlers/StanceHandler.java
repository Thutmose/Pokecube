package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class StanceHandler extends DefaultHandler
{
    public static final byte BUTTONTOGGLESTAY  = 0;
    public static final byte BUTTONTOGGLEGUARD = 1;
    public static final byte BUTTONTOGGLESIT   = 2;

    boolean                  state;
    byte                     key;

    public StanceHandler()
    {
    }

    public StanceHandler(Boolean state, Byte key)
    {
        this.state = state;
        this.key = key;
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        if (key == BUTTONTOGGLEGUARD)
        {
            pokemob.setCombatState(CombatStates.GUARDING, !pokemob.getCombatState(CombatStates.GUARDING));
        }
        else if (key == BUTTONTOGGLESTAY)
        {
            boolean stay;
            pokemob.setGeneralState(GeneralStates.STAYING, stay = !pokemob.getGeneralState(GeneralStates.STAYING));
            IGuardAICapability guard = pokemob.getEntity().getCapability(EventsHandler.GUARDAI_CAP, null);
            if (stay)
            {
                Vector3 mid = Vector3.getNewVector().set(pokemob.getEntity());
                if (guard != null)
                {
                    guard.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
                    guard.getPrimaryTask().setPos(mid.getPos());
                }
            }
            else
            {
                if (guard != null) guard.getPrimaryTask().setActiveTime(TimePeriod.never);
            }
        }
        else if (key == BUTTONTOGGLESIT)
        {
            pokemob.setLogicState(LogicStates.SITTING, !pokemob.getLogicState(LogicStates.SITTING));
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeBoolean(state);
        buf.writeByte(key);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        state = buf.readBoolean();
        key = buf.readByte();
    }

}
