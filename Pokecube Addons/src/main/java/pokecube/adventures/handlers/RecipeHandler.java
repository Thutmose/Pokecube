package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.getStack;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RecipeHandler {

	public static boolean tmRecipe = true;
	
	public static void register()
	{
		if(tmRecipe)
	        GameRegistry.addRecipe(getStack("tm"), new Object[]
	                {
	                    "SS ", 
	                    "SOS", 
	                    "SRS", 'S',Items.iron_ingot, 'O', Blocks.glass_pane, 'R', Items.redstone
	                });
        
        //Cloner
        GameRegistry.addRecipe(getStack("cloner"), new Object[]
                {
                    "III", 
                    "SRS", 
                    "SMS", 'R', getStack("tradingtable"), 'S', Blocks.iron_block, 'M', new ItemStack(Items.golden_apple, 1, 1), 'I',
                    Items.iron_ingot
                });

        //Target
        GameRegistry.addRecipe(getStack("pokemobTarget"), new Object[]
                {
                    " R ", 
                    "ROR", 
                    " E ", 'R', Items.redstone, 'O', Blocks.stone, 'E', Items.emerald
                });
        //Tainer editor
        GameRegistry.addRecipe(getStack("trainerSpawner"), new Object[]
                {
                    " R ", 
                    "ROR", 
                    " E ", 'R', Items.emerald, 'O', Blocks.stone, 'E', Items.emerald
                });
        ItemStack pad = getStack("warppad");
        pad.stackSize = 2;
        //Warp Pad
        GameRegistry.addRecipe(pad, new Object[]
                {
                    "IEI", 
                    "EIE", 
                    "IEI", 'I', Blocks.iron_block, 'E', Items.ender_eye
                });

        //Warp Linker
        GameRegistry.addRecipe(getStack("warplinker"), new Object[]
                {
                    " R ", 
                    "ROR", 
                    " E ", 'R', Items.emerald, 'O', Blocks.stone, 'E', Items.ender_eye
                });

        //Mega ring
        GameRegistry.addRecipe(getStack("megaring"), new Object[]
                {
                    " S ", 
                    "I I", 
                    " I ", 'S', getStack("megastone"), 'I', Items.iron_ingot
                });
        GameRegistry.addRecipe(getStack("pokecubebag"), new Object[]
                {
                    "CCC", 
                    "COC", 
                    "CCC", 'C', Blocks.wool, 'O', getStack("pctop").getItem()
                });

        //Warp Linker
        if(getStack("legendaryorb")!=null)
	        GameRegistry.addRecipe(getStack("megastone"), new Object[]
	                {
	                    " D ", 
	                    "DOD", 
	                    " D ", 'O', getStack("legendaryorb"), 'D', Items.diamond
	                });
        
        ItemStack shards18 = getStack("emerald_shard");
        ItemStack shards1 = getStack("emerald_shard");
        shards18.stackSize = 18;
        GameRegistry.addShapelessRecipe(shards18, new ItemStack(Items.emerald), new ItemStack(Items.emerald));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.emerald), shards1,shards1,shards1,shards1,shards1,shards1,shards1,shards1,shards1);
        
        // PRIEST     
//        VillagerRegistry.instance().registerVillageTradeHandler(2, new VillagerRegistry.IVillageTradeHandler() {
//			@Override
//			public void manipulateTradesForVillager(EntityVillager villager,
//					MerchantRecipeList recipeList, Random random) {
//				int rand = random.nextInt(1);
//				switch (rand) {
//				case 0:
//					recipeList.add(new MerchantRecipe(
//							new ItemStack(Items.emerald, 20), 
//							getStack("exp_share")));
//					break;
//				default:
//					break;
//				}
//			}
//		});
        
	}
}
