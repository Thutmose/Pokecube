package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.getStack;

import com.google.common.collect.Lists;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.adventures.items.bags.RecipeBag;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class RecipeHandler
{

    public static boolean tmRecipe = true;

    private static void addClonerRecipes()
    {
        for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            PokedexEntry i = PokecubeItems.fossils.get(stack);
            if (PokecubeMod.registered.get(i.getPokedexNb()))
            {
                RecipeFossilRevive newRecipe = new RecipeFossilRevive(stack, Lists.newArrayList(stack), i, 20000);
                RecipeFossilRevive.addRecipe(newRecipe);
            }
        }
        ItemStack eggIn = PokecubeItems.getStack("pokemobEgg");
        eggIn.setItemDamage(Short.MAX_VALUE);
        ItemStack mewhair = PokecubeItems.getStack("mewHair");
        ItemStack ironBlock = new ItemStack(Blocks.IRON_BLOCK);
        ItemStack redstoneBlock = new ItemStack(Blocks.REDSTONE_BLOCK);
        ItemStack diamondBlock = new ItemStack(Blocks.DIAMOND_BLOCK);
        ItemStack dome = PokecubeItems.getStack("kabuto");
        ItemStack potion = new ItemStack(Items.POTIONITEM, 1, Short.MAX_VALUE);

        // Ditto
        ItemStack eggOut = ItemPokemobEgg.getEggStack(132);
        RecipeFossilRevive newRecipe = new RecipeFossilRevive(eggOut, Lists.newArrayList(mewhair, eggIn, potion),
                Database.getEntry("ditto"), 10000);
        newRecipe.remainIndex.add(0);
        // Low priority
        newRecipe.priority = -1;
        RecipeFossilRevive.addRecipe(newRecipe);

        potion = new ItemStack(Items.POTIONITEM);
        PotionUtils.addPotionToItemStack(potion, PotionType.getPotionTypeForName("minecraft:strong_regeneration"));

        // Genesect
        eggOut = ItemPokemobEgg.getEggStack(649);
        newRecipe = new RecipeFossilRevive(eggOut,
                Lists.newArrayList(ironBlock, redstoneBlock, diamondBlock, dome, potion), Database.getEntry(649),
                30000);
        newRecipe.tame = false;
        newRecipe.level = 70;
        RecipeFossilRevive.addRecipe(newRecipe);

        // Mewtwo
        eggOut = ItemPokemobEgg.getEggStack(150);
        newRecipe = new RecipeFossilRevive(eggOut, Lists.newArrayList(mewhair, eggIn, potion),
                Database.getEntry("mewtwo"), 30000);
        newRecipe.tame = false;
        newRecipe.level = 70;
        RecipeFossilRevive.addRecipe(newRecipe);
    }

    private static void addLegendarySpawnerRecipes()
    {
        GameRegistry.addRecipe(getStack("registeelspawner"),
                new Object[] { "RSR", "SRS", "RSR", 'S', Blocks.IRON_BLOCK, 'R', Blocks.REDSTONE_BLOCK });

        GameRegistry.addRecipe(getStack("regicespawner"),
                new Object[] { "III", "IGI", "III", 'I', Blocks.PACKED_ICE, 'G', Blocks.GOLD_BLOCK });

        GameRegistry.addRecipe(getStack("regirockspawner"),
                new Object[] { "OSO", "OOO", "OSO", 'O', Blocks.OBSIDIAN, 'S', Blocks.STONE });

        GameRegistry.addRecipe(getStack("celebispawner"), new Object[] { "MMM", "SRS", "SIS", 'R', Items.REDSTONE, 'S',
                Blocks.LOG, 'M', Blocks.GLASS, 'I', Items.IRON_INGOT });

        GameRegistry.addRecipe(getStack("hoohspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Items.FEATHER, 'G',
                Blocks.GOLD_BLOCK, 'I', Items.GOLD_INGOT });

        GameRegistry.addRecipe(getStack("kyogrespawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.ICE, 'G',
                Blocks.LAPIS_BLOCK, 'I', PokecubeItems.getStack("waterstone") });

        GameRegistry.addRecipe(getStack("groudonspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.COAL_BLOCK,
                'G', Blocks.REDSTONE_BLOCK, 'I', PokecubeItems.getStack("firestone") });
    }

    public static void register()
    {
        RecipeSorter.register("pokecube_adventures:bag", RecipeBag.class, Category.SHAPELESS,
                "after:minecraft:shapeless");
        GameRegistry.addRecipe(new RecipeBag());
        if (tmRecipe) GameRegistry.addRecipe(getStack("tm"), new Object[] { "SS ", "SOS", "SRS", 'S', Items.IRON_INGOT,
                'O', Blocks.GLASS_PANE, 'R', Items.REDSTONE });

        // Fossil Reanimator
        GameRegistry.addRecipe(getStack("reanimator"),
                new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"), 'S', Blocks.GOLD_BLOCK, 'M',
                        new ItemStack(Items.GOLDEN_APPLE, 1, 0), 'I', Items.IRON_INGOT });
        // Splicing Device
        GameRegistry.addRecipe(getStack("cloner"), new Object[] { "III", "SRS", "SMS", 'R', getStack("reanimator"), 'S',
                Blocks.DIAMOND_BLOCK, 'M', new ItemStack(Items.GOLDEN_APPLE, 1, 1), 'I', Items.NETHER_STAR });

        // AFA
        GameRegistry.addRecipe(getStack("afa"), new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"), 'S',
                Blocks.IRON_BLOCK, 'M', Items.REDSTONE, 'I', Items.IRON_INGOT });

        // Energy Siphon
        GameRegistry.addRecipe(getStack("pokesiphon"),
                new Object[] { "BBB", "BRB", "BBB", 'R', getStack("afa"), 'B', Blocks.REDSTONE_BLOCK });

        // Target
        GameRegistry.addRecipe(getStack("pokemobTarget"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.REDSTONE, 'O', Blocks.STONE, 'E', Items.EMERALD });
        // Tainer editor
        GameRegistry.addRecipe(getStack("traderSpawner"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.EMERALD, 'O', Blocks.STONE, 'E', Items.EMERALD });
        ItemStack pad = getStack("warppad");
        pad.stackSize = 2;
        // Warp Pad
        GameRegistry.addRecipe(pad, new Object[] { "IEI", "EIE", "IEI", 'I', Blocks.IRON_BLOCK, 'E', Items.ENDER_EYE });

        // Warp Linker
        GameRegistry.addRecipe(getStack("warplinker"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.EMERALD, 'O', Blocks.STONE, 'E', Items.ENDER_EYE });

        // Bag
        GameRegistry.addRecipe(getStack("pokecubebag"),
                new Object[] { "CCC", "COC", "CCC", 'C', Blocks.WOOL, 'O', getStack("pctop").getItem() });

        ItemStack shards18 = getStack("emerald_shard");
        ItemStack shards1 = getStack("emerald_shard");
        shards18.stackSize = 18;
        GameRegistry.addShapelessRecipe(shards18, new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.EMERALD), shards1, shards1, shards1, shards1, shards1,
                shards1, shards1, shards1, shards1);
        addLegendarySpawnerRecipes();
        addClonerRecipes();
    }
}
