package pokecube.core.interfaces.pokemob;

import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public interface IHasCommands
{
    public static interface IMobCommandHandler
    {
        void handleCommand(IPokemob pokemob) throws Exception;

        void writeToBuf(ByteBuf buf);

        void readFromBuf(ByteBuf buf);
    }

    public static enum Command
    {
        //@formatter:off
        ATTACKENTITY, 
        ATTACKLOCATION, 
        CHANGEMOVEINDEX, 
        CHANGEFORM, 
        MOVETO,
        SWAPMOVES; 
        //@formatter:on
    }

    public static final Map<Command, Class<? extends IMobCommandHandler>> COMMANDHANDLERS = Maps.newHashMap();

    default void handleCommand(Command command, IMobCommandHandler handler)
    {
        IPokemob pokemob = (IPokemob) this;
        try
        {
            handler.handleCommand(pokemob);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE,
                    "Error Handling command for type " + command + " for mob " + pokemob.getEntity(), e);
        }
    }
}
