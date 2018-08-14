package pokecube.core.items.revive;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeRevive implements IDefaultRecipe
{
    private ItemStack healed = ItemStack.EMPTY;

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
    public boolean matches(InventoryCrafting craftMatrix, World world)
    {
        healed = ItemStack.EMPTY;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        int n = 0;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
        {
            ItemStack stack = craftMatrix.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                n++;
                if (stack.hasTagCompound() && PokecubeManager.isFilled(stack)) other = stack;
                if (stack.isItemEqual(PokecubeItems.getStack("Revive"))) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)) seal = stack;
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
                NBTTagCompound mobtag = mob.getEntity().getEntityData();
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
