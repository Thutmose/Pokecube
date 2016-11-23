package pokecube.compat.bling;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional.Method;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.HeldItemHandler;
import thut.bling.recipe.RecipeLoader;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class BlingCompat
{

    @Method(modid = "thut_bling")
    @CompatClass(phase = Phase.POST)
    public static void postInitBling()
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.bling.BlingCompat());
    }

    public BlingCompat()
    {
        for (int i = 4; i < HeldItemHandler.megaVariants.size(); i++)
        {
            String s = HeldItemHandler.megaVariants.get(i);
            String tex = null;
            ItemStack stack = PokecubeItems.getStack(s);
            tex = "pokecube:textures/items/" + stack.getDisplayName().toLowerCase(Locale.ENGLISH) + ".png";
            RecipeLoader.instance.knownTextures.put(PokecubeItems.getStack(s), tex);
        }
    }
}
