package pokecube.adventures.blocks.cloner;

import java.util.Random;

import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;

public interface IGeneSelector
{
    default Alleles merge(Alleles source, Alleles destination)
    {
        Random rand = new Random();
        Gene gene1 = source.getExpressed();
        Gene gene2 = destination.getExpressed();
        if (gene1.getEpigeneticRate() < rand.nextFloat())
        {
            gene1 = source.getAlleles()[rand.nextInt(2)];
            gene2 = destination.getAlleles()[rand.nextInt(2)];
            return new Alleles(gene1, gene2);
        }
        return new Alleles(source.getExpressed(), destination.getExpressed());
    }
}
