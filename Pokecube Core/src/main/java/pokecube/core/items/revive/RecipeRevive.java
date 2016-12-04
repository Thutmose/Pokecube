package pokecube.core.items.revive;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeRevive implements IDefaultRecipe
{
    private ItemStack healed = CompatWrapper.nullStack;

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
    public boolean matches(InventoryCrafting craftMatrix, World world)
    {
        healed = CompatWrapper.nullStack;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = CompatWrapper.nullStack;
        ItemStack seal = CompatWrapper.nullStack;

        int n = 0;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
        {
            ItemStack stack = craftMatrix.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                n++;
                if (stack.hasTagCompound() && PokecubeManager.isFilled(stack)) other = stack;
                if (stack.isItemEqual(PokecubeItems.getStack("Revive"))) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(-2)) seal = stack;
            }
        }
        revive = revive && CompatWrapper.isValid(other);
        pokeseal = CompatWrapper.isValid(seal) && CompatWrapper.isValid(other);
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
            if (CompatWrapper.isValid(stack) && stack.hasTagCompound() && PokecubeManager.isFilled(stack))
            {
                if (stack.getItemDamage() != 32767) return false;
                healed = stack.copy();
                if (stack.getItemDamage() == 32767) PokecubeManager.heal(healed);
            }
            return CompatWrapper.isValid(healed);
        }
        return CompatWrapper.isValid(healed);
    }

}
