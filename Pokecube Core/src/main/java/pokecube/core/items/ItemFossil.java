package pokecube.core.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.HeldItemHandler;

public class ItemFossil extends Item
{
    public ItemFossil()
    {
        super();
        this.setCreativeTab(PokecubeCore.creativeTabPokecube);
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
        String name = ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name"))
                .trim();
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "fossil";
            if (tag != null)
            {
                String stackname = tag.getString("pokemon");
                variant = stackname.toLowerCase();
            }
            variant = ("" + I18n.translateToLocal("item." + variant + ".name")).trim();
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
        for (String s : HeldItemHandler.fossilVariants)
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("pokemon", s);
            subItems.add(stack);
        }
    }
}
