package pokecube.core.client;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.HeldItemHandler;

public class ItemTextureHandler
{
    public static class MegaStone implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "megastone";
            if (tag != null)
            {
                String stackname = tag.getString("pokemon");
                variant = stackname.toLowerCase();
            }
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"),
                "type=" + name.toLowerCase());
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(PokecubeItems.megastone,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"), variant));
    }

    public static void registerMegaStoneItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.megastone, new MegaStone());
        for (String s : HeldItemHandler.megaVariants)
        {
            registerItemVariant("type=" + s);
            ItemStack stack = new ItemStack(PokecubeItems.megastone);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("pokemon", s);
            PokecubeItems.addSpecificItemStack(s, stack);
        }

    }
}
