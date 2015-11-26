package pokecube.core.moves.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;

public class Move_Teleport extends Move_Utility {

	public Move_Teleport(String name) 
	{
		super(name);
	}
	IPokemob teleporter;

	@Override
	public void attack(IPokemob attacker, Entity attacked, float f)
	{
		Entity target = ((EntityCreature) attacker).getAttackTarget();
		boolean angry = attacker.getPokemonAIState(IPokemob.ANGRY);
		((EntityCreature) attacker).setAttackTarget(null);
		attacker.setPokemonAIState(IPokemob.ANGRY, false);
		
		if (attacked instanceof EntityLiving) {
			((EntityLiving) attacked).setAttackTarget(null);
		}
		if (attacked instanceof EntityCreature) {
			((EntityCreature) attacker).setAttackTarget(null);
		}
		if (attacked instanceof IPokemob) {
			((IPokemob) attacked).setPokemonAIState(IPokemob.ANGRY, false);
		}
		if(attacker instanceof IPokemob && angry)
		{
			teleporter = ((IPokemob)attacker);
			if(((IPokemob)attacker).getPokemonAIState(IPokemob.TAMED))
				((IPokemob)attacker).returnToPokecube();
			else
				teleportRandomly();
		}
		
		if(attacker instanceof IPokemob && attacker.getPokemonAIState(IPokemob.TAMED) && !angry)// && PokecubeMod.hardMode)
		{
			teleporter = ((IPokemob)attacker);
		//	System.out.println(teleporter);
			if(target == null)
			{
				if(attacker.getPokemonOwner() instanceof EntityPlayer && ((EntityLivingBase)attacker).isServerWorld())
				{
					EventsHandler.recallAllPokemobsExcluding((EntityPlayer) attacker.getPokemonOwner(), (IPokemob) null);
				
					PokecubeClientPacket packet = new PokecubeClientPacket(new byte[]{(byte)13});
					PokecubePacketHandler.sendToClient(packet, (EntityPlayer) attacker.getPokemonOwner());
				}
			}
		}
	}
	
	void teleportRandomly()//TODO move the stuff from EntityMovesPokemob to here
	{
		
	}

}
