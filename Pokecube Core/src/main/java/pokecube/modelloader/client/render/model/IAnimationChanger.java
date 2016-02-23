package pokecube.modelloader.client.render.model;

import net.minecraft.entity.EntityLiving;

public interface IAnimationChanger
{
    String modifyAnimation(EntityLiving entity, float partialTicks, String phase);
}
