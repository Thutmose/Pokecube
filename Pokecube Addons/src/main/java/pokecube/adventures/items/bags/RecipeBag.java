package pokecube.adventures.items.bags;

import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import thut.lib.CompatWrapper;

public class RecipeBag implements IRecipe
{
    private ItemStack output = CompatWrapper.nullStack;

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
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = CompatWrapper.nullStack;
        boolean bag = false;
        boolean dye = false;
        ItemStack dyeStack = CompatWrapper.nullStack;
        ItemStack bagStack = CompatWrapper.nullStack;
        int n = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                n++;
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
        if (n == 2)
        {
            if (dye && bag)
            {
                output = bagStack.copy();
                if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
                output.getTagCompound().setInteger("dyeColour", dyeStack.getItemDamage());
            }
            return CompatWrapper.isValid(output);
        }
        return CompatWrapper.isValid(output);
    }

}
