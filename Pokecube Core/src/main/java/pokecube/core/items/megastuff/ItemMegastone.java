package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMegastone extends Item
{
    private static int                        index      = 0;
    private static Int2ObjectArrayMap<String> stoneIndex = new Int2ObjectArrayMap<>();

    public static void resetMap()
    {
        index = 0;
        stoneIndex.clear();
    }

    public static void registerStone(String name)
    {
        stoneIndex.put(index++, name);
    }

    public static int getStonesCount()
    {
        return index;
    }

    public static Collection<String> getStones()
    {
        return stoneIndex.values();
    }

    public static String getStone(int id)
    {
        return stoneIndex.get(id);
    }

    public ItemMegastone()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced)
    {
        if (stack.getItemDamage() < getStonesCount())
        {
            String s = getStone(stack.getItemDamage());
            tooltip.add(s);
        }
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
        for (int i = 0; i < getStonesCount(); i++)
        {
            stack = new ItemStack(this, 1, i);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        int damage = stack.getItemDamage();
        name = "item." + getStone(damage);
        return name;
    }
}
