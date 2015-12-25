package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;
import thut.api.maths.Vector3;

/** This class applies the tabula style animations to models consisting of
 * IExtendedModelPart parts.
 * 
 * @author Thutmose */
public class AnimationHelper
{
    /** Apples tabula Animation animation to the passed in part.
     * 
     * @param animation
     * @param entity
     * @param partName
     * @param part
     * @param partialTick
     * @return */
    public static boolean doAnimation(Animation animation, Entity entity, String partName, IExtendedModelPart part,
            float partialTick)
    {
        ArrayList<AnimationComponent> components = animation.sets.get(partName);
        if (animation.getLength() < 0)
        {
            animation.initLength();
        }
        int animationLength = animation.getLength();
        boolean animate = false;
        Vector3 temp = Vector3.getNewVectorFromPool();
        float x = 0, y = 0, z = 0;

        if (components != null) for (AnimationComponent component : components)
        {
            float time = entity.worldObj.getTotalWorldTime() + partialTick;
            time = time % animationLength;
            if (time >= component.startKey)
            {
                animate = true;
                float componentTimer = time - component.startKey;

                if (componentTimer > component.length)
                {
                    componentTimer = component.length;
                }
                temp.addTo(component.posChange[0] / component.length * componentTimer + component.posOffset[0],
                        component.posChange[1] / component.length * componentTimer + component.posOffset[1],
                        component.posChange[2] / component.length * componentTimer + component.posOffset[2]);
                x += (float) (component.rotChange[0] / component.length * componentTimer + component.rotOffset[0]);
                y += (float) (component.rotChange[1] / component.length * componentTimer + component.rotOffset[1]);
                z += (float) (component.rotChange[2] / component.length * componentTimer + component.rotOffset[2]);
            }
        }
        if (animate)
        {
            part.setPreTranslations(temp);
            Vector4 angle = null;
            if (z != 0)
            {
                angle = new Vector4(0, 0, 1, z);
            }
            if (y != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(0, 1, 0, y));
                }
                else
                {
                    angle = new Vector4(0, 1, 0, y);
                }
            }
            if (x != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(1, 0, 0, x));
                }
                else
                {
                    angle = new Vector4(1, 0, 0, x);
                }
            }
            if (angle != null) part.setPreRotations(angle);
        }
        temp.freeVectorFromPool();

        return animate;
    }
}
