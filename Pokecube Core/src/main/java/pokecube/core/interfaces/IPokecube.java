package pokecube.core.interfaces;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.events.CaptureEvent;
import thut.api.maths.Vector3;

public interface IPokecube {

	public static HashMap<Integer, PokecubeBehavior> map = new HashMap<Integer, PokecubeBehavior>();
	
	double getCaptureModifier(IPokemob mob, int pokecubeId);
	
	boolean throwPokecube(World world, EntityPlayer player, ItemStack cube, Vector3 targetLocation, Entity target);
	
	public static abstract class PokecubeBehavior
	{
		public abstract void onPreCapture(CaptureEvent.Pre evt);
		
		public abstract void onPostCapture(CaptureEvent.Post evt);
		
		public static void addCubeBehavior(int cubeId, PokecubeBehavior behavior)
		{
			map.put(cubeId, behavior);
		}
	}
}
