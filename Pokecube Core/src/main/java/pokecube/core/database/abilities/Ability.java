package pokecube.core.database.abilities;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public abstract class Ability {
	
	public final String name;
	
	public Ability(String name)
	{
		this.name = name;
	}
	
	public abstract void onUpdate(IPokemob mob);
	
	public abstract void onMoveUse(IPokemob mob, MovePacket move);
	
	public abstract void onAgress(IPokemob mob, EntityLivingBase target);
	
	@Override
	public String toString()
	{
		return name;
	}
}
