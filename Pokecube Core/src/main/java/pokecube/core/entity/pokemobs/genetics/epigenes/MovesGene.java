package pokecube.core.entity.pokemobs.genetics.epigenes;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class MovesGene implements Gene
{
    private static final Comparator<String> SORTER = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            if (o1 == null && o2 != null) return 1;
            else if (o2 == null && o1 != null) return -1;
            return 0;
        }
    };

    private static final void cleanup(String[] moves)
    {
        outer:
        for (int i = 0; i < moves.length; i++)
        {
            String temp = moves[i];
            if (temp == null) continue;
            for (int j = i + 1; j < moves.length; j++)
            {
                String temp2 = moves[j];
                if (temp2 == null) continue;
                if (temp.equals(temp2))
                {
                    moves[j] = null;
                    continue outer;
                }
            }
        }
        Arrays.sort(moves, SORTER);
    }

    String[] moves = new String[4];

    @Override
    public Gene interpolate(Gene other)
    {
        MovesGene newGene = new MovesGene();
        MovesGene otherG = (MovesGene) other;
        for (int i = 0; i < moves.length; i++)
        {
            if (moves[i] == null) continue;
            for (int j = 0; j < otherG.moves.length; j++)
            {
                if (moves[i].equals(otherG.moves[j]))
                {
                    newGene.moves[i] = moves[i];
                    break;
                }
            }
        }
        newGene.setValue(newGene.moves);
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        MovesGene newGene = new MovesGene();
        newGene.moves = moves.clone();
        return newGene;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) moves;
    }

    @Override
    public <T> void setValue(T value)
    {
        moves = (String[]) value;
        cleanup(moves);
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        cleanup(moves);
        for (int i = 0; i < moves.length; i++)
        {
            if (moves[i] != null)
            {
                tag.setString("" + i, moves[i]);
            }
        }
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        for (int i = 0; i < moves.length; i++)
        {
            if (tag.hasKey("" + i))
            {
                moves[i] = tag.getString("" + i);
            }
        }
        cleanup(moves);
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.MOVESGENE;
    }

    @Override
    public float getEpigeneticRate()
    {
        return 1;
    }

    @Override
    public String toString()
    {
        return Arrays.toString(moves);
    }

}
