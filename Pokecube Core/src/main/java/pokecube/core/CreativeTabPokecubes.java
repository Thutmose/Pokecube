/**
 *
 */
package pokecube.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabPokecubes extends CreativeTabs
{
    /** @param par1
     * @param par2Str */
    public CreativeTabPokecubes(int par1, String par2Str)
    {
        super(par1, par2Str);
    }

    /** the itemID for the item to be displayed on the tab */
    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem()
    {
        return PokecubeItems.getItem("pokecube");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTabLabel()
    {
        return StatCollector.translateToLocal("igwtab.entry.Pokecubes");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return getTabLabel();
    }
}
