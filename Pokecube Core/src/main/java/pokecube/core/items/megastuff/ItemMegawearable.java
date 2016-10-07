package pokecube.core.items.megastuff;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import thut.wearables.EnumWearable;

public class ItemMegawearable extends Item implements IMegaWearable
{
    public static Map<String, EnumWearable> wearables = Maps.newHashMap();

    static
    {
        wearables.put("megaring", EnumWearable.FINGER);
        wearables.put("megabelt", EnumWearable.WAIST);
        wearables.put("megahat", EnumWearable.HAT);
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
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
            name = "item." + variant;
        }
        return name;
    }

    /** Determines if the specific ItemStack can be placed in the specified
     * armor slot.
     *
     * @param stack
     *            The ItemStack
     * @param armorType
     *            Armor slot ID: 0: Helmet, 1: Chest, 2: Legs, 3: Boots
     * @param entity
     *            The entity trying to equip the armor
     * @return True if the given ItemStack can be inserted in the slot */
    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity)
    {
        String name = getUnlocalizedName(stack).replace("item.", "");
        if (name.equals("megahat")) return armorType == EntityEquipmentSlot.HEAD;
        return false;
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        String name = getUnlocalizedName(stack).replace("item.", "");
        return wearables.get(name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        PokecubeCore.proxy.renderWearable(slot, wearer, stack, partialTicks);
    }
}
