package pokecube.core.items.berries;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import pokecube.core.PokecubeItems;

public class RecipeBrewBerries implements IBrewingRecipe
{

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack ingredient)
	{
		//TODO pokebloc things here.
		return PokecubeItems.getStack("revive");
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
		if(tag.hasKey("pokebloc"))
			return true;
		return input.getItem() == Items.GLASS_BOTTLE;
	}

}
