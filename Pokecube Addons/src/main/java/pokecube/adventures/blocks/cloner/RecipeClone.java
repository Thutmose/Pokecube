package pokecube.adventures.blocks.cloner;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.lib.CompatWrapper;

public class RecipeClone implements IClonerRecipe
{
    ItemStack output = CompatWrapper.nullStack;
    ItemStack cube   = CompatWrapper.nullStack;
    ItemStack egg    = CompatWrapper.nullStack;
    ItemStack star   = CompatWrapper.nullStack;

    public RecipeClone()
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
        cube = CompatWrapper.nullStack;
        egg = CompatWrapper.nullStack;
        star = CompatWrapper.nullStack;
        boolean wrongnum = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            item = inv.getStackInSlot(i);
            if (!CompatWrapper.isValid(item)) continue;
            if (PokecubeManager.isFilled(item))
            {
                if (CompatWrapper.isValid(cube))
                {
                    wrongnum = true;
                    break;
                }
                cube = item.copy();
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
            else if (item.getItem() == Items.NETHER_STAR)
            {
                if (CompatWrapper.isValid(star))
                {
                    wrongnum = true;
                    break;
                }
                star = item.copy();
                continue;
            }
            wrongnum = true;
            break;
        }
        if (!wrongnum && CompatWrapper.isValid(cube) && CompatWrapper.isValid(egg))
        {
            PokedexEntry entry = PokecubeManager.getPokedexEntry(cube);
            if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
            egg.getTagCompound().setString("pokemob", entry.getName());
            // IPokemob mob = PokecubeManager.itemToPokemob(cube, worldIn);
            // TODO add in way to splice specific dna.
            // if (mob.isShiny() && egg.hasTagCompound())
            // egg.getTagCompound().setBoolean("shiny", true);
            // egg.getTagCompound().setByte("gender", mob.getSexe());
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
            if (star == null)
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

    @Override
    public boolean splicerRecipe()
    {
        return true;
    }
}
