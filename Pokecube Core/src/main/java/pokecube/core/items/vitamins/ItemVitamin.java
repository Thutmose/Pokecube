package pokecube.core.items.vitamins;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.ItemPokemobUseable;
import pokecube.core.utils.Tools;

public class ItemVitamin extends ItemPokemobUseable implements IMoveConstants
{
    public static List<String> vitamins = Lists.newArrayList();

    static
    {
        vitamins.add("carbos");
        vitamins.add("zinc");
        vitamins.add("protein");
        vitamins.add("calcium");
        vitamins.add("hpup");
        vitamins.add("iron");
    }

    public static ItemVitamin instance;

    public static boolean feedToPokemob(ItemStack stack, Entity entity)
    {
        if (entity instanceof IPokemob)
        {
            if (Tools.isSameStack(stack, PokecubeItems.getStack("hpup")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 10, 0, 0, 0, 0, 0 });
                return true;
            }
            if (Tools.isSameStack(stack, PokecubeItems.getStack("protein")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 10, 0, 0, 0, 0 });
                return true;
            }
            if (Tools.isSameStack(stack, PokecubeItems.getStack("iron")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 10, 0, 0, 0 });
                return true;
            }
            if (Tools.isSameStack(stack, PokecubeItems.getStack("calcium")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 0, 10, 0, 0 });
                return true;
            }
            if (Tools.isSameStack(stack, PokecubeItems.getStack("zinc")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 0, 0, 10, 0 });
                return true;
            }
            if (Tools.isSameStack(stack, PokecubeItems.getStack("carbos")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 0, 0, 0, 10 });
                return true;
            }
        }
        return false;
    }

    public ItemVitamin()
    {
        super();
        this.setHasSubtypes(true);
        instance = this;
    }

    @Override
    public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
    {
        return feedToPokemob(stack, mob);

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
        if (tab != getCreativeTab()) return;
        ItemStack stack;
        for (String s : vitamins)
        {
            stack = new ItemStack(this);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("vitamin", s);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "vitamin";
            if (tag != null)
            {
                String stackname = tag.getString("vitamin");
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
            name = "item." + variant;
        }
        return name;
    }
}
