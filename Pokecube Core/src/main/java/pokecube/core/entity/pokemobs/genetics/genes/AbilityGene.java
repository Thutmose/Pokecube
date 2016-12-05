package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class AbilityGene implements Gene
{
    public static class AbilityObject
    {
        public String ability      = "";
        public byte   abilityIndex = 0;
    }

    protected AbilityObject ability = new AbilityObject();

    @Override
    public Gene interpolate(Gene other)
    {
        AbilityGene otherA = (AbilityGene) other;
        byte otherIndex = otherA.ability.abilityIndex;
        byte index = otherIndex == ability.abilityIndex ? otherIndex
                : Math.random() < 0.5 ? otherIndex : ability.abilityIndex;
        AbilityGene newGene = new AbilityGene();
        if (!otherA.ability.ability.isEmpty() && otherA.ability.ability.equals(ability))
            newGene.ability.ability = ability.ability;
        newGene.ability.abilityIndex = index;
        return newGene;
    }

    @Override
    public Gene mutate()
    {
        if (Math.random() < GeneticsManager.mutationRates.get(getKey()))
        {
            AbilityGene newGene = new AbilityGene();
            byte index = (byte) (ability.abilityIndex == 2 ? new Random().nextInt(2) : 2);
            newGene.ability.abilityIndex = index;
            return newGene;
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) ability;
    }

    @Override
    public <T> void setValue(T value)
    {
        ability = (AbilityObject) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("I", ability.abilityIndex);
        tag.setString("A", ability.ability);
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        ability.abilityIndex = tag.getByte("I");
        ability.ability = tag.getString("A");
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.ABILITYGENE;
    }

    @Override
    public boolean isEpigenetic()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return ability.abilityIndex + " " + ability.ability;
    }

}
