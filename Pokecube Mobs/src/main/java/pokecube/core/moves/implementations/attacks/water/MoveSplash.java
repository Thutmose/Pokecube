package pokecube.core.moves.implementations.attacks.water;

import net.minecraft.util.text.ITextComponent;
import pokecube.core.commands.CommandTools;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSplash extends Move_Basic
{

    public MoveSplash()
    {
        super("splash");
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        super.preAttack(packet);
        packet.denied = true;
        if (packet.attacked instanceof IPokemob)
        {
            ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.move.doesnt.affect", "red",
                    ((IPokemob) packet.attacked).getPokemonDisplayName().getFormattedText());
            packet.attacker.displayMessageToOwner(text);
            text = CommandTools.makeTranslatedMessage("pokemob.move.doesnt.affect", "green",
                    ((IPokemob) packet.attacked).getPokemonDisplayName().getFormattedText());
            ((IPokemob) packet.attacked).displayMessageToOwner(text);
        }
    }

}
