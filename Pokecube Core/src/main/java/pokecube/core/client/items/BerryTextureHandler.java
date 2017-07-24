package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;

public class BerryTextureHandler
{
    public static class MeshDefinition implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            String variant = BerryManager.berryNames.get(stack.getItemDamage());
            if (variant == null || variant.isEmpty()) variant = "null";
            if (variant.equals("null")) PokecubeMod.log(variant + " " + stack);
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/berry"),
                "type=" + name.toLowerCase(java.util.Locale.ENGLISH));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.berries, new MeshDefinition());
        for (String s : BerryManager.berryNames.values())
        {
            registerItemVariant("type=" + s);
            PokecubeMod.log(s + " " + getLocation(s));
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.berries,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/berry"), variant));
    }
}
