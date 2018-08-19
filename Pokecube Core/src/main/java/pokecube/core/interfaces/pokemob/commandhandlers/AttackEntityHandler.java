package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class AttackEntityHandler extends DefaultHandler
{
    public int targetId;

    public AttackEntityHandler()
    {
    }

    public AttackEntityHandler(Integer targetId)
    {
        this.targetId = targetId;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        World world = pokemob.getEntity().getEntityWorld();
        Entity target = PokecubeMod.core.getEntityProvider().getEntity(world, targetId, true);
        if (target == null || !(target instanceof EntityLivingBase))
        {
            if (target == null) throw new IllegalArgumentException("Target Mob cannot be null!");
            else throw new IllegalArgumentException("Invalid target: " + target);
        }
        String moveName = "";
        int currentMove = pokemob.getMoveIndex();
        MinecraftForge.EVENT_BUS.post(new CommandAttackEvent(pokemob.getEntity(), target));
        if (currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            moveName = MovesUtils.getUnlocalizedMove(move.getName());
            if (move.isSelfMove())
            {
                pokemob.executeMove(pokemob.getEntity(), null, 0);
            }
            else
            {
                ITextComponent mess = new TextComponentTranslation("pokemob.command.attack",
                        pokemob.getPokemonDisplayName(), target.getDisplayName(),
                        new TextComponentTranslation(moveName));
                if (fromOwner()) pokemob.displayMessageToOwner(mess);
                pokemob.getEntity().setAttackTarget((EntityLivingBase) target);
                if (target instanceof EntityLiving) ((EntityLiving) target).setAttackTarget(pokemob.getEntity());
            }
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeInt(targetId);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        targetId = buf.readInt();
    }
}
