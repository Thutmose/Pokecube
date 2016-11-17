package pokecube.core.items.revive;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;

public class RecipeRevive implements IRecipe
{
    private ItemStack healed;

    @Override
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
    {
        return healed;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return healed;
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
    public boolean matches(InventoryCrafting craftMatrix, World world)
    {
        healed = null;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = null;
        ItemStack seal = null;

        int n = 0;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
        {
            ItemStack stack = craftMatrix.getStackInSlot(i);
            if (stack != null)
            {
                n++;
                if (stack.hasTagCompound() && PokecubeManager.isFilled(stack)) other = stack;
                if (stack.isItemEqual(PokecubeItems.getStack("Revive"))) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(-2)) seal = stack;
            }
        }
        revive = revive && other != null;
        pokeseal = seal != null && other != null;
        if (n != 2) return false;

        if (pokeseal)
        {
            if (seal.hasTagCompound())
            {
                IPokemob mob = PokecubeManager.itemToPokemob(other, world);
                NBTTagCompound tag = seal.getTagCompound().getCompoundTag(TagNames.POKESEAL);
                NBTTagCompound mobtag = ((Entity) mob).getEntityData();
                mobtag.setTag("sealtag", tag);
                other = PokecubeManager.pokemobToItem(mob);
                healed = other;
            }
        }
        else if (revive)
        {
            ItemStack stack = other;
            if (stack != null && stack.hasTagCompound() && PokecubeManager.isFilled(stack))
            {
                if (stack.getItemDamage() != 32767) return false;
                healed = stack.copy();
                if (stack.getItemDamage() == 32767) PokecubeManager.heal(healed);
            }
            return healed != null;
        }
        return healed != null;
    }

}
