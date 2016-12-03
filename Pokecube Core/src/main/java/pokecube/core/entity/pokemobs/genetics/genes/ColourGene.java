package pokecube.core.entity.pokemobs.genetics.genes;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneIntArray;

public class ColourGene extends GeneIntArray
{
    public ColourGene()
    {
        value = new int[] { 127, 127, 127, 127 };
    }

    @Override
    public Gene interpolate(Gene other)
    {
        ColourGene otherC = (ColourGene) other;
        ColourGene newGene = new ColourGene();
        int[] ret = newGene.value;
        for (int i = 0; i < value.length; i++)
        {
            ret[i] = (((value[i] + otherC.value[i]) / 2));
        }
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        // TODO mutations for colour.
        return this;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.COLOURGENE;
    }

}
