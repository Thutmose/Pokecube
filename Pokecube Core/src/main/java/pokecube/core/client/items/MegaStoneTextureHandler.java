package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.items.megastuff.ItemMegastone;

public class MegaStoneTextureHandler
{
    public static class MegaStone implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            String variant = ItemMegastone.getStone(stack.getItemDamage());
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"),
                "type=" + name.toLowerCase(java.util.Locale.ENGLISH));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.megastone, new MegaStone());
        for (String s : HeldItemHandler.megaVariants)
        {
            registerItemVariant("type=" + s);
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.megastone,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"), variant));
    }
}
