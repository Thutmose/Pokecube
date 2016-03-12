package pokecube.compat.jei.cloner;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class ClonerRecipeWrapper extends VanillaRecipeWrapper implements ICraftingRecipeWrapper
{

    @Nonnull
    private final ClonerRecipe recipe;

    public ClonerRecipeWrapper(@Nonnull ClonerRecipe recipe)
    {
        this.recipe = recipe;
        for (Object input : this.recipe.recipeItems)
        {
            if (input instanceof ItemStack)
            {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.stackSize != 1)
                {
                    itemStack.stackSize = 1;
                }
            }
        }
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight)
    {
        if (!isVanilla())
        {
            IPokemob pokemob = getPokemob();

            if (pokemob == null) return;

            int num = pokemob.getPokedexNb();
            PokedexEntry entry;
            pokemob = EventsHandlerClient.renderMobs.get(entry = pokemob.getPokedexEntry());
            if (pokemob == null)
            {
                pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(num, minecraft.theWorld);
                if (pokemob == null) return;
                EventsHandlerClient.renderMobs.put(entry, pokemob);
            }
            GL11.glPushMatrix();
            GL11.glTranslated(102, 32, 10);
            GL11.glScaled(2, 2, 2);
            EventsHandlerClient.renderMob(pokemob, 0);
            GL11.glPopMatrix();
        }

    }

    @Nonnull
    @Override
    public List<?> getInputs()
    {
        return recipe.recipeItems;
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs()
    {
        return Collections.singletonList(recipe.getRecipeOutput());
    }

    public IPokemob getPokemob()
    {
        return recipe.getPokemob();
    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        if (recipe.energyCost > 0) { return Lists.newArrayList("RF Cost: " + recipe.energyCost); }
        return null;
    }

    public boolean isVanilla()
    {
        return recipe.vanilla;
    }
}
