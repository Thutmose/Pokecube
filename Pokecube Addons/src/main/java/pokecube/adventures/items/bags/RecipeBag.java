package pokecube.adventures.items.bags;

import java.util.List;
import java.util.Locale;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeBag implements IRecipe
{
    private ItemStack output;

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public int getRecipeSize()
    {
        return 10;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
        return ret;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = null;
        boolean armour = false;
        boolean bag = false;
        boolean dye = false;
        ItemStack dyeStack = null;
        ItemStack armourStack = null;
        ItemStack bagStack = null;
        int n = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                n++;
                if (stack.getItem().isValidArmor(stack, EntityEquipmentSlot.CHEST, null)
                        && !(stack.getItem() instanceof ItemBag))
                {
                    armour = true;
                    armourStack = stack;
                    continue;
                }
                if (stack.getItem() instanceof ItemBag)
                {
                    bag = true;
                    bagStack = stack;
                    continue;
                }
                List<ItemStack> dyes = OreDictionary.getOres("dye");
                boolean isDye = false;
                for (ItemStack dye1 : dyes)
                {
                    if (OreDictionary.itemMatches(dye1, stack, false))
                    {
                        isDye = true;
                        break;
                    }
                }
                if (isDye)
                {
                    dye = true;
                    dyeStack = stack;
                }
            }
        }
        if (n == 1 && armour)
        {

        }
        if (n != 2 || !(bag && armour))
        {
            if (dye && bag)
            {
                output = bagStack.copy();
                if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
                output.getTagCompound().setInteger("dyeColour", dyeStack.getItemDamage());
            }
            else if (dye && armour && armourStack.hasTagCompound()
                    && armourStack.getTagCompound().getBoolean("isapokebag"))
            {
                output = armourStack.copy();
                if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
                int[] ids = OreDictionary.getOreIDs(dyeStack);
                int colour = dyeStack.getItemDamage();
                for (int i : ids)
                {
                    String name = OreDictionary.getOreName(i);
                    if (name.startsWith("dye") && name.length() > 3)
                    {
                        String val = name.replace("dye", "").toUpperCase(Locale.ENGLISH);
                        try
                        {
                            EnumDyeColor type = EnumDyeColor.valueOf(val);
                            colour = type.getDyeDamage();
                            break;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                output.getTagCompound().setInteger("dyeColour", colour);
            }
            return output != null;
        }
        output = armourStack.copy();
        if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
        output.getTagCompound().setBoolean("isapokebag", true);
        if (dye)
        {
            int[] ids = OreDictionary.getOreIDs(dyeStack);
            int colour = dyeStack.getItemDamage();
            for (int i : ids)
            {
                String name = OreDictionary.getOreName(i);
                if (name.startsWith("dye") && name.length() > 3)
                {
                    String val = name.replace("dye", "").toUpperCase(Locale.ENGLISH);
                    try
                    {
                        EnumDyeColor type = EnumDyeColor.valueOf(val);
                        colour = type.getDyeDamage();
                        break;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            output.getTagCompound().setInteger("dyeColour", colour);
        }
        else if (bagStack.hasTagCompound() && bagStack.getTagCompound().hasKey("dyeColour"))
        {
            output.getTagCompound().setInteger("dyeColour", bagStack.getTagCompound().getInteger("dyeColour"));
        }
        return output != null;
    }

}
