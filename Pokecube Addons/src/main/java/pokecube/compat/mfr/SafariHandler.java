package pokecube.compat.mfr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import powercrystals.minefactoryreloaded.api.ISafariNetHandler;

public class SafariHandler{
	static void registerSafariHandlers(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		for(Integer i: Pokedex.getInstance().getEntries())
		{
			PokedexEntry e = Pokedex.getInstance().getEntry(i);
			register.invoke(registry, new Handler(e));
		}
	}
	
	static class Handler implements ISafariNetHandler
	{
		PokedexEntry entry;
		Class<?> clazz;
		public Handler(PokedexEntry entry)
		{
			this.entry = entry;
			clazz = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
			
		}
		
		@Override
		public Class<?> validFor() {
			return clazz;
		}

		@Override
		public void addInformation(ItemStack safariNetStack,
				EntityPlayer player, List<String> infoList,
				boolean advancedTooltips) {
			int num = safariNetStack.getTagCompound().getInteger("pokedexNb");
			if(Database.getEntry(num) == entry)
			{
				infoList.set(0, entry.getName());
				infoList.set(1, entry.getName());
			}
		}
		
	}
}
