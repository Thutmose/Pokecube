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
        if (isIngredient(ingredient)) return PokecubeItems.getStack("revive");
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

}
