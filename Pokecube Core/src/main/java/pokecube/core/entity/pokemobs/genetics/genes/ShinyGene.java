package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneBoolean;

public class ShinyGene extends GeneBoolean
{
    Random rand = new Random();

    @Override
    public Gene interpolate(Gene other)
    {
        ShinyGene newGene = new ShinyGene();
        ShinyGene otherG = (ShinyGene) other;
        newGene.value = otherG.value && value;
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        if (rand.nextFloat() < GeneticsManager.mutationRates.get(getKey()))
        {
            ShinyGene newGene = new ShinyGene();
            newGene.value = Boolean.TRUE;
            return newGene;
        }
        return this;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SHINYGENE;
    }

}
