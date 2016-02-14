package pokecube.modelloader.client.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import pokecube.modelloader.client.custom.DefaultIModelRenderer.Vector5;
import thut.api.maths.Vector3;

public class PartInfo
{
	/** Unique name for this part, used for looking up in maps */
	public final String							name;
	/** The global offset of this part, only used by root parts, otherwise is
	 * empty */
	public final Vector3						offset;
	public int									tick		= 0;
	public int									lasttick	= 0;
	/** The global rotation of this part, only used by root parts. */
	public Vector5								rotation	= new Vector5();
	/** A global scaling factor to apply to the model. */
	public Vector3								scale		= Vector3.getNewVectorFromPool();
	/** Any child parts of this part */
	public HashMap<String, PartInfo>			children	= new HashMap<String, PartInfo>();
	private HashMap<String, ArrayList<Vector5>>	phaseInfo	= new HashMap<String, ArrayList<Vector5>>();

	public PartInfo(String name)
	{
		this(name, null);
	}

	public PartInfo(String name, Vector3 offset)
	{
		this.name = name;
		if (offset != null) this.offset = offset;
		else this.offset = Vector3.getNewVectorFromPool();
	}

	public void setPhaseInfo(HashMap<String, ArrayList<Vector5>> newInfo)
	{
		for (String s : newInfo.keySet())
		{
			phaseInfo.put(s, newInfo.get(s));
		}
	}

	public ArrayList<Vector5> getPhase(String phaseName)
	{
		return phaseInfo.get(phaseName);
	}

	public Set<String> getPhases()
	{
		return phaseInfo.keySet();
	}

	@Override
	public String toString()
	{
		return name + " " + phaseInfo;// + " " + children;
	}
}
