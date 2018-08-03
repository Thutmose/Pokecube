package pokecube.adventures.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import thut.lib.CompatItem;

public class ItemBadge extends CompatItem
{

    public static boolean isBadge(ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemBadge;
    }

    public final PokeType type;

    public ItemBadge(PokeType type)
    {
        super();
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        String name = type.name.equals("???") ? "unknown" : type.name;
        this.setRegistryName(PokecubeAdv.ID, "badge_" + name);
        this.setUnlocalizedName("badge_" + name);
        this.type = type;
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> list, ITooltipFlag advanced)
    {
        list.add(type.name);
    }
}
