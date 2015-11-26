package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.PartInfo;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;

public class PartAnimation
{
	public final String partName;
	public PartInfo					info;
	public ArrayList<Vector5>		positions;
	float partialTime = 0;
	float lastTime = 0;
	Vector4 empty = new Vector4();
	
	public PartAnimation(String name)
	{
		partName = name;
	}

	public void doAnimation(Entity entity, IExtendedModelPart part, float partialTick)
	{
		if(positions==null || positions.isEmpty())
		{
			part.setPreRotations(empty);
			part.setPostRotations(empty);
			return;
		}
		Vector5 next = positions.get(info.tick % positions.size());
		Vector5 prev;
		if (positions.size() > 1)
		{
			prev = positions.get((info.tick + 1) % positions.size());
		}
		else
		{
			prev = next;
		}
		float partial = partialTick;

		if (next != null && (prev != next || positions.size() == 1))
		{
			int time = Math.abs(next.time - prev.time);
			if (time == 0)
			{
				time = next.time;
			}
			float diff = (partialTick) / (time);
			if(lastTime==partialTick)
				diff = 0;
			partial = partialTime + diff;
			partialTime = partial;
			
			Vector5 rot = prev.interpolate(next, partial, true);
			if (partName.contains("head"))
			{
				Vector4 dir = new Vector4(0, 1, 0, -entity.getRotationYawHead() + entity.rotationYaw % 360);
				part.setPostRotations(dir);
//				new Exception().printStackTrace();//TODO see if this is ever called
			}

			part.setPreRotations(rot.rotations);
			
			if (partial > 1)
			{
				info.tick++;
				if (info.tick >= positions.size())
				{
					info.tick = 0;
				}
				next = positions.get(info.tick);
				if (positions.size() > 1)
				{
					prev = positions.get((info.tick + 1) % positions.size());
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
			if (partName.contains("head"))
			{
				Vector4 dir = new Vector4(0, 1, 0, -entity.getRotationYawHead() + entity.rotationYaw % 360);
				part.setPostRotations(dir);
//                new Exception().printStackTrace();//TODO see if this is ever called
			}
			part.setPreRotations(rot);
		}
		lastTime = partialTick;
	}
}
