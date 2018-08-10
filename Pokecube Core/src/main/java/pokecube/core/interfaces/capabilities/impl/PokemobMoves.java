package pokecube.core.interfaces.capabilities.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketSyncMoveUse;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public abstract class PokemobMoves extends PokemobSexed
{

    @Override
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        String currentMove = getMove(getMoveIndex());
        if (currentMove == MOVE_NONE || currentMove == null) { return; }

        if (targetLocation == null && target != null)
        {
            targetLocation = Vector3.getNewVector().set(target);
        }

        String attack;
        if (this.getPokemonAIState(IMoveConstants.TAMED))
        {
            // A tamed pokemon should not attack a player
            // but it must keep it as a target.
            attack = getMove(getMoveIndex());
            if (attack == null)
            {
                new Exception().getStackTrace();
                return;
            }

            if (attack.equalsIgnoreCase(MOVE_METRONOME))
            {
                attack = null;
                ArrayList<MoveEntry> moves = new ArrayList<MoveEntry>(MoveEntry.values());
                while (attack == null)
                {
                    Collections.shuffle(moves);
                    MoveEntry move = moves.iterator().next();
                    if (move != null) attack = move.name;

                }
            }
        }
        else
        {
            // TODO do not pick a disabled move.
            if (moveIndexCounter++ > rand.nextInt(3))
            {
                int nb = rand.nextInt(5);
                String move = getMove(nb);
                while (move == null && nb > 0)
                {
                    nb = rand.nextInt(nb);
                }
                moveIndexCounter = 0;
                setMoveIndex(nb);
            }
            attack = getMove(getMoveIndex());
        }
        int index = getMoveIndex();
        if (index < 4 && index >= 0)
        {
            if (getDisableTimer(index) > 0)
            {
                attack = "struggle";
            }
        }
        Move_Base move = MovesUtils.getMoveFromName(attack);
        if (move == null || move.move == null)
        {
            PokecubeMod.log(Level.SEVERE,
                    getPokemonDisplayName().getFormattedText() + " Has Used Unregistered Move: " + attack + " " + index,
                    new IllegalArgumentException());
            return;
        }
        boolean distanced = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0;
        this.setAttackCooldown(MovesUtils.getAttackDelay(this, attack, distanced, target instanceof EntityPlayer));

        PacketSyncMoveUse.sendUpdate(this);
        if (target != getEntity())
        {
            if (target instanceof EntityLiving)
            {
                EntityLiving t = (EntityLiving) target;
                if (t.getAttackTarget() != getEntity())
                {
                    t.setAttackTarget(getEntity());
                }
            }
            if (target instanceof EntityLivingBase)
            {
                if (((EntityLivingBase) target).getRevengeTarget() != getEntity())
                {
                    ((EntityLivingBase) target).setRevengeTarget(getEntity());
                    getEntity().setRevengeTarget((EntityLivingBase) target);
                }
            }
        }
        int statusChange = getChanges();
        if ((statusChange & CHANGE_FLINCH) != 0)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "red",
                    getPokemonDisplayName());
            displayMessageToOwner(mess);
            IPokemob targetMob = CapabilityPokemob.getPokemobFor(getEntity().getAttackTarget());
            if (targetMob != null)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "green", getPokemonDisplayName());
                targetMob.displayMessageToOwner(mess);
            }
            removeChange(CHANGE_FLINCH);
            return;
        }

        if ((statusChange & CHANGE_CONFUSED) != 0)
        {
            if (Math.random() > 0.75)
            {
                removeChange(CHANGE_CONFUSED);
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "green",
                        getPokemonDisplayName());
                IPokemob targetMob = CapabilityPokemob.getPokemobFor(getEntity().getAttackTarget());
                if (targetMob != null)
                {
                    mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "red",
                            getPokemonDisplayName());
                    targetMob.displayMessageToOwner(mess);
                }
                displayMessageToOwner(mess);
            }
            else if (Math.random() > 0.5)
            {
                MovesUtils.doAttack("pokemob.status.confusion", this, getEntity());
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "red",
                        getPokemonDisplayName());
                IPokemob targetMob = CapabilityPokemob.getPokemobFor(getEntity().getAttackTarget());
                if (targetMob != null)
                {
                    mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "green",
                            getPokemonDisplayName());
                    targetMob.displayMessageToOwner(mess);
                }
                displayMessageToOwner(mess);
                return;
            }
        }

        if (getMoveStats().infatuateTarget != null)
        {
            if (getMoveStats().infatuateTarget.isDead)
            {
                getMoveStats().infatuateTarget = null;
            }
            else if (Math.random() > 0.5)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.infatuate", "red",
                        getPokemonDisplayName());
                displayMessageToOwner(mess);
                return;
            }
        }
        if (here == null) here = Vector3.getNewVector();
        here.set(getEntity()).addTo(0, getEntity().getEyeHeight(), 0);
        MovesUtils.useMove(move, getEntity(), target, here, targetLocation);
        here.set(getEntity());
    }

    @Override
    public int getExplosionState()
    {
        return moveInfo.boomState;
    }

    @Override
    public int getMoveIndex()
    {
        int ret = (int) dataManager.get(params.MOVEINDEXDW);
        return Math.max(0, ret);
    }

    @Override
    public String[] getMoves()
    {
        IPokemob transformed = CapabilityPokemob.getPokemobFor(getTransformedTo());
        if (transformed != null && transformed.getTransformedTo() == null)
        {
            IPokemob to = transformed;
            if (to != this) return to.getMoves();
        }
        return super.getMoves();
    }

    @Override
    public PokemobMoveStats getMoveStats()
    {
        return moveInfo;
    }

    @Override
    public byte getStatus()
    {
        return (byte) Math.max(0, (int) dataManager.get(params.STATUSDW));
    }

    @Override
    public short getStatusTimer()
    {
        return dataManager.get(params.STATUSTIMERDW).shortValue();
    }

    @Override
    public Entity getTransformedTo()
    {
        return getEntity().getEntityWorld().getEntityByID(getDataManager().get(params.TRANSFORMEDTODW));
    }

    @Override
    public void healStatus()
    {
        IOngoingAffected affected = CapabilityAffected.getAffected(getEntity());
        if (affected != null)
        {
            affected.removeEffects(PersistantStatusEffect.ID);
        }
        dataManager.set(params.STATUSDW, (byte) 0);
    }

    @Override
    public void setExplosionState(int i)
    {
        if (i >= 0) moveInfo.Exploding = true;
        moveInfo.boomState = i;
    }

    @Override
    public void setMoveIndex(int moveIndex)
    {
        if (PokecubeCore.isOnClientSide())
        {
            // Do nothing, packet should be handled by gui handler, not us.
        }
        else
        {
            if (moveIndex == getMoveIndex() || getPokemonAIState(NOMOVESWAP)) return;
            if (getMove(moveIndex) == null)
            {
                setMoveIndex(5);
            }
            moveInfo.ROLLOUTCOUNTER = 0;
            moveInfo.FURYCUTTERCOUNTER = 0;
            moveInfo.BLOCKCOUNTER = 0;
            moveInfo.blocked = false;
            moveInfo.blockTimer = 0;
            dataManager.set(params.MOVEINDEXDW, (byte) moveIndex);
        }
    }

    @Override
    public boolean setStatus(byte status, int turns)
    {
        non:
        if (getStatus() != STATUS_NON)
        {
            // Check if we actually have a status, if we do not, then we can
            // apply one.
            IOngoingAffected affected = CapabilityAffected.getAffected(getEntity());
            if (affected != null) if (affected.getEffects(PersistantStatusEffect.ID) == null) break non;
            return false;
        }
        else if (status == STATUS_NON)
        {
            IOngoingAffected affected = CapabilityAffected.getAffected(getEntity());
            affected.removeEffects(PersistantStatusEffect.ID);
            dataManager.set(params.STATUSDW, status);
            return true;
        }
        if (status == STATUS_BRN && isType(PokeType.getType("fire"))) return false;
        if (status == STATUS_PAR && isType(PokeType.getType("electric"))) return false;
        if (status == STATUS_FRZ && isType(PokeType.getType("ice"))) return false;
        if ((status == STATUS_PSN || status == STATUS_PSN2) && (isType(poison) || isType(steel))) return false;
        dataManager.set(params.STATUSDW, status);
        if ((status == STATUS_SLP || status == STATUS_FRZ) && turns == -1) turns = 5;
        short timer = (short) (turns == -1 ? PokecubeMod.core.getConfig().attackCooldown * 5
                : turns * PokecubeMod.core.getConfig().attackCooldown);
        setStatusTimer(timer);
        PersistantStatusEffect statusEffect = new PersistantStatusEffect(status, turns);
        return CapabilityAffected.addEffect(getEntity(), statusEffect);
    }

    @Override
    public boolean setStatus(byte status)
    {
        return setStatus(status, -1);
    }

    @Override
    public void setStatusTimer(short timer)
    {
        dataManager.set(params.STATUSTIMERDW, (int) timer);
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        if (to != null) getMoveStats().transformedTo = to.getEntityId();
        else getMoveStats().transformedTo = -1;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(to);
        PokedexEntry newEntry = getPokedexEntry();
        if (pokemob != null)
        {
            newEntry = pokemob.getPokedexEntry();
        }
        this.setType1(newEntry.getType1());
        this.setType2(newEntry.getType2());
        getDataManager().set(params.TRANSFORMEDTODW, getMoveStats().transformedTo);
    }

    @Override
    public int getAttackCooldown()
    {
        return this.getDataManager().get(params.ATTACKCOOLDOWN);
    }

    @Override
    public void setAttackCooldown(int timer)
    {
        this.getDataManager().set(params.ATTACKCOOLDOWN, timer);
    }

    @Override
    public String getLastMoveUsed()
    {
        return this.getMoveStats().lastMove;
    }

    @Override
    public boolean getOnGround()
    {
        return getEntity().onGround;
    }

    @Override
    public void setDisableTimer(int index, int timer)
    {
        this.getDataManager().set(params.DISABLE[index], timer);
    }

    @Override
    public int getDisableTimer(int index)
    {
        return this.getDataManager().get(params.DISABLE[index]);
    }

    @Override
    public void setActiveMove(EntityMoveUse move)
    {
        this.activeMove = move;
    }

    @Override
    public EntityMoveUse getActiveMove()
    {
        return this.activeMove;
    }

}
