package pokecube.core.interfaces.pokemob;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IHasOwner extends IEntityOwnable, IHasMobAIStates
{
    /** Displays a message in the console of the owner player (if this pokemob
     * is tamed).
     * 
     * @param message */
    void displayMessageToOwner(ITextComponent message);

    /** @return UUID of original Trainer, used to prevent nicknaming of traded
     *         pokemobs */
    UUID getOriginalOwnerUUID();

    @Nullable
    @Override
    default Entity getOwner()
    {
        return getPokemonOwner();
    }

    @Nullable
    @Override
    default UUID getOwnerId()
    {
        return getPokemonOwnerID();
    }

    /** Returns the pokecube id to know whether its a greatcube, ultracube...
     * 
     * @return the shifted index of the item */
    ItemStack getPokecube();

    @Nonnull
    String getPokemobTeam();

    /** @return the String nickname */
    String getPokemonNickname();

    /** Gets the owner as an EntityLivingBase, may be null if not in world, or
     * if no owner. */
    EntityLivingBase getPokemonOwner();

    /** Gets the UUID of the owner, might be null */
    UUID getPokemonOwnerID();

    boolean isPlayerOwned();

    /** Sets owner uuid
     * 
     * @param original
     *            trainer's UUID */
    void setOriginalOwnerUUID(UUID original);

    /** Sets the pokecube id to know whether its a greatcube, ultracube...
     * 
     * @param pokeballId */
    void setPokecube(ItemStack pokecube);

    void setPokemobTeam(@Nonnull String team);

    /** Sets the nickname */
    void setPokemonNickname(String nickname);

    /** from wolf code */
    void setPokemonOwner(EntityLivingBase e);

    /** from wolf code */
    void setPokemonOwner(UUID id);

    default void setTraded(boolean trade)
    {
        setPokemonAIState(TRADED, trade);
    }

    /** Has pokemob been traded
     * 
     * @return */
    default boolean traded()
    {
        return getPokemonAIState(TRADED);
    }
}
