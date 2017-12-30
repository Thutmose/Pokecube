package pokecube.core.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.PokecubeMod;

public class ItemFossil extends Item
{
    public ItemFossil()
    {
        super();
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        if (stack.getItemDamage() < HeldItemHandler.fossilVariants.size())
        {
            String s = HeldItemHandler.fossilVariants.get(stack.getItemDamage());
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

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        ItemStack stack;
        for (String s : HeldItemHandler.fossilVariants)
        {
            stack = new ItemStack(itemIn, 1, HeldItemHandler.fossilVariants.indexOf(s));
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();
        if (stack.getItemDamage() < HeldItemHandler.fossilVariants.size())
        {
            name = "item." + HeldItemHandler.fossilVariants.get(stack.getItemDamage());
        }
        return name;
    }
}
