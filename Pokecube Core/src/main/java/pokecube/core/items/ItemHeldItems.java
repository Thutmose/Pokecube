package pokecube.core.items;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;

public class ItemHeldItems extends Item
{
    public static ArrayList<String> variants = Lists.newArrayList();

    static
    {
        variants.add("waterstone");
        variants.add("firestone");
        variants.add("leafstone");
        variants.add("thunderstone");
        variants.add("moonstone");
        variants.add("sunstone");
        variants.add("shinystone");
        variants.add("ovalstone");
        variants.add("everstone");
        variants.add("duskstone");
        variants.add("dawnstone");
        variants.add("kingsrock");
    }

    public ItemHeldItems()
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
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("type"))
        {
            String s = stack.getTagCompound().getString("type");
            list.add(s);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "???";
            if (tag != null)
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase();
            }
            name = "item." + variant;
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
        for (String s : variants)
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
            subItems.add(stack);
        }
    }
}
