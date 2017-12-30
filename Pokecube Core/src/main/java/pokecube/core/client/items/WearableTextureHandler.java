package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.items.megastuff.ItemMegawearable;

public class WearableTextureHandler
{
    public static class Mesh implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            String variant = ItemMegawearable.getWearable(stack.getItemDamage());
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/wearables"), "type=" + name);
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.megaring, new Mesh());
        for (String s : ItemMegawearable.getWearables())
        {
            registerItemVariant("type=" + s);
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.megaring,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/wearables"), variant));
    }
}
