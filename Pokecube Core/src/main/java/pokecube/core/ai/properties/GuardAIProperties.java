package pokecube.core.ai.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.GuardAIBuilder;

public class GuardAIProperties implements IExtendedEntityProperties
{
	public static final String EXT_PROP_NAME = "pokecube_tech:GuardAIProp";
	
	public static final void register(EntityLiving entity)
	{
		entity.registerExtendedProperties(EXT_PROP_NAME, new GuardAIProperties());
	}
	
	public static final GuardAIProperties get(EntityLiving entity)
	{
		return (GuardAIProperties)entity.getExtendedProperties(EXT_PROP_NAME);
	}

	private EntityLiving owner;
	private final GuardAIBuilder guardAIBuilder = new GuardAIBuilder();
	
	@Override
	public void saveNBTData(NBTTagCompound data)
	{
		if( null != owner )
		{
			for(Object taskEntryObj : owner.tasks.taskEntries)
			{
				EntityAITaskEntry taskEntry = (EntityAITaskEntry)taskEntryObj;
				if( taskEntry.action instanceof GuardAI )
				{
					NBTTagCompound properties = new NBTTagCompound();
					((GuardAI)taskEntry.action).writeToNBT(properties);
					properties.setInteger("AIPriority", taskEntry.priority);
					data.setTag(EXT_PROP_NAME, properties);
					break;
				}
			}
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound data)
	{
		NBTTagCompound properties = (NBTTagCompound)data.getTag(EXT_PROP_NAME);
		if( null != owner && null != properties )
		{
			int priority = properties.getInteger("AIPriority");
			GuardAI guardAITask = guardAIBuilder.createFromNBT(owner, properties);
			owner.tasks.addTask(priority, guardAITask);
		}
	}

	@Override
	public void init(Entity entity, World world)
	{
		// This call happens in the middle of constructing of an entity.
		// Nothing much to do here besides saving the entity for future reference.
		
		if( entity instanceof EntityLiving )
		{
			owner = (EntityLiving)entity;
		}
		else
		{
			throw new IllegalArgumentException("Initializing GuardAIProperties with a non-EntityLiving class (" + entity.getClass().toString() + ")");
		}
	}

}
