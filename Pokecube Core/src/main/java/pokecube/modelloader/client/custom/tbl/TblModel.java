package pokecube.modelloader.client.custom.tbl;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.IModel;
import pokecube.modelloader.client.custom.oldforgestuff.IModelCustom;
import pokecube.modelloader.client.custom.oldforgestuff.IModelCustomLoader;
import pokecube.modelloader.client.custom.oldforgestuff.ModelFormatException;

public class TblModel implements IModel, IModelCustomLoader, IModelCustom
{
	
	private HashMap<String, IExtendedModelPart>	parts	= new HashMap<String, IExtendedModelPart>();
	public String json;
	public TblModel(){}
	
	public TblModel(ResourceLocation l)
	{
		this();
		loadModel(l);
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
	public String getType()
	{
		return "json";
	}

	@Override
	public String[] getSuffixes()
	{
		return new String[] { "json", "tbl" };
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

	public void loadModel(ResourceLocation model)
	{
		TblParser parser = new TblParser(model, this);
		parser.parse();
	}
}
