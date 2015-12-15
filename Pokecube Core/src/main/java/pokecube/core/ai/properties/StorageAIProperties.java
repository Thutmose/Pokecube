package pokecube.core.ai.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class StorageAIProperties implements IExtendedEntityProperties
{
    public static final String EXT_PROP_NAME = "pokecube:StorageAIProp";
    
    public static final void register(EntityLiving entity)
    {
        entity.registerExtendedProperties(EXT_PROP_NAME, new StorageAIProperties());
    }
    
    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(Entity entity, World world)
    {
        // TODO Auto-generated method stub

    }

}
