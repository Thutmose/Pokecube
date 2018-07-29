package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;

public class ItemMegawearable extends Item
{
    private static Map<String, String> wearables = Maps.newHashMap();

    public static void registerWearable(String name, String slot)
    {
        wearables.put(name, slot);
    }

    public static String getSlot(String name)
    {
        return wearables.get(name);
    }

    public static Collection<String> getWearables()
    {
        return wearables.keySet();
    }

    static
    {
        registerWearable("ring", "FINGER");
        registerWearable("belt", "WAIST");
        registerWearable("hat", "HAT");
    }

    public final String name;
    public final String slot;

    public ItemMegawearable(String name, String slot)
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.name = name;
        this.slot = slot;
        this.setRegistryName(PokecubeMod.ID, "mega_" + name);
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setUnlocalizedName(this.getRegistryName().getResourcePath());

    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            int damage = stack.getTagCompound().getInteger("dyeColour");
            EnumDyeColor colour = EnumDyeColor.byDyeDamage(damage);
            String s = I18n.format(colour.getUnlocalizedName());
            tooltip.add(s);
        }
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
