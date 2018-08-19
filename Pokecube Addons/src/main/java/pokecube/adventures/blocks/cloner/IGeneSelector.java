package pokecube.adventures.blocks.cloner;

import java.util.Random;
import java.util.logging.Level;

import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.core.common.genetics.genes.GeneByteArr;
import thut.core.common.genetics.genes.GeneIntArray;

public interface IGeneSelector
{
    public static Gene copy(Gene geneIn) throws Exception
    {
        NBTTagCompound tag = GeneRegistry.save(geneIn);
        return GeneRegistry.load(tag);
    }

    default Alleles merge(Alleles source, Alleles destination)
    {
        Random rand = new Random();
        Gene geneSource = source.getExpressed();
        Gene geneDest = destination.getExpressed();
        if (geneSource.getEpigeneticRate() < rand.nextFloat())
        {
            geneSource = source.getAlleles()[rand.nextInt(2)];
            geneDest = destination.getAlleles()[rand.nextInt(2)];
        }
        return fromGenes(geneSource, geneDest);
    }

    default Alleles fromGenes(Gene geneSource, Gene geneDest)
    {
        if (arrIndex() >= 0)
        {
            try
            {
                if (geneSource instanceof GeneByteArr)
                {
                    byte[] source = geneSource.getValue();
                    geneSource = copy(geneDest);
                    byte[] dest = geneDest.getValue();
                    dest[arrIndex()] = source[arrIndex()];
                }
                else if (geneSource instanceof GeneIntArray)
                {
                    int[] source = geneSource.getValue();
                    geneSource = copy(geneDest);
                    int[] dest = geneDest.getValue();
                    dest[arrIndex()] = source[arrIndex()];
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error merging genes " + geneSource.getKey() + " " + arrIndex(), e);
            }
        }
        return new Alleles(geneSource, geneDest);
    }

    default int arrIndex()
    {
        return -1;
    }
}
