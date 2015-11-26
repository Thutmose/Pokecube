package pokecube.core.database.stats;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;

public interface ISpecialCaptureCondition 
{
	public static final HashMap<Integer, ISpecialCaptureCondition> captureMap = new HashMap<Integer, ISpecialCaptureCondition>();
	
	boolean canCapture(Entity trainer, IPokemob pokemon);
	
	boolean canCapture(Entity trainer);
}
