package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.moves.MovesUtils;

public class AttackEntityHandler implements IMobCommandHandler
{
    public int targetId;

    public AttackEntityHandler()
    {
    }

    public AttackEntityHandler(int targetId)
    {
        this.targetId = targetId;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        World world = pokemob.getEntity().getEntityWorld();
        Entity target = PokecubeMod.core.getEntityProvider().getEntity(world, targetId, true);
        if (target == null) throw new IllegalArgumentException("Target Mob cannot be null!");
        String moveName = "";
        int currentMove = pokemob.getMoveIndex();
        if (currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            moveName = MovesUtils.getUnlocalizedMove(move.getName());
            ITextComponent mess = new TextComponentTranslation("pokemob.command.attack",
                    pokemob.getPokemonDisplayName(), target.getDisplayName(), new TextComponentTranslation(moveName));
            pokemob.displayMessageToOwner(mess);
            pokemob.executeMove(target, null, 0);
        }
        // Initiate combat instead of using move.
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        buf.writeInt(targetId);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        targetId = buf.readInt();
    }

}
