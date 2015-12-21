package pokecube.core.ai.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;
import pokecube.core.ai.utils.StorageAIBuilder;
import pokecube.core.interfaces.IPokemob;

public class StorageAIProperties implements IExtendedEntityProperties
{
    public static final String EXT_PROP_NAME = "pokecube:StorageAIProp";

    public static final void register(EntityLiving entity)
    {
        if (entity instanceof IPokemob) entity.registerExtendedProperties(EXT_PROP_NAME, new StorageAIProperties());
    }

    public static final StorageAIProperties get(EntityLiving entity)
    {
        return (StorageAIProperties) entity.getExtendedProperties(EXT_PROP_NAME);
    }

    private EntityLiving           owner;
    private final StorageAIBuilder aiBuilder = new StorageAIBuilder();
    AIStoreStuff                   ai;

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        if (null != owner && ai != null)
        {
            NBTTagCompound properties = new NBTTagCompound();
            ai.writeToNBT(properties);
            compound.setTag(EXT_PROP_NAME, properties);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = (NBTTagCompound) compound.getTag(EXT_PROP_NAME);

        if (owner != null && properties != null)
        {
            ai = aiBuilder.createFromNBT(owner, properties);
            PokemobAIThread.addAI(owner, ai);
        }
        else if(owner!=null && owner instanceof IPokemob)
        {
            ai = aiBuilder.createFromNBT(owner, new NBTTagCompound());
            PokemobAIThread.addAI(owner, ai);
        }
    }

    @Override
    public void init(Entity entity, World world)
    {
        if (entity instanceof EntityLiving)
        {
            owner = (EntityLiving) entity;
        }
        else
        {
            throw new IllegalArgumentException("Initializing GuardAIProperties with a non-EntityLiving class ("
                    + entity.getClass().toString() + ")");
        }
    }

}
