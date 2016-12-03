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
        byte[] ret = newGene.value;
        Random rand = new Random();
        float chance = MUTRATE;
        for (int i = 0; i < 6; i++)
        {
            if (rand.nextFloat() > chance) continue;
            int mi = rand.nextInt(value[i] + 129);
            int fi = rand.nextInt(value[i] + 129);
            int iv = (Math.min(mi + fi, 255));
            ret[i] = (byte) (iv - 128);
        }
        return newGene;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.EVSGENE;
    }

    @Override
    public boolean isEpigenetic()
    {
        return true;
    }

}
