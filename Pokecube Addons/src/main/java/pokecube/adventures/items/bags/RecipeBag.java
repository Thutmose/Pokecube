package pokecube.adventures.items.bags;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeBag implements IRecipe
{
    private ItemStack output;

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = null;
        boolean armour = false;
        boolean bag = false;
        ItemStack armourStack = null;
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
                }
                else if (stack.getItem() instanceof ItemBag)
                {
                    bag = true;
                }
            }
        }
        if (n != 2 || !(bag && armour)) return false;
        output = armourStack.copy();
        if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
        output.getTagCompound().setBoolean("isapokebag", true);
        return output != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public int getRecipeSize()
    {
        return 10;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
        return ret;
    }

}
