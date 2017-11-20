package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AttackLocationHandler implements IMobCommandHandler
{
    Vector3 location;

    public AttackLocationHandler()
    {
    }

    public AttackLocationHandler(Vector3 location)
    {
        this.location = location.copy();
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        int currentMove = pokemob.getMoveIndex();
        if (currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            ITextComponent mess = new TextComponentTranslation("pokemob.action.usemove",
                    pokemob.getPokemonDisplayName(),
                    new TextComponentTranslation(MovesUtils.getUnlocalizedMove(move.getName())));
            pokemob.displayMessageToOwner(mess);
            pokemob.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, true);
            ((PokemobAIUtilityMove) pokemob.getUtilityMoveAI()).destination = location;
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        location.writeToBuff(buf);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        location = Vector3.readFromBuff(buf);
    }

}
