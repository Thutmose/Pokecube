package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;

public class GuardAIBuilder implements StorableAIBuilder<GuardAI>
{
	@Override
	public GuardAI createFromNBT(EntityLiving living, NBTTagCompound data)
	{
		return GuardAI.createFromNBT(living, data);
	}

	@Override
	public Class<GuardAI> getType()
	{
		return GuardAI.class;
	}

	@Override
	public String getRoutineName()
	{
		return "pokecube_tech:Guard";
	}

}
