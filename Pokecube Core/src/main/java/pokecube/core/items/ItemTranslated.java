/**
 *
 */
package pokecube.core.items;

import net.minecraft.item.Item;
import pokecube.core.interfaces.PokecubeMod;

/**
 * @author Manchou
 *
 */
public class ItemTranslated extends Item
{
	protected String modId = PokecubeMod.ID;
	
    /**
     * @param par1
     */
    public ItemTranslated()
    {
        super();
    }
    
    public ItemTranslated setModId(String modId) {
		this.modId = modId;
		return this;
	}

}
