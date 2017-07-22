package pokecube.core.interfaces.pokemob;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemob.StatModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public interface IHasStats extends IHasEntry
{
    /** At the end of a fight as a XP. {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evsToAdd
     *            the Effort Values to add */
    default void addEVs(byte[] evsToAdd)
    {
        byte[] evs = getEVs();
        for (int i = 0; i < 6; i++)
        {
            if (evs[i] + 128 + evsToAdd[i] <= 255 && evs[i] + 128 + evsToAdd[i] >= 0)
            {
                evs[i] = (byte) (evs[i] + evsToAdd[i]);
            }
            else
            {
                evs[i] = (byte) 127;
            }
        }

        int sum = 0;

        for (byte ev : evs)
        {
            sum += ev + 128;
        }

        if (sum < 510)
        {
            setEVs(evs);
        }
    }

    /** Bulk setting of all moves. This array must have length of 4. */
    void setMoves(String[] moves);

    /** adds to how happy is the pokemob, see {@link HappinessType} */
    void addHappiness(int toAdd);

    Ability getAbility();

    int getAbilityIndex();

    /** Computes an attack strength from stats. Only used against non-poke-mobs.
     *
     * @return the attack strength */
    default float getAttackStrength()
    {
        int ATT = getStat(Stats.ATTACK, true);
        int ATTSPE = getStat(Stats.SPATTACK, true);
        float mult = getPokedexEntry().isShadowForme ? 2 : 1;
        return mult * ((ATT + ATTSPE) / 6f);
    }

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stats */
    default int getBaseStat(Stats stat)
    {
        if (stat.ordinal() > 5) return 1;
        return getPokedexEntry().getStats()[stat.ordinal()];
    }

    /** To compute exp at the end of a fight.
     *
     * @return in base XP */
    default int getBaseXP()
    {
        return getPokedexEntry().getBaseXP();
    }

    /** Pokecube catch rate.
     *
     * @return the catch rate */
    default int getCatchRate()
    {
        return getPokedexEntry().isShadowForme ? 0
                : getPokemonAIState(DENYCAPTURE) ? 0 : getPokedexEntry().getCatchRate();
    }

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Effort Values */
    byte[] getEVs();

    /** @return all the experience */
    int getExp();

    /** 0, 1, 2, or 3 {@link Tools#xpToLevel(int, int)}
     *
     * @return in evolution mode */
    default int getExperienceMode()
    {
        return getPokedexEntry().getEvolutionMode();
    }

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Individual Values */
    byte[] getIVs();

    /** @return the level 1-100 */
    default int getLevel()
    {
        return Tools.xpToLevel(getExperienceMode(), getExp());
    }

    /** @return the Modifiers on stats */
    StatModifiers getModifiers();

    /** {@link IMoveConstants#HARDY} for an example of a nature byte
     * 
     * @return the nature */
    Nature getNature();

    float getSize();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stats */
    default int getStat(Stats stat, boolean modified)
    {
        return getModifiers().getStat(this, stat, modified);
    }

    /** Returns 1st type.
     * 
     * @see PokeType
     * @return the byte type */
    default PokeType getType1()
    {
        return getModifiers().type1 != null ? getModifiers().type1 : getPokedexEntry().getType1();
    }

    /** Returns 2nd type.
     * 
     * @see PokeType
     * @return the byte type */
    default PokeType getType2()
    {
        return getModifiers().type2 != null ? getModifiers().type2 : getPokedexEntry().getType2();
    }

    default double getWeight()
    {
        return this.getSize() * this.getSize() * this.getSize() * getPokedexEntry().mass;
    }

    default boolean isType(PokeType typeIn)
    {
        return this.getType1() == typeIn || getType2() == typeIn;
    }

    void setAbility(Ability ability);

    void setAbilityIndex(int index);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs
     *            the Effort Values */
    void setEVs(byte[] evs);

    /** Sets the experience.
     *
     * @param exp
     * @param notifyLevelUp
     *            should be false in an initialize step and true in a true exp
     *            earning */
    IPokemob setExp(int exp, boolean notifyLevelUp);

    void setHp(float min);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs
     *            the Individual Values */
    void setIVs(byte[] ivs);

    /** Sets the pokemobs's nature {@link IMoveConstants#HARDY} for an example
     * of a nature byte
     * 
     * @param nature */
    void setNature(Nature nature);

    void setSize(float size);

    default void setToHiddenAbility()
    {
        this.setAbilityIndex(2);
        this.setAbility(getPokedexEntry().getHiddenAbility(CapabilityPokemob.getPokemobFor(getEntity())));
    }

    default void setType1(PokeType type1)
    {
        getModifiers().type1 = type1;
    }

    default void setType2(PokeType type2)
    {
        getModifiers().type2 = type2;
    }
}
