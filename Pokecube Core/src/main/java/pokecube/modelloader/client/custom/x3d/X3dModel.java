package pokecube.modelloader.client.custom.x3d;

import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.IModel;
import pokecube.modelloader.client.custom.oldforgestuff.IModelCustom;
import pokecube.modelloader.client.custom.oldforgestuff.IModelCustomLoader;
import pokecube.modelloader.client.custom.oldforgestuff.ModelFormatException;
import thut.api.maths.Vector3;

public class X3dModel implements IModelCustom, IModelCustomLoader, IModel
{
	private HashMap<String, IExtendedModelPart>	parts	= new HashMap<String, IExtendedModelPart>();
	public String								name;

	public X3dModel()
	{

	}

	public X3dModel(ResourceLocation l)
	{
		this();
		loadModel(l);

	}

	public ArrayList<Vertex> parseVertices(String line) throws ModelFormatException
	{
		ArrayList<Vertex> ret = new ArrayList<Vertex>();

		String[] points = line.split(" ");
		if (points.length
				% 3 != 0) { throw new ModelFormatException("Invalid number of elements in the points string"); }
		for (int i = 0; i < points.length; i += 3)
		{
			Vertex toAdd = new Vertex(Float.parseFloat(points[i]), Float.parseFloat(points[i + 1]),
					Float.parseFloat(points[i + 2]));
			ret.add(toAdd);
		}
		return ret;
	}

	public ArrayList<TextureCoordinate> parseTextures(String line) throws ModelFormatException
	{
		ArrayList<TextureCoordinate> ret = new ArrayList<TextureCoordinate>();

		String[] points = line.split(" ");
		if (points.length % 2 != 0) { throw new ModelFormatException(
				"Invalid number of elements in the points string " + points.length); }
		for (int i = 0; i < points.length; i += 2)
		{
			TextureCoordinate toAdd = new TextureCoordinate(Float.parseFloat(points[i]),
					1 - Float.parseFloat(points[i + 1]));
			ret.add(toAdd);
		}

		return ret;
	}

	HashMap<String, IExtendedModelPart> makeObjects(X3dXMLParser parser) throws Exception
	{
		HashMap<String, HashMap<String, String>> partTranslations = parser.partTranslations;
		HashMap<String, HashMap<String, String>> partPoints = parser.partPoints;
		HashMap<String, ArrayList<String>> childMap = parser.partChildren;
		String name = parser.partName;
		this.name = name;

		if (partTranslations.size() != partPoints.size())
		{
			System.out.println(partTranslations.keySet() + " " + partPoints.keySet());
//			throw new Exception();
		}

		for (String s : partPoints.keySet())
		{
			HashMap<String, String> points = partPoints.get(s);
			X3dObject o = new X3dObject(s);

			o.vertices = parseVertices(points.get("coordinates"));
			o.textureCoordinates = parseTextures(points.get("textures"));
			o.vertexNormals = parseVertices(points.get("normals"));

			String[] offset = partTranslations.get(s).get("translation").split(" ");
			o.offset = Vector3.getNewVectorFromPool().set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
					Float.parseFloat(offset[2]));
			offset = partTranslations.get(s).get("scale").split(" ");
			o.scale = new Vertex(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(offset[2]));
			offset = partTranslations.get(s).get("rotation").split(" ");
			o.rotations.set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(offset[2]),
					(float) toDegrees(Float.parseFloat(offset[3])));
			
			offset = points.get("index").split(" ");
			for (String s1 : offset)
			{
				o.order.add(Integer.parseInt(s1));
			}
			parts.put(s, o);
		}
		for(String s: parts.keySet())
		{
			if(childMap.containsKey(s))
			{
				for(String s1:childMap.get(s))
				{
					parts.get(s).addChild(parts.get(s1));
				}
			}
		}

		return parts;
	}

	public void loadModel(ResourceLocation model)
	{
		X3dXMLParser parser = new X3dXMLParser(model);
		parser.parse();

		try
		{
			makeObjects(parser);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void renderAll()
	{
		for (IExtendedModelPart o : parts.values())
		{
			o.renderAll();
		}
	}

	@Override
	public String getType()
	{
		return "x3d";
	}

	@Override
	public void renderOnly(String... groupNames)
	{
		for (String s : groupNames)
			if (parts.containsKey(s)) parts.get(s).renderAll();
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames)
	{
		for (String s : parts.keySet())
		{
			boolean skipPart = false;
			for (String excludedGroupName : excludedGroupNames)
			{
				if (excludedGroupName.equalsIgnoreCase(s))
				{
					skipPart = true;
				}
			}
			if (!skipPart)
			{
				parts.get(s).renderAll();
			}
		}
	}

	@Override
	public void renderPart(String partName)
	{
		if (parts.containsKey(partName)) parts.get(partName).renderPart(partName);
	}

	@Override
	public String[] getSuffixes()
	{
		return new String[] { "x3d" };
	}

	@Override
	public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
	{
		loadModel(resource);
		return this;
	}

	@Override
	public HashMap<String, IExtendedModelPart> getParts()
	{
		return parts;
	}
}
