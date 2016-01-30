package pokecube.compat.jei.berry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class BerryBrewingWrapper extends VanillaRecipeWrapper
{
    private final List<ItemStack> ingredients;
    private final ItemStack potionInput;
    private final ItemStack potionOutput;
    private final List<Object> inputs;
    private final int brewingSteps;
    private final int hashCode;

    public BerryBrewingWrapper(ItemStack ingredient, ItemStack potionInput, ItemStack potionOutput, int brewingSteps) {
        this(Collections.singletonList(ingredient), potionInput, potionOutput, brewingSteps);
    }

    public BerryBrewingWrapper(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, int brewingSteps) {
        this.ingredients = ingredients;
        this.potionInput = potionInput;
        this.potionOutput = potionOutput;
        this.brewingSteps = brewingSteps;

        this.inputs = new ArrayList<>();
        this.inputs.add(potionInput);
        this.inputs.add(potionInput);
        this.inputs.add(potionInput);
        this.inputs.add(ingredients);

        ItemStack firstIngredient = ingredients.get(0);

        this.hashCode = Objects.hashCode(potionInput.getMetadata(), potionOutput.getMetadata(), firstIngredient.getItem(), firstIngredient.getMetadata());
    }

    @Override
    public List<?> getInputs() {
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return Collections.singletonList(potionOutput);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrewingRecipeWrapper)) {
            return false;
        }
        BerryBrewingWrapper other = (BerryBrewingWrapper) obj;

        if (ingredients.size() != other.ingredients.size()) {
            return false;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            if (!ItemStack.areItemStacksEqual(ingredients.get(i), other.ingredients.get(i))) {
                return false;
            }
        }

        return true;
    }
    
    public int getBrewingSteps() {
        return brewingSteps;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return ingredients + " + " + potionInput + " = " + potionOutput;
    }
}
