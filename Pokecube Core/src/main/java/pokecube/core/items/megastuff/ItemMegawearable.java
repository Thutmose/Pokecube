package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMegawearable extends Item
{
    private static int                        index         = 0;

    private static Int2ObjectArrayMap<String> wearableIndex = new Int2ObjectArrayMap<>();
    private static Map<String, String>        wearables     = Maps.newHashMap();

    public static void registerWearable(String name, String slot)
    {
        wearableIndex.put(index++, name);
        wearables.put(name, slot);
    }

    public static int getWearableCount()
    {
        return index;
    }

    public static Collection<String> getWearables()
    {
        return wearableIndex.values();
    }

    public static String getWearable(int id)
    {
        return wearableIndex.get(id);
    }

    public static String getSlot(int id)
    {
        return wearables.get(wearableIndex.get(id));
    }

    static
    {
        registerWearable("megaring", "FINGER");
        registerWearable("megabelt", "WAIST");
        registerWearable("megahat", "HAT");
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
        if (tab != getCreativeTab()) return;
        for (int i = 0; i < getWearableCount(); i++)
        {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        String variant = getWearable(stack.getItemDamage());
        name = "item." + variant;
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
}
