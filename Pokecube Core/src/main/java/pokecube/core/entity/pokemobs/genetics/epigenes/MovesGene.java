package pokecube.core.entity.pokemobs.genetics.epigenes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class MovesGene implements Gene
{
    String[] moves = new String[4];

    @Override
    public Gene interpolate(Gene other)
    {
        MovesGene newGene = new MovesGene();
        MovesGene otherG = (MovesGene) other;
        for (int i = 0; i < moves.length; i++)
        {
            if (moves[i] == null) continue;
            for (int j = 0; i < otherG.moves.length; j++)
            {
                if (moves[i].equals(otherG.moves[j]))
                {
                    newGene.moves[i] = moves[i];
                    break;
                }
            }
        }
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        return this;
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
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
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
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.MOVESGENE;
    }

    @Override
    public boolean isEpigenetic()
    {
        return true;
    }

}
