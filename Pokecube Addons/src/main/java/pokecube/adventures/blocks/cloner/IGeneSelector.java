package pokecube.adventures.blocks.cloner;

import thut.api.entity.genetics.Alleles;

public interface IGeneSelector
{
    default Alleles merge(Alleles source, Alleles destination)
    {
        Alleles ret = new Alleles(source.getExpressed(), destination.getExpressed());
        return ret;
    }
}
