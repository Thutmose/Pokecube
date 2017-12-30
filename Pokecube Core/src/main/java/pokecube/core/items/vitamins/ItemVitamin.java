package pokecube.core.items.vitamins;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveConstants;

public class ItemVitamin extends Item implements IMoveConstants
{
    public static List<String> vitamins = Lists.newArrayList();

    public static ItemVitamin  instance;

    public ItemVitamin()
    {
        super();
        this.setHasSubtypes(true);
        instance = this;
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (!this.isInCreativeTab(tab)) return;
        ItemStack stack;
        for (int i = 0; i < vitamins.size(); i++)
        {
            stack = new ItemStack(this, 1, i);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        int i = stack.getItemDamage();
        if (i < vitamins.size()) name = "item." + vitamins.get(i);
        return name;
    }
}
