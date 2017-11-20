package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;

public class MoveIndexHandler implements IMobCommandHandler
{
    public byte index;

    public MoveIndexHandler()
    {
    }

    public MoveIndexHandler(byte index)
    {
        this.index = index;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        pokemob.setMoveIndex(index);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        buf.writeByte(index);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        index = buf.readByte();
    }

}
