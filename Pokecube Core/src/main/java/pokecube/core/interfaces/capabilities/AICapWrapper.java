package pokecube.core.interfaces.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.PokecubeCore;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.ai.IAIMob;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ILogicRunnable;

public class AICapWrapper implements IAIMob, ICapabilitySerializable<NBTTagCompound>
{
    public static final ResourceLocation AICAP = new ResourceLocation(PokecubeCore.ID, "ai");

    final DefaultPokemob                 wrapped;
    private NBTTagCompound               read  = null;

    public AICapWrapper(DefaultPokemob wrapped)
    {
        this.wrapped = wrapped;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void init()
    {
        if (read == null) return;
        NBTTagCompound nbt = read;
        read = null;
        NBTTagCompound aiTag = nbt.getCompoundTag("ai");
        NBTTagCompound logicTag = nbt.getCompoundTag("logic");
        for (IAIRunnable runnable : getAI().aiTasks)
        {
            if (runnable instanceof INBTSerializable)
            {
                if (aiTag.hasKey(runnable.getIdentifier()))
                {
                    ((INBTSerializable) runnable).deserializeNBT(aiTag.getTag(runnable.getIdentifier()));
                }
            }
        }
        for (ILogicRunnable runnable : getAI().aiLogic)
        {
            if (runnable instanceof INBTSerializable<?>)
            {
                if (logicTag.hasKey(runnable.getIdentifier()))
                {
                    ((INBTSerializable) runnable).deserializeNBT(aiTag.getTag(runnable.getIdentifier()));
                }
            }
        }
    }

    @Override
    public AIStuff getAI()
    {
        return wrapped.getAI();
    }

    @Override
    public boolean selfManaged()
    {
        return wrapped.selfManaged();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == THUTMOBAI;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (hasCapability(capability, facing)) return THUTMOBAI.cast(this);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound savedAI = new NBTTagCompound();
        NBTTagCompound savedLogic = new NBTTagCompound();
        for (IAIRunnable runnable : getAI().aiTasks)
        {
            if (runnable instanceof INBTSerializable<?>)
            {
                NBTBase base = INBTSerializable.class.cast(runnable).serializeNBT();
                savedAI.setTag(runnable.getIdentifier(), base);
            }
        }
        for (ILogicRunnable runnable : getAI().aiLogic)
        {
            if (runnable instanceof INBTSerializable<?>)
            {
                NBTBase base = INBTSerializable.class.cast(runnable).serializeNBT();
                savedLogic.setTag(runnable.getIdentifier(), base);
            }
        }
        tag.setTag("ai", savedAI);
        tag.setTag("logic", savedLogic);
        if (read != null && savedAI.hasNoTags() && savedLogic.hasNoTags())
        {
            tag = read;
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        read = nbt;
    }

}
