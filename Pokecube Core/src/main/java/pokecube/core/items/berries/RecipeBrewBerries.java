package pokecube.core.items.berries;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import pokecube.core.PokecubeItems;
import thut.lib.CompatWrapper;

public class RecipeBrewBerries implements IBrewingRecipe
{

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient)
    {
        if (isIngredient(ingredient)) return makeOutput(input, ingredient);
        return CompatWrapper.nullStack;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient)
    {
        return ingredient.getItem() instanceof ItemBerry;
    }

    @Override
    public boolean isInput(ItemStack input)
    {
        NBTTagCompound tag = input.getTagCompound();
        if (tag != null && tag.hasKey("pokebloc")) return true;
        return input.getItem() == Items.GLASS_BOTTLE;
    }

    private ItemStack makeOutput(ItemStack input, ItemStack ingredient)
    {
        NBTTagCompound pokebloc = new NBTTagCompound();
        int[] flav = BerryManager.berryFlavours.get(ingredient.getItemDamage());
        int[] old = null;
        if (input.hasTagCompound() && input.getTagCompound().hasKey("pokebloc"))
            old = input.getTagCompound().getIntArray("pokebloc");
        ItemStack stack = PokecubeItems.getStack("revive");
        if (flav != null)
        {
            flav = flav.clone();
            if (old != null) for (int i = 0; i < (Math.min(old.length, flav.length)); i++)
            {
                flav[i] += old[i];
            }
            pokebloc.setIntArray("pokebloc", flav);
            NBTTagCompound tag = input.hasTagCompound() ? input.getTagCompound().copy() : new NBTTagCompound();
            tag.setTag("pokebloc", pokebloc);
            stack.setTagCompound(tag);
        }
        return stack;
    }

}
