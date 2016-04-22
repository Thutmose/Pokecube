package pokecube.core.items.megastuff;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.handlers.HeldItemHandler;

public class ItemMegastone extends Item
{
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
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("pokemon"))
        {
            String s = stack.getTagCompound().getString("pokemon");
            list.add(s);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name"))
                .trim();
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "megastone";
            if (tag != null)
            {
                String stackname = tag.getString("pokemon");
                variant = stackname.toLowerCase();
            }
            variant = ("" + StatCollector.translateToLocal("item." + variant + ".name")).trim();
            if (!variant.contains(".name")) name = variant;
        }
        return name;
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
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        ItemStack stack;
        subItems.add(new ItemStack(itemIn));
        int n = 0;
        for (String s : HeldItemHandler.megaVariants)
        {
            if (n++ == 0) continue;
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("pokemon", s);
            subItems.add(stack);
        }
    }
}
