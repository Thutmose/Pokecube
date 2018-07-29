package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;

public class ItemFossil extends Item
{
    public final String type;

    public ItemFossil(String type)
    {
        super();
        this.type = type;
        this.setRegistryName(PokecubeMod.ID, "fossil_" + type);
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setUnlocalizedName(this.getRegistryName().getResourcePath());
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> list, ITooltipFlag advanced)
    {
        list.add(type);
    }
}
