package pokecube.core.interfaces.pokemob;

import net.minecraft.entity.EntityLiving;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public interface IHasEntry extends IHasMobAIStates
{
    /** @return the minecraft entity associated with this pokemob */
    default EntityLiving getEntity()
    {
        return (EntityLiving) this;
    }

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    PokedexEntry getPokedexEntry();

    /** @return the int pokedex number */
    default Integer getPokedexNb()
    {
        return getPokedexEntry().getPokedexNb();
    }

    /** @return is this a shadow pokemob */
    default boolean isShadow()
    {
        return getPokedexEntry().isShadowForme;
    }

    /** @return is the pokemob shiny */
    boolean isShiny();

    /** @param entityIn
     *            Sets the vanilla entity for this pokemob */
    default void setEntity(EntityLiving entityIn)
    {
        // Nope for default impl
    }

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    IPokemob setPokedexEntry(PokedexEntry newEntry);
}
