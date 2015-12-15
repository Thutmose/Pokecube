package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;

public interface StorableAIBuilder<T extends StorableAI>
{
	/**
	 * Create a new AI routine based on the data in the NBTTagCompound.
	 * This makes it possible for the routines themselves to be immutable.
	 * 
	 * @return The created AI routine, or <i>null</i> if it's not possible.
	 */
	T createFromNBT(EntityLiving living, NBTTagCompound data);
	
	/**
	 * Return the class type this builder is creating (for registration).
	 */
	Class<T> getType();
	
	/**
	 * Return the globally unique ID of the AI routines this builder is making.
	 * This should be something like modID + ":" + unique identifier.
	 */
	String getRoutineName();
}
