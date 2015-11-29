package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.Entity;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;

public class AnimationBipedWalk extends ModelAnimation
{
	/** Left leg */
	public HashMap<String, ArrayList<Vector5>>	positionsL	= new HashMap<String, ArrayList<Vector5>>();
	/** Right leg */
	public HashMap<String, ArrayList<Vector5>>	positionsR	= new HashMap<String, ArrayList<Vector5>>();
	/** Left arm */
	public HashMap<String, ArrayList<Vector5>>	positionsLA	= new HashMap<String, ArrayList<Vector5>>();
	/** Right arm */
	public HashMap<String, ArrayList<Vector5>>	positionsRA	= new HashMap<String, ArrayList<Vector5>>();

	public HashSet<String>	namesL	= new HashSet<String>();
	public HashSet<String>	namesR	= new HashSet<String>();
	public HashSet<String>	namesLA	= new HashSet<String>();
	public HashSet<String>	namesRA	= new HashSet<String>();

	public HashMap<String, Float>	partialTimes	= new HashMap<String, Float>();
	private int[]					ticks			= new int[4];
	private float					lastTick;
	
	public float angleLegs = 20;
	public float angleArms = 20;

	public boolean doAnimation(Entity entity, String partName, IExtendedModelPart part, float partialTick)
	{
		if(partialTick!=lastTick)
			lastTick = partialTick;
		if (namesL.contains(partName))
		{
			if (!partialTimes.containsKey(part.getName())) partialTimes.put(part.getName(), (float) 0);
			ticks[0] = doAnimation(entity, part, partialTick, positionsL.get(partName), ticks[0],
					partialTimes.get(part.getName()));
			return true;
		}
		if (namesR.contains(partName))
		{
			if (!partialTimes.containsKey(part.getName())) partialTimes.put(part.getName(), (float) 0);
			ticks[1] = doAnimation(entity, part, partialTick, positionsR.get(partName), ticks[1],
					partialTimes.get(part.getName()));
			return true;
		}
		if (namesLA.contains(partName))
		{
			if (!partialTimes.containsKey(part.getName())) partialTimes.put(part.getName(), (float) 0);
			ticks[2] = doAnimation(entity, part, partialTick, positionsLA.get(partName), ticks[2],
					partialTimes.get(part.getName()));
			return true;
		}
		if (namesRA.contains(partName))
		{
			if (!partialTimes.containsKey(part.getName())) partialTimes.put(part.getName(), (float) 0);
			ticks[3] = doAnimation(entity, part, partialTick, positionsRA.get(partName), ticks[3],
					partialTimes.get(part.getName()));
			return true;
		}

		return super.doAnimation(entity, partName, part, partialTick);
	}

	public int doAnimation(Entity entity, IExtendedModelPart part, float partialTick, ArrayList<Vector5> positions,
			int tick, float partialTime)
	{
		if (positions == null || positions.isEmpty()) return tick;
		Vector5 next = positions.get(tick % positions.size());
		Vector5 prev;
		if (positions.size() > 1)
		{
			prev = positions.get((tick + 1) % positions.size());
		}
		else
		{
			prev = next;
		}
		float partial = lastTick;

		if (next != null && (prev != next || positions.size() == 1))
		{
			int time = Math.abs(next.time - prev.time);
			if (time == 0)
			{
				time = next.time;
			}
			float diff = partial / (time);
			partial = partialTime + diff;
			partialTime = partial;

			Vector5 rot = prev.interpolate(next, partial, true);
			part.setPreRotations(rot.rotations);

			if (partial > 1)
			{
				tick++;
				if (tick >= positions.size())
				{
					tick = 0;
				}
				next = positions.get(tick);
				if (positions.size() > 1)
				{
					prev = positions.get((tick + 1) % positions.size());
				}
				else
				{
					prev = next;
				}
				partialTime = diff;
			}
		}
		else
		{
			Vector4 rot = next.rotations.copy();
			part.setPreRotations(rot);
		}
		partialTimes.put(part.getName(), partialTime);
		return tick;
	}

	public void initAnimation(HashSet<IExtendedModelPart> ll, HashSet<IExtendedModelPart> rl,HashSet<IExtendedModelPart> la,HashSet<IExtendedModelPart> ra, int speed)
	{
		namesL.clear();
		namesR.clear();
		namesLA.clear();
		namesRA.clear();
		positionsL.clear();
		positionsR.clear();
		positionsLA.clear();
		positionsRA.clear();
		for (IExtendedModelPart p : ll)
		{
			namesL.add(p.getName());
			initPositions(speed, positionsL, p.getName());
		}
		for (IExtendedModelPart p : rl)
		{
			namesR.add(p.getName());
			initPositions(speed, positionsR, p.getName());
		}
		for (IExtendedModelPart p : la)
		{
			namesLA.add(p.getName());
			initPositions(speed, positionsLA, p.getName());
		}
		for (IExtendedModelPart p : ra)
		{
			namesRA.add(p.getName());
			initPositions(speed, positionsRA, p.getName());
		}
	}

	public void initPositions(int speed, HashMap<String, ArrayList<Vector5>> map, String partName)
	{
		ArrayList<Vector5> list = new ArrayList<Vector5>();
		if(map==positionsL || map==positionsRA)
		{
			float angle = map==positionsL?angleLegs:angleArms;
			list.add(new Vector5(new Vector4(1, 0, 0, angle), speed));
			list.add(new Vector5(new Vector4(1, 0, 0, -angle), speed));
		}
		else
		{
			float angle = map==positionsR?angleLegs:angleArms;
			list.add(new Vector5(new Vector4(1, 0, 0, -angle), speed));
			list.add(new Vector5(new Vector4(1, 0, 0, angle), speed));
		}
		map.put(partName, list);
	}

}
