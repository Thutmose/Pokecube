package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;
import java.util.logging.Level;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.genetics.genes.GeneByteArr;

public class IVsGene extends GeneByteArr
{
    public IVsGene()
    {
        Random rand = new Random();
        value = new byte[] { Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand),
                Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand) };
    }

    @Override
    public Gene interpolate(Gene other)
    {
        IVsGene newGene = new IVsGene();
        byte[] ret = newGene.value;
        IVsGene otherI = (IVsGene) other;
        for (int i = 0; i < 6; i++)
        {
            byte mi = value[i];
            byte fi = otherI.value[i];
            byte iv = (byte) ((mi + fi) / 2);
            ret[i] = iv;
        }
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        IVsGene newGene = new IVsGene();
        newGene.value = value.clone();
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

    public Gene mutate(IMobGenetics parent1, IMobGenetics parent2)
    {
        Alleles evs1 = parent1.getAlleles().get(GeneticsManager.EVSGENE);
        Alleles evs2 = parent2.getAlleles().get(GeneticsManager.EVSGENE);
        Alleles ivs1 = parent1.getAlleles().get(GeneticsManager.IVSGENE);
        Alleles ivs2 = parent2.getAlleles().get(GeneticsManager.IVSGENE);
        IVsGene newGene = new IVsGene();
        newGene.value = value.clone();
        if (evs1 == null || evs2 == null || ivs1 == null || ivs2 == null)
        {
            // No Mutation, return clone of this gene.
            PokecubeMod.log(Level.WARNING, "Someone has null genes: " + evs1 + " " + evs2 + " " + ivs1 + " " + ivs2
                    + " " + parent1 + " " + parent2);
            return newGene;
        }
        Random rand = new Random();
        EVsGene gene1 = evs1.getExpressed();
        EVsGene gene2 = evs2.getExpressed();
        byte[] ret = newGene.value;
        byte[] ev1 = gene1.getValue();
        byte[] ev2 = gene2.getValue();
        byte[] iv1 = ivs1.getExpressed().getEpigeneticRate() > rand.nextFloat() ? ivs1.getExpressed().getValue()
                : ivs1.getAlleles()[rand.nextInt(2)].getValue();
        byte[] iv2 = ivs2.getExpressed().getEpigeneticRate() > rand.nextFloat() ? ivs2.getExpressed().getValue()
                : ivs2.getAlleles()[rand.nextInt(2)].getValue();
        for (int i = 0; i < 6; i++)
        {
            int v = (ev1[i] + ev2[2]) / 2;
            GeneticsManager.epigeneticParser.setVarValue("v", v);
            byte mi = (byte) rand.nextInt((int) (iv1[i] + 1 + GeneticsManager.epigeneticParser.getValue()));
            byte fi = (byte) rand.nextInt((int) (iv2[i] + 1 + GeneticsManager.epigeneticParser.getValue()));
            byte iv = (byte) (Math.min(mi + fi, 31));
            ret[i] = iv;
        }
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
        return GeneticsManager.IVSGENE;
    }

}
