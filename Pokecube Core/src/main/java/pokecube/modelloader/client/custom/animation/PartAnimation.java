package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.PartInfo;
import thut.api.maths.Vector3;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;

public class PartAnimation
{
    public final String       partName;
    public boolean            isHeadRoot  = false;
    public PartInfo           info;
    public Vector3            initialOffset;
    public Vector4            initialRot;
    public Vector3            initialScale;
    public ArrayList<Vector3> offsets;
    public ArrayList<Vector5> rotations;
    float                     partialTime = 0;
    float                     lastTime    = 0;
    Vector4                   empty       = new Vector4();

    public PartAnimation(String name)
    {
        partName = name;
    }

    public void doAnimation(Entity entity, IExtendedModelPart part, float partialTick)
    {
        if (rotations == null || rotations.isEmpty())
        {
            part.setPreRotations(empty);
            part.setPostRotations(empty);
            return;
        }
        Vector5 next = rotations.get(info.tick % rotations.size());
        Vector5 prev;
        if (rotations.size() > 1)
        {
            prev = rotations.get((info.tick + 1) % rotations.size());
        }
        else
        {
            prev = next;
        }
        float partial = partialTick;

        if (next != null && (prev != next || rotations.size() == 1))
        {
            int time = Math.abs(next.time - prev.time);
            if (time == 0)
            {
                time = next.time;
            }
            float diff = (partialTick) / (time);
            if (lastTime == partialTick) diff = 0;
            partial = partialTime + diff;
            partialTime = partial;

            Vector5 rot = prev.interpolate(next, partial, true);
            part.setPreRotations(rot.rotations);

            if (partial > 1)
            {
                info.tick++;
                if (info.tick >= rotations.size())
                {
                    info.tick = 0;
                }
                next = rotations.get(info.tick);
                if (rotations.size() > 1)
                {
                    prev = rotations.get((info.tick + 1) % rotations.size());
                }
                else
                {
                    prev = next;
                }
                partialTime = 0;
            }
        }
        else
        {
            Vector4 rot = next.rotations.copy();
            part.setPreRotations(rot);
        }
        lastTime = partialTick;
    }
}
