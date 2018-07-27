package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class StanceHandler implements IMobCommandHandler
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
            pokemob.setPokemonAIState(IMoveConstants.GUARDING, !pokemob.getPokemonAIState(IMoveConstants.GUARDING));
        }
        else if (key == BUTTONTOGGLESTAY)
        {
            boolean stay;
            pokemob.setPokemonAIState(IMoveConstants.STAYING,
                    stay = !pokemob.getPokemonAIState(IMoveConstants.STAYING));
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
                if (guard != null) guard.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
            }
        }
        else if (key == BUTTONTOGGLESIT)
        {
            pokemob.setPokemonAIState(IMoveConstants.SITTING, !pokemob.getPokemonAIState(IMoveConstants.SITTING));
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        buf.writeBoolean(state);
        buf.writeByte(key);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        state = buf.readBoolean();
        key = buf.readByte();
    }

}
