package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.items.ItemHeldItems;

public class HeldItemTextureHandler
{
    public static class MegaStone implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            String variant = "???";
            if (stack.getItemDamage() < ItemHeldItems.variants.size())
            {
                variant = ItemHeldItems.variants.get(stack.getItemDamage());
            }
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/held"),
                "type=" + name.toLowerCase(java.util.Locale.ENGLISH));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.held, new MegaStone());
        for (String s : ItemHeldItems.variants)
        {
            registerItemVariant("type=" + s);
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.held,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/held"), variant));
    }
}
