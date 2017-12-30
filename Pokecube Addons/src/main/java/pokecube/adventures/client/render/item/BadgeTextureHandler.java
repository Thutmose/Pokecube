package pokecube.adventures.client.render.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.adventures.handlers.ItemHandler;
import pokecube.core.utils.PokeType;

public class BadgeTextureHandler
{
    public static class MegaStone implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            int num = stack.getItemDamage();
            String variant = "???";
            if (num >= 0 && num < PokeType.values().length)
            {
                String stackname = PokeType.values()[num].name;
                variant = stackname;
            }
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube_adventures", "item/badge"),
                "type=" + name.toLowerCase(java.util.Locale.ENGLISH));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(ItemHandler.badges, new MegaStone());
        for (PokeType type : PokeType.values())
        {
            registerItemVariant("type=" + type.name);
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(ItemHandler.badges,
                new ModelResourceLocation(new ResourceLocation("pokecube_adventures", "item/badge"), variant));
    }
}
