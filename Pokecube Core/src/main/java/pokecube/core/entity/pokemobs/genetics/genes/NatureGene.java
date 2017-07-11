package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.Nature;
import thut.api.entity.genetics.Gene;

public class NatureGene implements Gene
{
    Random rand   = new Random();
    Nature nature = Nature.values()[rand.nextInt(Nature.values().length)];

    @Override
    public Gene interpolate(Gene other)
    {
        NatureGene newGene = new NatureGene();
        NatureGene otherG = (NatureGene) other;
        newGene.nature = getNature(nature, otherG.nature);
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        NatureGene newGene = new NatureGene();
        return newGene;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(getKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) nature;
    }

    @Override
    public <T> void setValue(T value)
    {
        nature = (Nature) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("V", (byte) nature.ordinal());
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        nature = Nature.values()[tag.getByte("V")];
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.NATUREGENE;
    }

    @Override
    public String toString()
    {
        return "" + nature;
    }

    private static Nature getNature(Nature nature, Nature nature2)
    {
        byte ret = 0;
        Random rand = new Random();
        byte[] motherMods = nature.getStatsMod();
        byte[] fatherMods = nature2.getStatsMod();
        byte[] sum = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            sum[i] = (byte) (motherMods[i] + fatherMods[i]);
        }
        int pos = 0;
        int start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
        {
            if (sum[(i + start) % 6] > 0)
            {
                pos = (i + start) % 6;
                break;
            }
        }
        int neg = 0;
        start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
        {
            if (sum[(i + start) % 6] < 0)
            {
                neg = (i + start) % 6;
                break;
            }
        }
        if (pos != 0 && neg != 0)
        {
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[i].getStatsMod()[pos] > 0 && Nature.values()[i].getStatsMod()[neg] < 0)
                {
                    ret = i;
                    break;
                }
            }
        }
        else if (pos != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[pos] > 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
            }
        }
        else if (neg != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[neg] < 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
            }
        }
        else
        {
            int num = rand.nextInt(5);
            ret = (byte) (num * 6);
        }
        return Nature.values()[ret];
    }

}
