package pokecube.core.client.items;

import java.util.Map;

import com.google.common.collect.Maps;

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
    private static Map<String, ModelResourceLocation> models = Maps.newHashMap();

    public static class MeshDefinition implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            String variant = BerryManager.berryNames.get(stack.getItemDamage());
            System.out.println(variant+" "+stack);
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return models.get(name);
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.berries, new MeshDefinition());
        for (String s : BerryManager.berryNames.values())
        {
            registerItemVariant("type=" + s);
            if (PokecubeMod.debug) PokecubeMod.log(s + " " + getLocation(s));
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelResourceLocation loc;
        ModelBakery.registerItemVariants(PokecubeItems.berries,
                loc = new ModelResourceLocation(new ResourceLocation("pokecube", "item/berry"), variant));
        models.put(variant.replace("type=", ""), loc);
    }
}
