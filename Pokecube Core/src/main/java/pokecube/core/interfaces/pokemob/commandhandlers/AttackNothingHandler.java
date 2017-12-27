package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;

public class AttackNothingHandler implements IMobCommandHandler
{

    public AttackNothingHandler()
    {
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        MinecraftForge.EVENT_BUS.post(new CommandAttackEvent(pokemob.getEntity(), null));
        pokemob.executeMove(pokemob.getEntity(), null, 0);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
    }

}
