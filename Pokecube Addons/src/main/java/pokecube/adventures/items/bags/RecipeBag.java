package pokecube.adventures.items.bags;

import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeBag implements IDefaultRecipe
{
    private ItemStack output = ItemStack.EMPTY;

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
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = ItemStack.EMPTY;
        boolean bag = false;
        boolean dye = false;
        ItemStack dyeStack = ItemStack.EMPTY;
        ItemStack bagStack = ItemStack.EMPTY;
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

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }
}
