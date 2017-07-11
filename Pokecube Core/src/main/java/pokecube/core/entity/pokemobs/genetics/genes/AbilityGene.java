package pokecube.core.entity.pokemobs.genetics.genes;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.abilities.Ability;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;

public class AbilityGene implements Gene
{
    public static class AbilityObject
    {
        // This value is only set when a pokemob makes the ability, so should
        // only exist in an expressed gene.
        public Ability abilityObject = null;
        // Have we searched for an ability yet, if not, will look for one first
        // time ability is got.
        public boolean searched      = false;
        public String  ability       = "";
        public byte    abilityIndex  = 0;
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
        AbilityGene newGene = new AbilityGene();
        byte index = (byte) (ability.abilityIndex == 2 ? new Random().nextInt(2) : 2);
        newGene.ability.abilityIndex = index;
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
    public String toString()
    {
        return ability.abilityIndex + " " + ability.ability;
    }

}
