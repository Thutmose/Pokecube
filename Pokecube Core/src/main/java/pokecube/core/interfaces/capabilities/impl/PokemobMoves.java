package pokecube.core.interfaces.capabilities.impl;

import java.util.ArrayList;
import java.util.Collections;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public abstract class PokemobMoves extends PokemobSexed
{

    @Override
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        String currentMove = getMove(getMoveIndex());
        if (currentMove == MOVE_NONE || currentMove == null) { return; }
        getDataManager().set(params.LASTMOVE, currentMove);
        if (target instanceof EntityLiving)
        {
            EntityLiving t = (EntityLiving) target;
            if (t.getAttackTarget() == null)
            {
                t.setAttackTarget(getEntity());
            }
        }
        if (target instanceof EntityLivingBase)
        {
            ((EntityLivingBase) target).setLastAttacker(getEntity());
            getEntity().setLastAttacker(target);
        }
        int statusChange = getChanges();
        if ((statusChange & CHANGE_FLINCH) != 0)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "red",
                    getPokemonDisplayName());
            displayMessageToOwner(mess);
            removeChanges(CHANGE_FLINCH);
            return;
        }

        if ((statusChange & CHANGE_CONFUSED) != 0)
        {
            if (Math.random() > 0.75)
            {
                removeChanges(CHANGE_CONFUSED);
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "green",
                        getPokemonDisplayName());
                displayMessageToOwner(mess);
            }
            else if (Math.random() > 0.5)
            {
                MovesUtils.doAttack("pokemob.status.confusion", this, getEntity());
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
        Move_Base move = MovesUtils.getMoveFromName(attack);
        if (move == null || move.move == null)
        {
            System.err.println("SOMEONE USING NULL MOVE " + attack);
            Thread.dumpStack();
            return;
        }
        if (here == null) here = Vector3.getNewVector();
        here.set(getEntity()).addTo(0, getEntity().getEyeHeight(), 0);
        MovesUtils.useMove(move, getEntity(), target, here, targetLocation);
        this.setAttackCooldown(MovesUtils.getAttackDelay(this, currentMove,
                (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0, false));
        here.set(getEntity());
    }

    @Override
    public int getExplosionState()
    {
        return (int) dataManager.get(params.BOOMSTATEDW);
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
        if (transformed != null && getTransformedTo() != getEntity())
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
        dataManager.set(params.STATUSDW, (byte) 0);
    }

    @Override
    public void setExplosionState(int i)
    {
        if (i >= 0) moveInfo.Exploding = true;
        dataManager.set(params.BOOMSTATEDW, Byte.valueOf((byte) i));
    }

    @Override
    public void setMoveIndex(int moveIndex)
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

        if (PokecubeCore.isOnClientSide())
        {
            try
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                buffer.writeByte(MessageServer.MOVEINDEX);
                buffer.writeInt(getEntity().getEntityId());
                buffer.writeByte((byte) moveIndex);
                MessageServer packet = new MessageServer(buffer);
                PokecubePacketHandler.sendToServer(packet);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            dataManager.set(params.MOVEINDEXDW, (byte) moveIndex);
        }
    }

    @Override
    public boolean setStatus(byte status)
    {
        if (getStatus() != STATUS_NON) { return false; }
        if (status == STATUS_BRN && isType(PokeType.getType("fire"))) return false;
        if (status == STATUS_PAR && isType(PokeType.getType("electric"))) return false;
        if (status == STATUS_FRZ && isType(PokeType.getType("ice"))) return false;
        if ((status == STATUS_PSN || status == STATUS_PSN2) && (isType(poison) || isType(steel))) return false;
        dataManager.set(params.STATUSDW, status);
        setStatusTimer((short) (PokecubeMod.core.getConfig().attackCooldown * 5));
        return true;
    }

    @Override
    public void setStatusTimer(short timer)
    {
        dataManager.set(params.STATUSTIMERDW, (int) timer);
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        if (to != null) getDataManager().set(params.TRANSFORMEDTODW, to.getEntityId());
        else getDataManager().set(params.TRANSFORMEDTODW, -1);
        if (to instanceof IPokemob)
        {
            PokedexEntry newEntry = ((IPokemob) to).getPokedexEntry();
            this.setType1(newEntry.getType1());
            this.setType2(newEntry.getType2());
        }
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
        return this.getDataManager().get(params.LASTMOVE);
    }

    @Override
    public boolean getOnGround()
    {
        return getEntity().onGround;
    }
}