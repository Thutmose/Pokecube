package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.items.vitamins.ItemVitamin;

public class VitaminTextureHandler
{
    public static class MeshDefinition implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "vitamin";
            if (tag != null)
            {
                String stackname = tag.getString("vitamin");
                variant = stackname.toLowerCase();
            }
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/vitamins"), "type=" + name.toLowerCase());
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(ItemVitamin.instance,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/vitamins"), variant));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(ItemVitamin.instance, new MeshDefinition());
        System.out.println(ItemVitamin.instance+" "+ItemVitamin.vitamins);
        for (String s : ItemVitamin.vitamins)
        {
            registerItemVariant("type=" + s);
        }
    }
}
