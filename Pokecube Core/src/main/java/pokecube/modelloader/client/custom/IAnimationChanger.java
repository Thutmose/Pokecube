package pokecube.modelloader.client.custom;

import net.minecraft.entity.EntityLiving;

public interface IAnimationChanger
{
    String modifyAnimation(EntityLiving entity, float partialTicks, String phase);
}
