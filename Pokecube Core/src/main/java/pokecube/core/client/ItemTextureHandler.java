package pokecube.core.client;

import java.util.ArrayList;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import pokecube.core.PokecubeItems;

public class ItemTextureHandler
{
    public static ArrayList<String> megaVariants = new ArrayList<>();
    
    static
    {
        megaVariants.add("megastone");
        megaVariants.add("gardevoirmega");
        megaVariants.add("shiny_charm");
    }
    
    public static void registerMegaStoneItemModels()
    {
        ModelLoader.setCustomMeshDefinition(PokecubeItems.megastone, new MegaStone());
        for(String s: megaVariants)
        {
            registerItemVariant("type=" + s);
        }

        ItemStack stack = new ItemStack(PokecubeItems.megastone);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("pokemon", "shiny_charm");
        PokecubeItems.addSpecificItemStack("shiny_charm", stack);
    }

    private static void registerItemVariant(String variant)
    {
        ModelLoader.registerItemVariants(PokecubeItems.megastone,
                new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"), variant));
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation("pokecube", "item/megastone"),
                "type=" + name.toLowerCase());
    }

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
}
