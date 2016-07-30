package pokecube.adventures.client.render.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.adventures.handlers.ItemHandler;
import pokecube.adventures.items.ItemBadge;

public class BadgeTextureHandler
{
    public static class MegaStone implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "???";
            if (tag != null)
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase();
            }
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube_adventures", "item/badge"), "type=" + name.toLowerCase());
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(ItemHandler.badges,
                new ModelResourceLocation(new ResourceLocation("pokecube_adventures", "item/badge"), variant));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(ItemHandler.badges, new MegaStone());
        for (String s : ItemBadge.variants)
        {
            registerItemVariant("type=" + s);
        }
    }
}
