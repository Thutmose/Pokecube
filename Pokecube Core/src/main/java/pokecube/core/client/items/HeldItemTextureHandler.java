package pokecube.core.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/held"), "type=" + name.toLowerCase());
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.held,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/held"), variant));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.held, new MegaStone());
        for (String s : ItemHeldItems.variants)
        {
            registerItemVariant("type=" + s);
        }
    }
}
