package pokecube.adventures.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import thut.lib.CompatItem;

public class ItemBadge extends CompatItem
{

    public static boolean isBadge(ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemBadge
                && stackIn.getItemDamage() < PokeType.values().length;
    }

    public ItemBadge()
    {
        super();
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> list, ITooltipFlag advanced)
    {
        if (stack.getItemDamage() < PokeType.values().length)
        {
            String s = PokeType.getTranslatedName(PokeType.values()[stack.getItemDamage()]);
            list.add(s);
        }
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected List<ItemStack> getTabItems(Item itemIn, CreativeTabs tab)
    {
        List<ItemStack> subItems = Lists.newArrayList();
        if (!this.isInCreativeTab(tab)) return subItems;
        ItemStack stack;
        for (int i = 0; i < PokeType.values().length; i++)
        {
            stack = new ItemStack(itemIn, 1, i);
            subItems.add(stack);
        }
        return subItems;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();
        if (stack.getItemDamage() < PokeType.values().length)
        {
            name = "item.badge" + PokeType.values()[stack.getItemDamage()];
        }
        return name;
    }
}
