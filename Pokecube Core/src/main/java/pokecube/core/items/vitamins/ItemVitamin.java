package pokecube.core.items.vitamins;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.ItemPokemobUseable;

public class ItemVitamin extends ItemPokemobUseable implements IMoveConstants
{
    public static List<String> vitamins = Lists.newArrayList();

    static
    {
        vitamins.add("hpup");
        vitamins.add("protein");
        vitamins.add("iron");
        vitamins.add("zinc");
        vitamins.add("calcium");
        vitamins.add("carbos");
    }

    public static ItemVitamin instance;

    public static boolean feedToPokemob(ItemStack stack, Entity entity)
    {
        if (entity instanceof IPokemob)
        {
            if (stack.isItemEqual(PokecubeItems.getStack("hpup")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 10, 0, 0, 0, 0, 0 });
                return true;
            }
            if (stack.isItemEqual(PokecubeItems.getStack("protein")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 10, 0, 0, 0, 0 });
                return true;
            }
            if (stack.isItemEqual(PokecubeItems.getStack("iron")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 10, 0, 0, 0 });
                return true;
            }
            if (stack.isItemEqual(PokecubeItems.getStack("calcium")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 0, 10, 0, 0 });
                return true;
            }
            if (stack.isItemEqual(PokecubeItems.getStack("zinc")))
            {
                ((IPokemob) entity).addEVs(new byte[] { 0, 0, 0, 0, 10, 0 });
                return true;
            }
            if (stack.isItemEqual(PokecubeItems.getStack("carbos")))
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
        boolean ret = feedToPokemob(stack, mob);
        if (ret)
        {
            stack.splitStack(1);
        }
        return feedToPokemob(stack, mob);

    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name"))
                .trim();
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "vitamins";
            if (tag != null)
            {
                String stackname = tag.getString("vitamin");
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
        for (String s : vitamins)
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("vitamin", s);
            subItems.add(stack);
        }
    }
}
