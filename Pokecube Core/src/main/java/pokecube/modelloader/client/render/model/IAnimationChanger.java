package pokecube.modelloader.client.render.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public interface IAnimationChanger
{
    String modifyAnimation(EntityLiving entity, float partialTicks, String phase);

    int getColourForPart(String partIdentifier, Entity entity, int default_);

    boolean isPartHidden(String part, Entity entity, boolean default_);

    boolean isHeadRoot(String part);

    /** headcap => yaw, headcap1 => pitch<br>
     * { headcap, -headcap, headDir, headcap1, -headcap1, headDir1} */
    float[] getHeadInfo();
}
