package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.api.maths.Vector3;

public class AttackLocationHandler extends DefaultHandler
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
        CommandAttackEvent evt = new CommandAttackEvent(pokemob.getEntity(), null);
        MinecraftForge.EVENT_BUS.post(evt);

        if (!evt.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            // Send move use message first.
            ITextComponent mess = new TextComponentTranslation("pokemob.action.usemove",
                    pokemob.getPokemonDisplayName(),
                    new TextComponentTranslation(MovesUtils.getUnlocalizedMove(move.getName())));
            if (fromOwner()) pokemob.displayMessageToOwner(mess);

            // If too hungry, send message about that.
            if (pokemob.getHungerTime() > 0)
            {
                mess = new TextComponentTranslation("pokemob.action.hungry", pokemob.getPokemonDisplayName());
                if (fromOwner()) pokemob.displayMessageToOwner(mess);
                return;
            }

            // Otherwise set the location for execution of move.
            pokemob.setCombatState(CombatStates.NEWEXECUTEMOVE, true);
            pokemob.setTargetPos(location);
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        location.writeToBuff(buf);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        location = Vector3.readFromBuff(buf);
    }
}
