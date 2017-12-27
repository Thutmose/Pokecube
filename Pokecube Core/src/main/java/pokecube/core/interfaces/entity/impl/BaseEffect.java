package pokecube.core.interfaces.entity.impl;

import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;

public abstract class BaseEffect implements IOngoingEffect
{
    private final ResourceLocation id;
    private int                    duration;

    public BaseEffect(ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

    @Override
    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    @Override
    public ResourceLocation getID()
    {
        return id;
    }

}
