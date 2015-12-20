package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;

public class StorageAIBuilder implements StorableAIBuilder<AIStoreStuff>
{

    @Override
    public AIStoreStuff createFromNBT(EntityLiving living, NBTTagCompound data)
    {
        return AIStoreStuff.createFromNBT(living, data);
    }

    @Override
    public Class<AIStoreStuff> getType()
    {
        return AIStoreStuff.class;
    }

    @Override
    public String getRoutineName()
    {
        return "pokecube:store";
    }

}
