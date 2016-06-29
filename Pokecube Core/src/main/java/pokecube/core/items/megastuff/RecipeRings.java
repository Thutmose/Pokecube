package pokecube.core.items.megastuff;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeRings implements IRecipe
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
        boolean ring = false;
        boolean dye = false;
        ItemStack dyeStack = null;
        ItemStack ringStack = null;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                if (stack.getItem() instanceof ItemMegaring)
                {
                    ring = true;
                    ringStack = stack;
                }
                else if (stack.getItem() instanceof ItemDye)
                {
                    dye = true;
                    dyeStack = stack;
                }
            }
        }
        if (dye && ring)
        {
            output = ringStack.copy();
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            output.getTagCompound().setInteger("dyeColour", dyeStack.getItemDamage());
        }
        return output != null;
    }

}