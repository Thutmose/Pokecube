/**
 * 
 */
package pokecube.core.items.vitamins;

import static pokecube.core.PokecubeItems.registerItemTexture;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.PokecubeMod;

/**
 * 
 * @author Oracion
 * @author Manchou
 */
public class VitaminManager implements IMoveConstants{

	public static Map<Integer, ItemVitamin> vitaminItems = new HashMap<Integer, ItemVitamin>();
	
	public static void addVitamin(String name, int id) {
		ItemVitamin vitamin = (ItemVitamin) new ItemVitamin().setUnlocalizedName(name);
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
        	registerItemTexture(vitamin, 0, new ModelResourceLocation("pokecube:"+name, "inventory"));
        }
		vitamin.setVitamin(name);
		vitamin.setVitaminIndex(id);
		vitamin.setCreativeTab(PokecubeMod.creativeTabPokecube);
		vitaminItems.put(id, vitamin);
        PokecubeItems.register(vitamin, name);
	}
	
	public static ItemVitamin getVitaminItem(String name){
		for (ItemVitamin vitaminItem : vitaminItems.values()) {
			if (vitaminItem.getVitaminName().equalsIgnoreCase(name))
				return vitaminItem;
		}
		return null;
	}
	
}