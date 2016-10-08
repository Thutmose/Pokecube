package pokecube.compat.bling;

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
            RecipeLoader.instance.knownTextures.put(PokecubeItems.getStack(s), "");
        }
    }
}
