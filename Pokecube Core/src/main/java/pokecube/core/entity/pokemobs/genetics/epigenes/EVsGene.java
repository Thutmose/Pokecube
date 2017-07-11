package pokecube.core.entity.pokemobs.genetics.epigenes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneByteArr;

public class EVsGene extends GeneByteArr
{
    private static float MUTRATE = 0.1f;

    public EVsGene()
    {
        value = new byte[6];
        for (int i = 0; i < 6; i++)
            value[i] = Byte.MIN_VALUE;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        EVsGene newGene = new EVsGene();
        byte[] ret = newGene.value;
        EVsGene otherI = (EVsGene) other;
        Random rand = new Random();
        float chance = MUTRATE;
        for (int i = 0; i < 6; i++)
        {
            byte mi = value[i];
            byte fi = otherI.value[i];
            if (rand.nextFloat() < chance)
            {
                ret[i] = (byte) Math.max(fi, mi);
                continue;
            }
            byte iv = (byte) ((mi + fi) / 2);
            ret[i] = iv;
        }
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
