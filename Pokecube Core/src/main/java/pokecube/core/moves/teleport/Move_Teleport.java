package pokecube.core.moves.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;

public class Move_Teleport extends Move_Basic
{
    public Move_Teleport(String name)
    {
        super(name);
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        IPokemob attacker = packet.attacker;
        Entity attacked = packet.attacked;
        Entity target = ((EntityCreature) attacker).getAttackTarget();
        boolean angry = attacker.getPokemonAIState(IMoveConstants.ANGRY);
        ((EntityCreature) attacker).setAttackTarget(null);
        attacker.setPokemonAIState(IMoveConstants.ANGRY, false);

        if (attacked instanceof EntityLiving)
        {
            ((EntityLiving) attacked).setAttackTarget(null);
        }
        if (attacked instanceof EntityCreature)
        {
            ((EntityCreature) attacker).setAttackTarget(null);
        }
        if (attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).setPokemonAIState(IMoveConstants.ANGRY, false);
        }
        if (attacker instanceof IPokemob && attacker.getPokemonAIState(IMoveConstants.TAMED) && !angry)
        {
            if ((target == null && packet.attacked == null) || (packet.attacked == packet.attacker))
            {
                if (attacker.getPokemonOwner() instanceof EntityPlayer && ((EntityLivingBase) attacker).isServerWorld())
                {
                    EventsHandler.recallAllPokemobsExcluding((EntityPlayer) attacker.getPokemonOwner(),
                            (IPokemob) null);
                    PokecubeClientPacket mess = new PokecubeClientPacket(
                            new byte[] { PokecubeClientPacket.TELEPORTINDEX });
                    PokecubePacketHandler.sendToClient(mess, (EntityPlayer) attacker.getPokemonOwner());
                }
            }
        }
        super.postAttack(packet);
    }
}
