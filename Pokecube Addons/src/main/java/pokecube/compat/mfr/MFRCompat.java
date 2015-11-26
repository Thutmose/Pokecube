package pokecube.compat.mfr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;

public class MFRCompat {

//	public static List<IFactoryRanchable> ranchables = new ArrayList<IFactoryRanchable>();
//	
//	static {
//		ranchables.add(Ranchables.getMareep());
//		ranchables.add(Ranchables.getMiltank());
//	}
//	
//	public static void register()
//	{
//		 try {
//	            Class<?> registry = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
//	            if(registry != null) {
//	            	//Ranchables
//	            	Method registerRanchable = registry.getMethod("registerRanchable", IFactoryRanchable.class);
//
//	            	for(IFactoryRanchable r: ranchables)
//	            	{
//						registerRanchable.invoke(registry, r);
//	            	}
//
//	            	//Grindables
//	            	Method registerGrindable = registry.getMethod("registerGrindable", IFactoryGrindable.class);
//					Grindables.registerGrindables(registry, registerGrindable);
//	            	//Mob Spawner logic
//	            	Method registerSpawnHandler = registry.getMethod("registerSpawnHandler", IMobSpawnHandler.class);
//					Grindables.registerSpawnHandler(registry, registerSpawnHandler);
//					//Mob spawn costs
//	            	Method registerSpawnCosts = registry.getMethod("setBaseSpawnCost", String.class, int.class);
//					Grindables.registerSpawnCosts(registry, registerSpawnCosts);
//	            	
//					Method registerSafariHandler = registry.getMethod("registerSafariNetHandler", ISafariNetHandler.class);
//					SafariHandler.registerSafariHandlers(registry, registerSafariHandler);
//					
//					Method registerLaserOre = registry.getMethod("registerLaserOre", int.class, ItemStack.class);
//					registerOres(registry, registerLaserOre);
//					
//					Method registerFruit = registry.getMethod("registerFruit", IFactoryFruit.class);
//					Plants.registerFruits(registry, registerFruit);
//	          	  
//	            }
//	          } catch(Exception e) {
//	      //  	  e.printStackTrace();
//	        }
//	}
//	
//	static void registerOres(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//	{
//		ItemStack fossil = PokecubeItems.getStack("fossilStone");
//		fossil.stackSize = 1;
//		int num = 10;
//		register.invoke(registry, num, fossil);
//		
//	}
	
}
