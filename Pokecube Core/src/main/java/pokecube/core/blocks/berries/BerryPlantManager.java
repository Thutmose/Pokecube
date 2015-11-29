package pokecube.core.blocks.berries;

import static pokecube.core.PokecubeItems.registerItemTexture;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.moves.TreeRemover;

public class BerryPlantManager extends BerryManager
{
    public static HashMap<String, Block> toRegister = new HashMap<String, Block>();

    public static void addBerry(String name, int id)
    {
        BlockBerryCrop crop = (BlockBerryCrop) new BlockBerryCrop().setHardness(0F).setUnlocalizedName(name + "Crop");
        crop.setBerry(name);
        crop.setBerryIndex(id);
        berryCrops.put(id, crop);
        GameRegistry.registerBlock(crop, ItemBlock.class, name + "Crop");

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            toRegister.put("pokecube_adventures:" + name + "Crop", crop);
        }

        BlockBerryFruit fruit = (BlockBerryFruit) new BlockBerryFruit(id).setHardness(0F)
                .setUnlocalizedName(name + "Fruit");
        fruit.setBerry(name);
        fruit.setBerryIndex(id);
        berryFruits.put(id, fruit);
        GameRegistry.registerBlock(fruit, ItemBlock.class, name + "Fruit");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(Item.getItemFromBlock(fruit), 0,
                    new ModelResourceLocation("pokecube_adventures:" + name + "Fruit", "inventory"));
        }

        TreeRemover.plantTypes.add(fruit);
    }
}
