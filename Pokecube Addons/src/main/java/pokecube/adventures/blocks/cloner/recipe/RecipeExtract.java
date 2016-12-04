package pokecube.adventures.blocks.cloner.recipe;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.lib.CompatWrapper;

public class RecipeExtract implements IPoweredRecipe
{
    private static List<RecipeExtract> recipeList = Lists.newArrayList();

    public static List<RecipeExtract> getRecipeList()
    {
        return Lists.newArrayList(recipeList);
    }

    public static void addRecipe(RecipeExtract toAdd)
    {
        recipeList.add(toAdd);
    }

    ItemStack output      = CompatWrapper.nullStack;
    ItemStack source      = CompatWrapper.nullStack;
    ItemStack destination = CompatWrapper.nullStack;
    ItemStack selector    = CompatWrapper.nullStack;

    public RecipeExtract()
    {
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.output;
    }

    /** Returns an Item that is the result of this recipe */
    @Override
    @Nullable
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        if (!CompatWrapper.isValid(output)) return CompatWrapper.nullStack;
        return this.output.copy();
    }

    @Override
    public int getEnergyCost()
    {
        return 10000;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = CompatWrapper.nullStack;
        ItemStack item;
        source = CompatWrapper.nullStack;
        destination = CompatWrapper.nullStack;
        selector = CompatWrapper.nullStack;
        boolean wrongnum = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            item = inv.getStackInSlot(i);
            if (!CompatWrapper.isValid(item)) continue;
            if (ClonerHelper.getGenes(item) != null)
            {
                if (CompatWrapper.isValid(source))
                {
                    wrongnum = true;
                    break;
                }
                source = item.copy();
                continue;
            }
            else if (ClonerHelper.isDNAContainer(item))
            {
                if (CompatWrapper.isValid(destination))
                {
                    wrongnum = true;
                    break;
                }
                destination = item.copy();
                continue;
            }
            else if (!ClonerHelper.getGeneSelectors(item).isEmpty())
            {
                if (CompatWrapper.isValid(selector))
                {
                    wrongnum = true;
                    break;
                }
                selector = item.copy();
                continue;
            }
            wrongnum = true;
            break;
        }
        if (!wrongnum && CompatWrapper.isValid(source) && CompatWrapper.isValid(destination))
        {
            PokedexEntry entry = PokecubeManager.getPokedexEntry(source);
            if (destination.getTagCompound() == null) destination.setTagCompound(new NBTTagCompound());
            destination.getTagCompound().setString("pokemob", entry.getName());
            ClonerHelper.mergeGenes(source, destination);
            CompatWrapper.setStackSize(destination, 1);
            output = destination;
            return true;
        }
        return false;
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];
        for (int i = 0; i < aitemstack.length; ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (selector == null)
            {
                aitemstack[i] = null;
            }
            else if (!PokecubeManager.isFilled(itemstack))
            {
                aitemstack[i] = null;
            }
            else
            {
                aitemstack[i] = itemstack.copy();
            }
        }
        return aitemstack;
    }
}
