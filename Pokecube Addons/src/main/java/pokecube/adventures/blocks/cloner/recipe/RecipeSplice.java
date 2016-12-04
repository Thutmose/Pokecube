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
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.lib.CompatWrapper;

public class RecipeSplice implements IPoweredRecipe
{
    private static List<RecipeSplice> recipeList = Lists.newArrayList();

    public static List<RecipeSplice> getRecipeList()
    {
        return Lists.newArrayList(recipeList);
    }

    public static void addRecipe(RecipeSplice toAdd)
    {
        recipeList.add(toAdd);
    }

    ItemStack output = CompatWrapper.nullStack;
    ItemStack dna    = CompatWrapper.nullStack;
    ItemStack egg    = CompatWrapper.nullStack;
    ItemStack selector   = CompatWrapper.nullStack;

    public RecipeSplice()
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
        dna = CompatWrapper.nullStack;
        egg = CompatWrapper.nullStack;
        selector = CompatWrapper.nullStack;
        boolean wrongnum = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            item = inv.getStackInSlot(i);
            if (!CompatWrapper.isValid(item)) continue;
            if (ClonerHelper.getGenes(item) != null)
            {
                if (CompatWrapper.isValid(dna))
                {
                    wrongnum = true;
                    break;
                }
                dna = item.copy();
                continue;
            }
            else if (item.getItem() instanceof ItemPokemobEgg)
            {
                if (CompatWrapper.isValid(egg))
                {
                    wrongnum = true;
                    break;
                }
                egg = item.copy();
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
        if (!wrongnum && CompatWrapper.isValid(dna) && CompatWrapper.isValid(egg))
        {
            PokedexEntry entry = PokecubeManager.getPokedexEntry(dna);
            if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
            egg.getTagCompound().setString("pokemob", entry.getName());
            ClonerHelper.mergeGenes(dna, egg);
            CompatWrapper.setStackSize(egg, 1);
            output = egg;
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
