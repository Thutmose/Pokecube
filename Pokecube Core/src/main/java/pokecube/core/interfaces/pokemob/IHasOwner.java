package pokecube.core.interfaces.pokemob;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

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
    /** @return Team we are on, guarding pokemobs shouldn't attack team
     *         members. */
    String getPokemobTeam();

    /** @return the String nickname */
    String getPokemonNickname();

    /** Gets the owner as an EntityLivingBase, may be null if not in world, or
     * if no owner. */
    EntityLivingBase getPokemonOwner();

    /** Gets the UUID of the owner, might be null */
    UUID getPokemonOwnerID();

    /** @return Is our owner a player. */
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

    /** Sets the team we are on, this is used for things like guarding
     * 
     * @param team */
    void setPokemobTeam(@Nonnull String team);

    /** Sets the nickname */
    void setPokemonNickname(String nickname);

    /** sets owner by specific entity. */
    void setPokemonOwner(EntityLivingBase e);

    /** sets owner by UUID */
    void setPokemonOwner(UUID id);

    /** Sets that we are traded.
     * 
     * @param trade */
    default void setTraded(boolean trade)
    {
        setGeneralState(GeneralStates.TRADED, trade);
    }

    /** Has pokemob been traded
     * 
     * @return */
    default boolean traded()
    {
        return getGeneralState(GeneralStates.TRADED);
    }

    /** @return the controller object for when this is ridden */
    default LogicMountedControl getController()
    {
        return null;
    }

    /** Sets the direction this mob is going when ridden, if the mob is not
     * ridden, this method should do nothing. */
    void setHeading(float heading);

    /** @return The direction this mob is going, only relevant when ridden. */
    float getHeading();

    /** Additional NBT tag for addons to save stuff specific to the pokemob in
     * 
     * @return */
    NBTTagCompound getExtraData();
}
