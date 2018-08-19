package pokecube.core.entity.pokemobs.genetics.epigenes;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneByteArr;

public class EVsGene extends GeneByteArr
{
    public EVsGene()
    {
        value = new byte[6];
        for (int i = 0; i < 6; i++)
            value[i] = Byte.MIN_VALUE;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        // Don't actually interpolate the EVs.
        EVsGene newGene = new EVsGene();
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        EVsGene newGene = new EVsGene();
        newGene.value = value.clone();
        return newGene;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.EVSGENE;
    }

    @Override
    public float getEpigeneticRate()
    {
        return 1;
    }

}
