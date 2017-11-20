package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;

public class SwapMovesHandler implements IMobCommandHandler
{
    public byte indexA;
    public byte indexB;

    public SwapMovesHandler()
    {
    }

    public SwapMovesHandler(byte indexA, byte indexB)
    {
        this.indexA = indexA;
        this.indexB = indexB;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        String[] moves = pokemob.getMoves();
        String moveA = moves[indexA];
        String moveB = moves[indexB];
        moves[indexA] = moveB;
        moves[indexB] = moveA;
        pokemob.setMoves(moves);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        buf.writeByte(indexA);
        buf.writeByte(indexB);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        indexA = buf.readByte();
        indexB = buf.readByte();
    }

}
