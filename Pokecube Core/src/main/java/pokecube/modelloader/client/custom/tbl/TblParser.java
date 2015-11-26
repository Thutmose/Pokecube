package pokecube.modelloader.client.custom.tbl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;

public class TblParser
{
	HashMap<String, HashMap<String, String>>	partTranslations;
	HashMap<String, HashMap<String, String>>	partPoints;
	HashMap<String, ArrayList<String>>			partChildren;
	String										partName;
	ResourceLocation							model;
	TblModel			toLoad;
	HashSet<String> existingNames = new HashSet();

	public TblParser(ResourceLocation _model, TblModel _toLoad)
	{
		model = _model;
		toLoad = _toLoad;
	}

	public void parse()
	{
		if(model.toString().contains("tbl"))
		{
			try
			{
				IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);

				InputStream stream = res.getInputStream();
				ZipInputStream zipStream = new ZipInputStream(stream);
				Scanner scanner = new Scanner(zipStream);
				zipStream.getNextEntry();
				String s = scanner.nextLine();
				toLoad.json = s;
				JsonParser parser = new JsonParser();
				JsonObject root = parser.parse(s).getAsJsonObject();
				partName = root.getAsJsonPrimitive("modelName").getAsString();
				int textureWidth, textureHeight;
				textureWidth = root.getAsJsonPrimitive("textureWidth").getAsInt();
				textureHeight = root.getAsJsonPrimitive("textureHeight").getAsInt();

				Vector3 scale = Vector3.getNewVectorFromPool();
				scale.x = root.getAsJsonArray("scale").get(0).getAsFloat();
				scale.y = root.getAsJsonArray("scale").get(1).getAsFloat();
				scale.z = root.getAsJsonArray("scale").get(2).getAsFloat();

				JsonArray cubes = root.getAsJsonArray("cubes");

				List<TblObject> parts = getChildParts(cubes, textureWidth, textureHeight);
				
				for(TblObject part:parts)
				{
					part.scale.set(scale);
				}
				zipStream.close();
				scanner.close();
//				stream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.err.println(model);
			}
			
			
			return;
		}
		try
		{
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
			
			JsonParser parser = new JsonParser();
			JsonReader reader = new JsonReader(new InputStreamReader(res.getInputStream()));
			reader.setLenient(true);
			JsonObject root = parser.parse(reader).getAsJsonObject();
			partName = root.getAsJsonPrimitive("modelName").getAsString();
			int textureWidth, textureHeight;
			textureWidth = root.getAsJsonPrimitive("textureWidth").getAsInt();
			textureHeight = root.getAsJsonPrimitive("textureHeight").getAsInt();

			Vector3 scale = Vector3.getNewVectorFromPool();
			scale.x = root.getAsJsonArray("scale").get(0).getAsFloat();
			scale.y = root.getAsJsonArray("scale").get(1).getAsFloat();
			scale.z = root.getAsJsonArray("scale").get(2).getAsFloat();

			JsonArray cubes = root.getAsJsonArray("cubes");

			List<TblObject> parts = getChildParts(cubes, textureWidth, textureHeight);
			
			for(TblObject part:parts)
			{
				part.scale.set(scale);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println(model);
		}
	}

	public List<TblObject> getChildParts(JsonArray children, int tW, int tH)
	{
		ArrayList<TblObject> ret = new ArrayList<TblObject>();

		for (int i = 0; i < children.size(); i++)
		{
			JsonObject current = children.get(i).getAsJsonObject();
			String cubeName = current.getAsJsonPrimitive("name").getAsString();
			cubeName = getUnique(cubeName);
			existingNames.add(cubeName);
			TblObject o = new TblObject(cubeName);

			ModelBase base = new ModelBase()
			{
			};
			
			base.textureHeight = tH;
			base.textureWidth = tW;
			JsonArray txOffset = current.getAsJsonArray("txOffset");
			int texX = txOffset.get(0).getAsInt(), texY = txOffset.get(1).getAsInt();
			JsonArray dimensions = current.getAsJsonArray("dimensions");
			JsonArray offset = current.getAsJsonArray("offset");
			JsonArray rotation = current.getAsJsonArray("rotation");
			JsonArray position = current.getAsJsonArray("position");

			int x = dimensions.get(0).getAsInt();
			int y = dimensions.get(1).getAsInt();
			int z = dimensions.get(2).getAsInt();
			

			float dx = offset.get(0).getAsFloat();
			float dy = offset.get(1).getAsFloat();
			float dz = offset.get(2).getAsFloat();
			
			float rx = position.get(0).getAsFloat(), ry = position.get(1).getAsFloat(),
					rz = position.get(2).getAsFloat();
			float ax = (float) (rotation.get(0).getAsFloat() * Math.PI/180f),
			ay = (float) (rotation.get(1).getAsFloat() * Math.PI/180f),
			az = (float) (rotation.get(2).getAsFloat() * Math.PI/180f);

			ModelRenderer model = new ModelRenderer(base, texX, texY);
			model.addBox(dx, dy, dz, x, y, z, current.get("mcScale").getAsFloat());
			model.setRotationPoint(rx, ry, rz);
			model.rotateAngleX = ax;
			model.rotateAngleY = ay;
			model.rotateAngleZ = az;
			model.mirror = current.get("txMirror").getAsBoolean();
			o.model = model;
			
			JsonArray childs = current.getAsJsonArray("children");
			List<TblObject> subChilds = getChildParts(childs, tW, tH);
			for(TblObject o1: subChilds)
			{
				o.addChild(o1);
			}
			ret.add(o);
			toLoad.getParts().put(cubeName, o);
		}
		return ret;
	}
	
	private String getUnique(String name)
	{
	    boolean has = false;
	    for(String s: existingNames)
	    {
	        if(s.equalsIgnoreCase(name))
	        {
	            has = true;
	            break;
	        }
	    }
	    if(has)
	    {
	        name += 1;
	        name = getUnique(name);
	    }
	    return name;
	}
}
