package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneFloat;

public class SizeGene extends GeneFloat
{
    Random rand = new Random();

    public SizeGene()
    {
        value = 1f;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        SizeGene newGene = new SizeGene();
        SizeGene otherG = (SizeGene) other;
        newGene.value = rand.nextBoolean() ? otherG.value : value;
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        if (rand.nextFloat() < GeneticsManager.mutationRates.get(getKey()))
        {
            SizeGene newGene = new SizeGene();
            newGene.value = (1 + 0.075f * (float) (new Random()).nextGaussian());
            return newGene;
        }
        return this;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SIZEGENE;
    }

}
