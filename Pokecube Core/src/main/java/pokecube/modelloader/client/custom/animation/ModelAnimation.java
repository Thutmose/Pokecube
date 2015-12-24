package pokecube.modelloader.client.custom.animation;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import pokecube.modelloader.client.custom.IExtendedModelPart;

public class ModelAnimation
{
	public HashMap<String, PartAnimation> animations = new HashMap<String, PartAnimation>();
	
	public boolean doAnimation(Entity entity, String partName, IExtendedModelPart part, float partialTick)
	{
		if(animations.containsKey(partName))
		{
			PartAnimation anim = animations.get(partName);
			anim.doAnimation(entity, part, partialTick);
			if(anim.rotations == null)
				return false;
			return true;
		}
		
		return false;
	}
}
