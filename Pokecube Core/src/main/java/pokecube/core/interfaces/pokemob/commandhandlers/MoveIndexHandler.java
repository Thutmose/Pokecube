package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class MoveIndexHandler extends DefaultHandler
{
    public byte index;

    public MoveIndexHandler()
    {
    }

    public MoveIndexHandler(Byte index)
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
        super.writeToBuf(buf);
        buf.writeByte(index);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        index = buf.readByte();
    }
}
