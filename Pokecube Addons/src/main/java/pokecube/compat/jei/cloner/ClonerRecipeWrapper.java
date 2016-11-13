package pokecube.compat.jei.cloner;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class ClonerRecipeWrapper implements ICraftingRecipeWrapper
{

    @Nonnull
    private final RecipeFossilRevive recipe;

    public ClonerRecipeWrapper(@Nonnull RecipeFossilRevive recipe)
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
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
        IPokemob pokemob = getPokemob();
        if (pokemob == null) return;
        PokedexEntry entry;
        pokemob = EventsHandlerClient.renderMobs.get(entry = pokemob.getPokedexEntry());
        if (pokemob == null)
        {
            pokemob = (IPokemob) PokecubeMod.core.createPokemob(entry, minecraft.theWorld);
            if (pokemob == null) return;
            EventsHandlerClient.renderMobs.put(entry, pokemob);
        }
        GL11.glPushMatrix();
        GL11.glTranslated(102, 36, 10);
        double scale = 1.2;
        GL11.glScaled(scale, scale, scale);
        EventsHandlerClient.renderMob(pokemob, 0);
        GL11.glPopMatrix();
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

    @SuppressWarnings("unchecked")
    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputs(ItemStack.class, (List<ItemStack>) getInputs());
    }

    @Override
    public List<FluidStack> getFluidInputs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
