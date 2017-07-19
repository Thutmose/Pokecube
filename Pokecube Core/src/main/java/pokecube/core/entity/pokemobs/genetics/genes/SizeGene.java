package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneFloat;

public class SizeGene extends GeneFloat
{
    public static float scaleFactor = 0.075f;
    Random              rand        = new Random();

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
        SizeGene newGene = new SizeGene();
        newGene.value = (1 + scaleFactor * (float) (new Random()).nextGaussian());
        return newGene;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(getKey());
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SIZEGENE;
    }

}
