package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneByteArr;

public class IVsGene extends GeneByteArr
{
    public IVsGene()
    {
        value = new byte[6];
    }

    @Override
    public Gene interpolate(Gene other)
    {
        IVsGene newGene = new IVsGene();
        byte[] ret = newGene.value;
        IVsGene otherI = (IVsGene) other;
        Random rand = new Random();
        float chance = GeneticsManager.mutationRates.get(getKey());
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
        IVsGene newGene = new IVsGene();
        byte[] ret = newGene.value;
        Random rand = new Random();
        float chance = GeneticsManager.mutationRates.get(getKey());
        for (int i = 0; i < 6; i++)
        {
            if (rand.nextFloat() > chance) continue;
            byte mi = (byte) rand.nextInt(value[i] + 1);
            byte fi = (byte) rand.nextInt(value[i] + 1);
            byte iv = (byte) (Math.min(mi + fi, 31));
            ret[i] = iv;
        }
        return newGene;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.IVSGENE;
    }

}
