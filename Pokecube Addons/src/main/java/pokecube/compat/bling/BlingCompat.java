package pokecube.compat.bling;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.HeldItemHandler;
import thut.bling.recipe.RecipeLoader;

public class BlingCompat
{
    public BlingCompat()
    {
        for (int i = 0; i < HeldItemHandler.megaVariants.size(); i++)
        {
            if (i != 0 && i < 4) continue;
            String s = HeldItemHandler.megaVariants.get(i);
            String tex = null;
            ItemStack stack = PokecubeItems.getStack(s);
            tex = "pokecube:textures/items/" + stack.getDisplayName().toLowerCase(Locale.ENGLISH) + ".png";
            RecipeLoader.instance.knownTextures.put(PokecubeItems.getStack(s), tex);
        }
    }
}
