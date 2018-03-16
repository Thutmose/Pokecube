package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.genetics.genes.GeneIntArray;

public class ColourGene extends GeneIntArray
{
    /** The higher this value, the more likely for mobs to range in colour. It
     * is very sensitive to the size of this number. */
    private static final double colourDiffFactor = 0.25;

    public ColourGene()
    {
        value = new int[] { 127, 127, 127, 255 };
        setRandomColour();
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
        ColourGene mutate = new ColourGene();
        mutate.setRandomColour();
        return mutate;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(getKey());
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.COLOURGENE;
    }

    void setRandomColour()
    {
        Random r = new Random();
        int first = r.nextInt(3);
        byte red = 127, green = 127, blue = 127;
        if (first == 0)
        {
            int min = 0;
            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = green < 63 ? 63 : 0;

            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
        }
        if (first == 1)
        {
            int min = 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = green < 63 ? 63 : 0;

            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
        }
        if (first == 2)
        {
            int min = 0;
            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = blue < 63 ? 63 : 0;

            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);

        }
        value[0] = red + 128;
        value[1] = green + 128;
        value[2] = blue + 128;
    }
}
