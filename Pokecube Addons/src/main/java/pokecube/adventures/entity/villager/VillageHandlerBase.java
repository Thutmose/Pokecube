package pokecube.adventures.entity.villager;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public abstract class VillageHandlerBase
{
	// Common trade goods
	protected ItemStack emerald = new ItemStack(Items.emerald, 1);
	
	// Bunch of helper methods
	protected void addRandomTrade(MerchantRecipeList list, ItemStack src, ItemStack dst, double chance, Random rnd)
	{
		addRandomTrade(list, src, (ItemStack)null, dst, chance, rnd);
	}

	protected void addRandomTrade(MerchantRecipeList list, ItemStack src1, ItemStack src2, ItemStack dst, double chance, Random rnd)
	{
		if( chance >= rnd.nextDouble() )
		{
			list.add(new MerchantRecipe(src1, src2, dst));
		}
	}
	
	protected ItemStack randomItem(Block block, int min, int max, int metadata, Random rnd)
	{
		return new ItemStack(block, min + rnd.nextInt(max - min + 1), metadata);
	}
	
	protected ItemStack randomItem(Block block, int min, int max, Random rnd)
	{
		return randomItem(block, min, max, 0, rnd);
	}

	protected ItemStack randomItem(Item item, int min, int max, int metadata, Random rnd)
	{
		return new ItemStack(item, min + rnd.nextInt(max - min + 1), metadata);
	}
	
	protected ItemStack randomItem(Item item, int min, int max, Random rnd)
	{
		return randomItem(item, min, max, 0, rnd);
	}
}
