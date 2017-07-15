package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.genetics.Gene;

public class SpeciesGene implements Gene
{
    public static byte getSexe(int baseValue, Random random)
    {
        if (baseValue == 255) { return IPokemob.NOSEXE; }
        if (random.nextInt(255) >= baseValue) { return IPokemob.MALE; }
        return IPokemob.FEMALE;
    }

    public static class SpeciesInfo
    {
        public byte         value;
        public PokedexEntry entry;

        NBTTagCompound save()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("G", value);
            if (entry != null) tag.setString("E", entry.getName());
            return tag;
        }

        void load(NBTTagCompound tag)
        {
            value = tag.getByte("G");
            entry = Database.getEntry(tag.getString("E"));
        }

        public SpeciesInfo clone()
        {
            SpeciesInfo info = new SpeciesInfo();
            info.value = value;
            info.entry = entry;
            return info;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SpeciesInfo)) return false;
            SpeciesInfo info = (SpeciesInfo) obj;
            return value == info.value && (entry == null ? true : entry.equals(info.entry));
        }

        @Override
        public String toString()
        {
            return entry + " " + value;
        }
    }

    SpeciesInfo info = new SpeciesInfo();
    Random      rand = new Random();

    /** The value here is of format {gender, ratio}. */
    public SpeciesGene()
    {
        info.value = 0;
    }

    @Override
    public Gene interpolate(Gene other)
    {
        SpeciesGene newGene = new SpeciesGene();
        SpeciesGene otherG = (SpeciesGene) other;
        SpeciesGene mother = info.value == IPokemob.FEMALE ? this : info.value > 0 ? this : otherG;
        if (info.value == otherG.info.value) mother = rand.nextFloat() < 0.5 ? this : otherG;
        SpeciesGene father = mother == otherG ? this : otherG;
        newGene.setValue(mother.info.clone());
        newGene.info.entry = newGene.info.entry.getChild(father.info.entry);
        newGene.mutate();
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        SpeciesGene newGene = new SpeciesGene();
        newGene.setValue(info.clone());
        newGene.info.value = getSexe(newGene.info.entry.getSexeRatio(), rand);
        return newGene;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SPECIESGENE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) info;
    }

    @Override
    public <T> void setValue(T value)
    {
        info = (SpeciesInfo) value;
    }

    @Override
    public float getEpigeneticRate()
    {
        return GeneticsManager.mutationRates.get(getKey());
    }

    @Override
    public float getMutationRate()
    {
        return 1;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("V", info.save());
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        info.load(tag.getCompoundTag("V"));
    }

    @Override
    public String toString()
    {
        return info.toString();
    }
}
