package pokecube.core.items.megastuff;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class ItemMegawearable extends Item implements IBauble, IMegaWearable
{
    public static Map<String, String> wearables = Maps.newHashMap();

    static
    {
        wearables.put("megaring", "RING");
        wearables.put("megabelt", "BELT");
    }

    public ItemMegawearable()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            int damage = stack.getTagCompound().getInteger("dyeColour");
            EnumDyeColor colour = EnumDyeColor.byDyeDamage(damage);
            String s = I18n.format(colour.getUnlocalizedName());
            list.add(s);
        }
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack itemstack)
    {
        String name = getUnlocalizedName(itemstack).replace("item.", "");
        String type = wearables.get(name);
        if (type != null) { return BaubleType.valueOf(type); }
        return BaubleType.RING;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onEquipped(ItemStack itemstack, EntityLivingBase player)
    {
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
    {
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onWornTick(ItemStack itemstack, EntityLivingBase player)
    {
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        ItemStack stack;
        for (String s : wearables.keySet())
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
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
            String variant = "megaring";
            if (tag != null)
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase();
            }
            name = "item." + variant;
        }
        return name;
    }
}
