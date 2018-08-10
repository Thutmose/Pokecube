package pokecube.core.interfaces.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.PokecubeCore;
import pokecube.core.events.MoveUse;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.PokemobMoveStats;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public interface IHasMoves extends IHasStats
{
    /** Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example. The set can
     * fail because the mob is immune against this change or because it already
     * has the change. If so, the method returns false.
     * 
     * @param change
     *            the change to add
     * @return whether the change has actually been added */
    default boolean addChange(int change)
    {
        int old = getMoveStats().changes;
        getMoveStats().changes |= change;
        return getMoveStats().changes != old;
    }

    /** Used by Gui Pokedex. Exchange the two moves.
     *
     * @param moveIndex0
     *            index of 1st move
     * @param moveIndex1
     *            index of 2nd move */
    default void exchangeMoves(int moveIndex0, int moveIndex1)
    {
        if (PokecubeCore.isOnClientSide() && getPokemonAIState(IMoveConstants.TAMED))
        {
            String[] moves = getMoves();
            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
                getMoveStats().num++;
            }
            try
            {
                PacketCommand.sendCommand((IPokemob) this, Command.SWAPMOVES,
                        new SwapMovesHandler((byte) moveIndex0, (byte) moveIndex1));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            String[] moves = getMoves();

            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
                getMoveStats().num++;
            }
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
                int index = Math.min(moveIndex0, moveIndex1);
                if (getMove(4) == null || index > 3) return;
                String move = getMove(4);
                getMoveStats().newMoves.remove(move);
                setMove(index, move);
                PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
            }
            else
            {
                String move0 = moves[moveIndex0];
                String move1 = moves[moveIndex1];
                if (move0 != null && move1 != null)
                {
                    moves[moveIndex0] = move1;
                    moves[moveIndex1] = move0;
                }
                setMoves(moves);
            }
        }
    }

    /** Called by attackEntity(Entity entity, float f). Executes the move it's
     * supposed to do according to his trainer command or a random one if it's
     * wild.
     * 
     * @param target
     *            the Entity to attack
     * @param f
     *            the float parameter of the attackEntity method */
    void executeMove(Entity target, Vector3 targetLocation, float f);

    int getAttackCooldown();

    void onSetTarget(EntityLivingBase entity);

    int getTargetID();

    void setTargetID(int id);

    void setActiveMove(EntityMoveUse move);

    EntityMoveUse getActiveMove();

    /** Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example.
     *
     * @return the change state */
    default int getChanges()
    {
        return getMoveStats().changes;
    }

    String getLastMoveUsed();

    /** Gets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @return the String name of the move */
    default String getMove(int index)
    {
        IPokemob to = CapabilityPokemob.getPokemobFor(getTransformedTo());
        if (to != null && getTransformedTo() == null) { return to.getMove(index); }

        String[] moves = getMoves();

        if (index >= 0 && index < 4) { return moves[index]; }
        if (index == 4 && moves[3] != null)
        {
            if (!getMoveStats().newMoves.isEmpty()) { return getMoveStats().newMoves
                    .get(getMoveStats().num % getMoveStats().newMoves.size()); }
        }

        if (index == 5) { return IMoveConstants.MOVE_NONE; }
        return null;
    }

    /** Returns the index of the move to be executed in executeMove method.
     * 
     * @return the index from 0 to 3; */
    public int getMoveIndex();

    /** Returns all the 4 available moves name.
     *
     * @return an array of 4 {@link String} */
    String[] getMoves();

    PokemobMoveStats getMoveStats();

    Entity getTransformedTo();

    EntityAIBase getUtilityMoveAI();

    default Entity getWeapon(int index)
    {
        return index == 0 ? getMoveStats().weapon1 : getMoveStats().weapon2;
    }

    /** The pokemob learns the specified move. It will be set to an available
     * position or erase an existing one if non are available.
     *
     * @param moveName
     *            an existing move (registered in {@link MovesUtils}) */
    default void learn(String moveName)
    {
        if (moveName == null || getEntity().getEntityWorld() == null || getEntity().getEntityWorld().isRemote) return;
        if (!MovesUtils.isMoveImplemented(moveName)) { return; }
        String[] moves = getMoves();
        EntityLivingBase thisEntity = getEntity();
        IPokemob thisMob = CapabilityPokemob.getPokemobFor(thisEntity);
        // check it's not already known or forgotten
        for (String move : moves)
        {
            if (moveName.equals(move)) return;
        }

        if (thisMob.getPokemonOwner() != null && !thisEntity.isDead)
        {
            ITextComponent move = new TextComponentTranslation(MovesUtils.getUnlocalizedMove(moveName));
            ITextComponent mess = new TextComponentTranslation("pokemob.move.notify.learn",
                    thisMob.getPokemonDisplayName(), move);
            thisMob.displayMessageToOwner(mess);
        }
        if (moves[0] == null)
        {
            setMove(0, moveName);
        }
        else if (moves[1] == null)
        {
            setMove(1, moveName);
        }
        else if (moves[2] == null)
        {
            setMove(2, moveName);
        }
        else if (moves[3] == null)
        {
            setMove(3, moveName);
        }
        else
        {
            if (getPokemonAIState(IMoveConstants.TAMED))
            {
                if (moves[3] != null)
                {
                    for (String s : moves)
                    {
                        if (s == null) continue;
                        if (s.equals(moveName)) return;
                    }
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "",
                            thisMob.getPokemonDisplayName().getFormattedText(),
                            new TextComponentTranslation(MovesUtils.getUnlocalizedMove(moveName)));
                    thisMob.displayMessageToOwner(mess);
                    if (!getMoveStats().newMoves.contains(moveName))
                    {
                        getMoveStats().newMoves.add(moveName);
                        PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
                    }
                    return;
                }
            }
            else
            {
                int index = thisEntity.getRNG().nextInt(4);
                setMove(index, moveName);
            }
        }
    }

    /** This is called during move use to both the attacker and the attacked
     * entity, in that order. This can be used to add in abilities, In
     * EntityMovesPokemob, this is used for accounting for moves like curse,
     * detect, protect, etc, moves which either have different effects per
     * pokemon type, or moves that prevent damage.
     * 
     * @param move */
    default void onMoveUse(MovePacket move)
    {
        Event toPost = move.pre ? new MoveUse.DuringUse.Pre(move, move.attacker == getEntity())
                : new MoveUse.DuringUse.Post(move, move.attacker == getEntity());
        PokecubeCore.MOVE_BUS.post(toPost);
    }

    default void healChanges()
    {
        this.getMoveStats().changes = 0;
        IOngoingAffected affected = CapabilityAffected.getAffected(getEntity());
        if (affected != null)
        {
            affected.removeEffects(NonPersistantStatusEffect.ID);
        }
    }

    /** @param change
     *            the changes to set */
    default void removeChange(int change)
    {
        this.getMoveStats().changes -= change;
        IOngoingAffected affected = CapabilityAffected.getAffected(getEntity());
        if (affected != null)
        {
            Effect toRemove = Effect.getStatus((byte) change);
            for (IOngoingEffect effect : affected.getEffects(NonPersistantStatusEffect.ID))
            {
                if (effect instanceof NonPersistantStatusEffect
                        && ((NonPersistantStatusEffect) effect).effect == toRemove)
                {
                    affected.removeEffect(effect);
                    break;
                }
            }
        }
    }

    void setAttackCooldown(int timer);

    default void setLeaningMoveIndex(int num)
    {
        this.getMoveStats().num = num;
    }

    /** Sets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @param moveName */
    void setMove(int i, String moveName);

    /** Sets the move index.
     * 
     * @param i
     *            must be a value from 0 to 3 */
    public void setMoveIndex(int i);

    /** Same as {@link IHasMoves#setStatus(byte)} but also specifies the
     * duration for the effect.
     * 
     * @param status
     *            the status to set
     * @param turns
     *            How many times attackCooldown should the status apply.
     * @return whether the status has actually been set */
    boolean setStatus(byte status, int turns);

    /** Statuses: {@link IMoveConstants#STATUS_PSN} for example. The set can
     * fail because the mob is immune against this status (a fire-type Pokemon
     * can't be burned for example) or because it already have a status. If so,
     * the method returns false.
     * 
     * @param status
     *            the status to set
     * @return whether the status has actually been set */
    boolean setStatus(byte status);

    /** Sets the initial status timer. The timer will be decreased until 0. The
     * timer for SLP. When reach 0, the mob wakes up.
     * 
     * @param timer
     *            the initial value to set */
    void setStatusTimer(short timer);

    void setTransformedTo(Entity to);

    /** Used by moves such as vine whip to set the pokemob as using something.
     * 
     * @param index
     * @param weapon */
    default void setWeapon(int index, Entity weapon)
    {
        if (index == 0) getMoveStats().weapon1 = weapon;
        else getMoveStats().weapon2 = weapon;
    }

    void setDisableTimer(int index, int timer);

    int getDisableTimer(int index);
}
