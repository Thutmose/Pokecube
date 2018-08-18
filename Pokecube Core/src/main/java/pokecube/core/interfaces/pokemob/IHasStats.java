package pokecube.core.interfaces.pokemob;

import net.minecraft.entity.IEntityOwnable;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemob.StatModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
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
        byte[] evs = getEVs().clone();

        // Assign the values, cap the EVs at Byte.MAX_VALUE
        for (int i = 0; i < 6; i++)
        {
            if (evs[i] + 128 + evsToAdd[i] <= 255 && evs[i] + 128 + evsToAdd[i] >= 0)
            {
                evs[i] = (byte) (evs[i] + evsToAdd[i]);
            }
            else
            {
                evs[i] = Byte.MAX_VALUE;
            }
        }

        int sum = 0;

        // Cap to 510 EVs
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

    /** @return The actual ability object for this pokemob. */
    Ability getAbility();

    /** @return Index of ability, 0 and 1 are "normal" abilities, above 1 are
     *         "hidden" abilities. */
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
                : getGeneralState(GeneralStates.DENYCAPTURE) ? 0
                        : getEntity() instanceof IEntityOwnable ? getPokedexEntry().getCatchRate() : 0;
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

    /** @return Scale factor for this mob, this is applied linearly to each
     *         dimension of the mob. */
    float getSize();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stat */
    default int getStat(Stats stat, boolean modified)
    {
        return Math.max(1, (int) getModifiers().getStat(this, stat, modified));
    }

    /** Gets the stat as a float, this is used for things like evasion/accuracy
     * which are not integer values.
     * 
     * @param stat
     * @param modified
     * @return the stat */
    default float getFloatStat(Stats stat, boolean modified)
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

    /** Gets the weight of the pokemob, this scaled by the value from
     * {@link IHasStats#getSize()}
     * 
     * @return */
    default double getWeight()
    {
        return this.getSize() * this.getSize() * this.getSize() * getPokedexEntry().mass;
    }

    /** @param typeIn
     * @return Are we typeIn */
    default boolean isType(PokeType typeIn)
    {
        return this.getType1() == typeIn || getType2() == typeIn;
    }

    /** Sets the ability object for the pokemob
     * 
     * @param ability */
    void setAbility(Ability ability);

    /** Sets the ability index for the pokemob, see
     * {@link IHasStats#getAbilityIndex()}
     * 
     * @param index */
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

    /** Sets the size for this mob, see {@link IHasStats#getSize()}
     * 
     * @param size */
    void setSize(float size);

    /** Sets ability index to 2. */
    default void setToHiddenAbility()
    {
        this.setAbilityIndex(2);
        this.setAbility(getPokedexEntry().getHiddenAbility(CapabilityPokemob.getPokemobFor(getEntity())));
    }

    /** Sets first type
     * 
     * @param type1 */
    default void setType1(PokeType type1)
    {
        getModifiers().type1 = type1;
    }

    /** Sets second type
     * 
     * @param type2 */
    default void setType2(PokeType type2)
    {
        getModifiers().type2 = type2;
    }
}
