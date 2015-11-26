package pokecube.core.ai.utils;

import net.minecraft.nbt.NBTTagCompound;

public interface StorableAI
{
	/**
	 * Serialize the data of this AI routine to an NBT compound.
	 * The routine itself should be immutable, though there's no way to
	 * enforce that restriction at compile time.
	 */
	void writeToNBT(NBTTagCompound data);
}
