package pokecube.adventures.blocks.cloner;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.lib.CompatWrapper;

public class RecipeClone implements IClonerRecipe
{
    ItemStack output;
    ItemStack cube = null;
    ItemStack egg  = null;
    ItemStack star = null;

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
        if (output == null) return null;
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
        output = null;
        ItemStack item;
        cube = null;
        egg = null;
        star = null;
        boolean wrongnum = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            item = inv.getStackInSlot(i);
            if (item == null) continue;
            if (PokecubeManager.isFilled(item))
            {
                if (cube != null)
                {
                    wrongnum = true;
                    break;
                }
                cube = item.copy();
                continue;
            }
            else if (item.getItem() instanceof ItemPokemobEgg)
            {
                if (egg != null)
                {
                    wrongnum = true;
                    break;
                }
                egg = item.copy();
                continue;
            }
            else if (item.getItem() == Items.NETHER_STAR)
            {
                if (star != null)
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
        if (!wrongnum && cube != null && egg != null)
        {
            int pokenb = PokecubeManager.getPokedexNb(cube);
            PokedexEntry entry = Database.getEntry(pokenb);
            pokenb = entry.getChildNb();
            if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
            egg.getTagCompound().setInteger("pokemobNumber", pokenb);
            IPokemob mob = PokecubeManager.itemToPokemob(cube, worldIn);
            if (mob.isShiny() && egg.hasTagCompound()) egg.getTagCompound().setBoolean("shiny", true);
            egg.getTagCompound().setByte("gender", mob.getSexe());
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
