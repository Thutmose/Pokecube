package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;

public class StorageAIBuilder implements StorableAIBuilder<AIStoreStuff>
{

    @Override
    public AIStoreStuff createFromNBT(EntityLiving living, NBTTagCompound data)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<AIStoreStuff> getType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRoutineName()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
