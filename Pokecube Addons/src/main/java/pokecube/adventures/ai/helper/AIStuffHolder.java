package pokecube.adventures.ai.helper;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.ai.IAIMob;

public class AIStuffHolder implements IAIMob, ICapabilityProvider
{
    final AIStuff stuff;

    public AIStuffHolder(EntityLiving entity)
    {
        this.stuff = new AIStuff(entity);
    }

    @Override
    public AIStuff getAI()
    {
        return stuff;
    }

    @Override
    public boolean selfManaged()
    {
        return false;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == IAIMob.THUTMOBAI;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return hasCapability(capability, facing) ? IAIMob.THUTMOBAI.cast(this) : null;
    }

}
