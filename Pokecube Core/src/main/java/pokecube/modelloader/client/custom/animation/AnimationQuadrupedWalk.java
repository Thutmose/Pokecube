package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.Entity;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;

public class AnimationQuadrupedWalk extends ModelAnimation
{

	/** Front Left */
	public HashMap<String, ArrayList<Vector5>>	positionsFL	= new HashMap<String, ArrayList<Vector5>>();
	/** Front Right */
	public HashMap<String, ArrayList<Vector5>>	positionsFR	= new HashMap<String, ArrayList<Vector5>>();
	/** Hind Right */
	public HashMap<String, ArrayList<Vector5>>	positionsHR	= new HashMap<String, ArrayList<Vector5>>();
	/** Hind Left */
	public HashMap<String, ArrayList<Vector5>>	positionsHL	= new HashMap<String, ArrayList<Vector5>>();

	public HashSet<String>	namesFL	= new HashSet<String>();
	public HashSet<String>	namesFR	= new HashSet<String>();
	public HashSet<String>	namesHL	= new HashSet<String>();
	public HashSet<String>	namesHR	= new HashSet<String>();
	
	public HashMap<String, Float> partialTimes = new HashMap<String, Float>();
	private int[]		ticks	= new int[4];

	private float					lastTick;
	public float maxAngle = 20;
	
	public boolean doAnimation(Entity entity, String partName, IExtendedModelPart part, float partialTick)
	{
		if(partialTick!=lastTick)
			lastTick = partialTick;
		if(namesHL.contains(partName))
		{
			if(!partialTimes.containsKey(part.getName()))
				partialTimes.put(part.getName(), (float) 0);
			ticks[0] = doAnimation(entity, part, partialTick, positionsHL.get(partName), ticks[0], partialTimes.get(part.getName()));
			return true;
		}
		if(namesHR.contains(partName))
		{
			if(!partialTimes.containsKey(part.getName()))
				partialTimes.put(part.getName(), (float) 0);
			ticks[1] = doAnimation(entity, part, partialTick, positionsHR.get(partName), ticks[1], partialTimes.get(part.getName()));
			return true;
		}
		if(namesFL.contains(partName))
		{
			if(!partialTimes.containsKey(part.getName()))
				partialTimes.put(part.getName(), (float) 0);
			ticks[2] = doAnimation(entity, part, partialTick, positionsFL.get(partName), ticks[2], partialTimes.get(part.getName()));
			return true;
		}
		if(namesFR.contains(partName))
		{
			if(!partialTimes.containsKey(part.getName()))
				partialTimes.put(part.getName(), (float) 0);
			ticks[3] = doAnimation(entity, part, partialTick, positionsFR.get(partName), ticks[3], partialTimes.get(part.getName()));
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
	
	public void initAnimation(HashSet<IExtendedModelPart> fl,HashSet<IExtendedModelPart> fr,HashSet<IExtendedModelPart> hl,HashSet<IExtendedModelPart> hr, int speed)
	{
		namesFL.clear();
		namesHL.clear();
		namesFR.clear();
		namesHR.clear();
		positionsFL.clear();
		positionsFR.clear();
		positionsHL.clear();
		positionsHR.clear();
		
		for(IExtendedModelPart p: fl)
		{
			namesFL.add(p.getName());
			initPositions(speed, positionsFL, p.getName());
		}
		for(IExtendedModelPart p: fr)
		{
			namesFR.add(p.getName());
			initPositions(speed, positionsFR, p.getName());
		}
		for(IExtendedModelPart p: hl)
		{
			namesHL.add(p.getName());
			initPositions(speed, positionsHL, p.getName());
		}
		for(IExtendedModelPart p: hr)
		{
			namesHR.add(p.getName());
			initPositions(speed, positionsHR, p.getName());
		}
	}
	
	public void initPositions(int speed, HashMap<String, ArrayList<Vector5>> map, String partName)
	{
		ArrayList<Vector5> list = new ArrayList<Vector5>();
		if(map==positionsFL || map==positionsHL)
		{
			list.add(new Vector5(new Vector4(1, 0, 0, maxAngle), speed));
			list.add(new Vector5(new Vector4(1, 0, 0, -maxAngle), speed));
		}
		else
		{
			list.add(new Vector5(new Vector4(1, 0, 0, -maxAngle), speed));
			list.add(new Vector5(new Vector4(1, 0, 0, maxAngle), speed));
		}
		map.put(partName, list);
	}
	
}
