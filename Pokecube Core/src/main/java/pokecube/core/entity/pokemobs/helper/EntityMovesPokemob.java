/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityMovesPokemob extends EntitySexedPokemob
{
    private PokemobMoveStats moveInfo         = new PokemobMoveStats();

    private int              moveIndexCounter = 0;

    /** @param par1World */
    public EntityMovesPokemob(World world)
    {
        super(world);
    }

    @Override
    public boolean addChange(int change)
    {
        int old = moveInfo.changes;
        moveInfo.changes |= change;
        return moveInfo.changes != old;
    }

    @Override
    public boolean addOngoingEffect(Move_Base effect)
    {
        if (effect instanceof Move_Ongoing)
        {
            if (!moveInfo.ongoingEffects.containsKey(effect))
            {
                moveInfo.ongoingEffects.put((Move_Ongoing) effect, ((Move_Ongoing) effect).getDuration());
                return true;
            }
        }
        return false;
    }

    @Override
    /** Reduces damage, depending on armor */
    protected float applyArmorCalculations(DamageSource source, float damage)
    {
        if (!(source instanceof PokemobDamageSource))
        {
            int armour = 0;
            if (source.isMagicDamage())
            {
                armour = (int) ((getActualStats()[4]) / 12.5);
            }
            else
            {
                armour = this.getTotalArmorValue();
            }
            damage = CombatRules.getDamageAfterAbsorb(damage, armour,
                    (float) this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        }
        return damage;
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        if (this.getAttackTarget() != null)
        {
            float distanceToEntity = this.getAttackTarget().getDistanceToEntity(this);
            attackEntityAsPokemob(par1Entity, distanceToEntity);
        }
        return super.attackEntityAsMob(par1Entity);
    }

    protected void attackEntityAsPokemob(Entity entity, float f)
    {
        if (getLover() == entity) return;
        Vector3 v = Vector3.getNewVector().set(entity);
        executeMove(entity, v, f);
    }

    @Override
    public void exchangeMoves(int moveIndex0, int moveIndex1)
    {
        if (PokecubeCore.isOnClientSide() && getPokemonAIState(IMoveConstants.TAMED))
        {
            String[] moves = getMoves();
            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
                moveInfo.num++;
            }
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {

            }

            try
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(11));
                buffer.writeByte(MessageServer.MOVESWAP);
                buffer.writeInt(getEntityId());
                buffer.writeByte((byte) moveIndex0);
                buffer.writeByte((byte) moveIndex1);
                buffer.writeInt(moveInfo.num);
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
            String[] moves = getMoves();

            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
                moveInfo.num++;
            }
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
                if (getMove(4) == null) return;

                moveInfo.newMoves--;
                moves[3] = getMove(4);
                setMoves(moves);
                if (moveInfo.newMoves <= 0) this.setPokemonAIState(LEARNINGMOVE, false);
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

    @Override
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        String currentMove = getMove(getMoveIndex());
        if (currentMove == MOVE_NONE || currentMove == null) { return; }

        if (target instanceof EntityLiving)
        {
            EntityLiving t = (EntityLiving) target;
            if (t.getAttackTarget() == null)
            {
                t.setAttackTarget(this);
            }
        }
        int statusChange = getChanges();

        if ((statusChange & CHANGE_FLINCH) != 0)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "red",
                    getPokemonDisplayName().getFormattedText());
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
                        getPokemonDisplayName().getFormattedText());
                displayMessageToOwner(mess);
            }
            else if (Math.random() > 0.5)
            {
                MovesUtils.doAttack("pokemob.status.confusion", this, this);
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
                        getPokemonDisplayName().getFormattedText());
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
        here.set(posX, posY + getEyeHeight(), posZ);
        MovesUtils.useMove(move, this, target, here, targetLocation);
        here.set(this);
    }

    @Override
    public int getChanges()
    {
        return moveInfo.changes;
    }

    @SideOnly(Side.CLIENT)
    /** Params: (Float)Render tick. Returns the intensity of the creeper's flash
     * when it is ignited. */
    public float getCreeperFlashIntensity(float par1)
    {
        return (this.moveInfo.lastActiveTime + (this.moveInfo.timeSinceIgnited - this.moveInfo.lastActiveTime) * par1)
                / (this.moveInfo.fuseTime - 2);
    }

    @Override
    public int getExplosionState()
    {
        return dataManager.get(BOOMSTATEDW);
    }

    public String[] getLearnableMoves()
    {
        List<String> moves = Database.getLearnableMoves(this.getPokedexNb());
        return moves.toArray(new String[0]);
    }

    @Override
    public String getMove(int index)
    {
        if (getTransformedTo() instanceof IPokemob && getTransformedTo() != this)
        {
            IPokemob to = (IPokemob) getTransformedTo();
            if (to.getTransformedTo() != this) return to.getMove(index);
        }

        String[] moves = getMoves();

        if (index >= 0 && index < 4) { return moves[index]; }
        if (index == 4 && moves[3] != null && getPokemonAIState(LEARNINGMOVE))
        {
            List<String> list;
            List<String> lastMoves = new ArrayList<String>();
            int n = getLevel();

            while (n > 0)
            {
                list = getPokedexEntry().getMovesForLevel(this.getLevel(), --n);
                if (!list.isEmpty())
                {
                    list:
                    for (String s : list)
                    {
                        for (String s1 : moves)
                        {
                            if (s.equals(s1)) continue list;
                        }
                        lastMoves.add(s);
                    }
                    break;
                }
            }

            if (!lastMoves.isEmpty()) { return lastMoves.get(moveInfo.num % lastMoves.size()); }
        }

        if (index == 5) { return IMoveConstants.MOVE_NONE; }

        return null;
    }

    @Override
    public int getMoveIndex()
    {
        int ret = dataManager.get(MOVEINDEXDW);
        return Math.max(0, ret);
    }

    @Override
    public String[] getMoves()
    {
        if (getTransformedTo() instanceof IPokemob && getTransformedTo() != this)
        {
            IPokemob to = (IPokemob) getTransformedTo();
            if (to.getTransformedTo() != this) return to.getMoves();
        }
        String movesString = dataManager.get(MOVESDW);
        String[] moves = new String[4];

        if (movesString != null && movesString.length() > 2)
        {
            String[] movesSplit = movesString.split(",");
            for (int i = 0; i < Math.min(4, movesSplit.length); i++)
            {
                String move = movesSplit[i];

                if (move != null && move.length() > 1 && MovesUtils.isMoveImplemented(move))
                {
                    moves[i] = move;
                }
            }
        }
        return moves;
    }

    @Override
    public PokemobMoveStats getMoveStats()
    {
        return moveInfo;
    }

    @Override
    public HashMap<Move_Ongoing, Integer> getOngoingEffects()
    {
        return moveInfo.ongoingEffects;
    }

    @Override
    public byte getStatus()
    {
        return (byte) Math.max(0, dataManager.get(STATUSDW));
    }

    @Override
    public short getStatusTimer()
    {
        return dataManager.get(STATUSTIMERDW).shortValue();
    }

    @Override
    /** Returns the current armor value as determined by a call to
     * InventoryPlayer.getTotalArmorValue */
    public int getTotalArmorValue()
    {
        return (int) ((getActualStats()[2]) / 12.5);
    }

    @Override
    public Entity getTransformedTo()
    {
        return worldObj.getEntityByID(getDataManager().get(TRANSFORMEDTODW));
    }

    @Override
    public Entity getWeapon(int index)
    {
        return index == 0 ? moveInfo.weapon1 : moveInfo.weapon2;
    }

    @Override
    public void healStatus()
    {
        dataManager.set(STATUSDW, (byte) 0);
    }

    @Override
    public void learn(String moveName)
    {
        if (!MovesUtils.isMoveImplemented(moveName)) { return; }

        String[] moves = getMoves();

        if (moveName == null) return;

        // check it's not already known or forgotten
        for (String move : moves)
        {
            if (moveName.equals(move)) return;
        }

        if (getPokemonOwner() != null && !this.isDead)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "",
                    getPokemonDisplayName().getFormattedText(), MovesUtils.getUnlocalizedMove(moveName));
            displayMessageToOwner(mess);
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
                String[] current = getMoves();
                if (current[3] != null)
                {
                    for (String s : current)
                    {
                        for (String s1 : moves)
                        {
                            if (s.equals(s1)) return;
                        }
                    }
                    for (String s : moves)
                    {
                        ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "",
                                getPokemonDisplayName().getFormattedText(), s);
                        displayMessageToOwner(mess);
                        moveInfo.newMoves++;
                    }
                    setPokemonAIState(LEARNINGMOVE, true);
                    return;
                }
            }
            else
            {
                int index = rand.nextInt(4);
                setMove(index, moveName);
            }
        }
    }

    @Override
    public IPokemob levelUp(int level)
    {
        List<String> moves = Database.getLevelUpMoves(this.getPokedexNb(), level, oldLevel);
        Collections.shuffle(moves);
        if (!worldObj.isRemote)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.info.levelup", "",
                    getPokemonDisplayName().getFormattedText(), level + "");
            displayMessageToOwner(mess);
        }
        HappinessType.applyHappiness(this, HappinessType.LEVEL);
        if (moves != null)
        {
            if (this.getPokemonAIState(IMoveConstants.TAMED))
            {
                String[] current = getMoves();
                if (current[3] != null)
                {
                    for (String s : current)
                    {
                        for (String s1 : moves)
                        {
                            if (s.equals(s1)) return this;
                        }
                    }
                    for (String s : moves)
                    {
                        ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "",
                                getPokemonDisplayName().getFormattedText(), MovesUtils.getUnlocalizedMove(s));
                        displayMessageToOwner(mess);
                        moveInfo.newMoves++;
                    }
                    setPokemonAIState(LEARNINGMOVE, true);
                    return this;
                }
            }
            for (String s : moves)
            {
                ((EntityPokemob) this).learn(s);
            }
        }
        return this;
    }

    @Override
    public void onMoveUse(MovePacket move)
    {
        Move_Base attack = move.getMove();

        IPokemob attacker = move.attacker;
        Entity attacked = move.attacked;

        if (attacked == this)
        {
            this.getEntityData().setString("lastMoveHitBy", move.attack);
        }

        if (moveInfo.substituteHP > 0 && attacked == this)
        {
            float damage = MovesUtils.getAttackStrength(attacker, (IPokemob) attacked, move.getMove().move.category,
                    move.PWR, move);

            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.substitute.absorb", "green");
            displayMessageToOwner(mess);
            mess = CommandTools.makeTranslatedMessage("pokemob.substitute.absorb", "red");
            attacker.displayMessageToOwner(mess);
            moveInfo.substituteHP -= damage;
            if (moveInfo.substituteHP < 0)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.substitute.break", "red");
                displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.substitute.break", "green");
                attacker.displayMessageToOwner(mess);
            }
            move.failed = true;
            move.PWR = 0;
            move.changeAddition = 0;
            move.statusChange = 0;
        }

        if (attacker == this && attack.getName().equals(MOVE_SUBSTITUTE))
        {
            moveInfo.substituteHP = getMaxHealth() / 4;
        }

        if (getHeldItemMainhand() != null)
        {
            HeldItemHandler.processHeldItemUse(move, this, getHeldItemMainhand());
        }

        if (getAbility() != null)
        {
            getAbility().onMoveUse(this, move);
        }

        if (attack.getName().equals(MOVE_FALSESWIPE))
        {
            move.noFaint = true;
        }

        if (attack.getName().equals(MOVE_PROTECT) || attack.getName().equals(MOVE_DETECT) && !moveInfo.blocked)
        {
            moveInfo.blockTimer = 30;
            moveInfo.blocked = true;
            moveInfo.BLOCKCOUNTER++;
        }
        boolean blockMove = false;

        for (String s : MoveEntry.protectionMoves)
            if (s.equals(move.attack))
            {
                blockMove = true;
                break;
            }

        if (move.attacker == this && !blockMove && moveInfo.blocked)
        {
            moveInfo.blocked = false;
            moveInfo.blockTimer = 0;
            moveInfo.BLOCKCOUNTER = 0;
        }

        boolean unblockable = false;
        for (String s : MoveEntry.unBlockableMoves)
            if (s.equals(move.attack))
            {
                unblockable = true;
                System.out.println("Unblockable");
                break;
            }

        if (moveInfo.blocked && move.attacked != move.attacker && !unblockable)
        {
            float count = Math.min(0, moveInfo.BLOCKCOUNTER - 1);
            float chance = count != 0 ? Math.max(0.125f, ((1 / (count * 2)))) : 1;
            if (chance > Math.random())
            {
                move.canceled = true;
            }
            else
            {
                move.failed = true;
            }
        }
        if (moveInfo.BLOCKCOUNTER > 0) moveInfo.BLOCKCOUNTER--;
    }

    @Override
    public void popFromPokecube()
    {
        super.popFromPokecube();
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void readSpawnData(ByteBuf data)
    {
        int abilityNumber = data.readInt();
        setAbility(AbilityManager.getAbility(abilityNumber, this));
        super.readSpawnData(data);
    };

    @Override
    public void removeChanges(int changes)
    {
        this.moveInfo.changes -= changes;
    }

    @Override
    public void setExplosionState(int i)
    {
        if (i >= 0) moveInfo.Exploding = true;
        dataManager.set(BOOMSTATEDW, Byte.valueOf((byte) i));
    }

    @Override
    public void setLeaningMoveIndex(int num)
    {
        this.moveInfo.num = num;
    }

    @Override
    public void setMove(int i, String moveName)
    {
        String[] moves = getMoves();
        moves[i] = moveName;
        setMoves(moves);
    }

    @Override
    public void setMoveIndex(int moveIndex)
    {
        if (moveIndex == getMoveIndex()) return;
        if (getMove(moveIndex) == null)
        {
            setMoveIndex(5);
        }
        moveInfo.ROLLOUTCOUNTER = 0;
        moveInfo.FURYCUTTERCOUNTER = 0;

        if (PokecubeCore.isOnClientSide())
        {
            try
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                buffer.writeByte(MessageServer.MOVEINDEX);
                buffer.writeInt(getEntityId());
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
            dataManager.set(MOVEINDEXDW, (byte) moveIndex);
        }
    }

    public void setMoves(String[] moves)
    {
        String movesString = "";

        if (moves != null && moves.length == 4)
        {
            for (int i = 0; i < moves.length; i++)
            {
                String move = moves[i];

                if (move != null)
                {
                    movesString += move + ",";
                }
            }
        }
        dataManager.set(MOVESDW, movesString);
    }

    @Override
    public boolean setStatus(byte status)
    {
        if (getStatus() != STATUS_NON) { return false; }

        if (status == STATUS_BRN && isType(PokeType.fire)) return false;
        if (status == STATUS_PAR && isType(PokeType.electric)) return false;
        if (status == STATUS_FRZ && isType(PokeType.ice)) return false;
        if ((status == STATUS_PSN || status == STATUS_PSN2) && (isType(poison) || isType(steel))) return false;
        dataManager.set(STATUSDW, status);
        setStatusTimer((short) 100);
        return true;
    }

    @Override
    public void setStatusTimer(short timer)
    {
        dataManager.set(STATUSTIMERDW, (int) timer);
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        if (to != null) getDataManager().set(TRANSFORMEDTODW, to.getEntityId());
        else getDataManager().set(TRANSFORMEDTODW, -1);
        if (to instanceof IPokemob)
        {
            PokedexEntry newEntry = ((IPokemob) to).getPokedexEntry();
            this.setType1(newEntry.getType1());
            this.setType2(newEntry.getType2());
        }
    }

    @Override
    public void setWeapon(int index, Entity weapon)
    {
        if (index == 0) moveInfo.weapon1 = weapon;
        else moveInfo.weapon2 = weapon;
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        int abilityNumber = getAbility() == null ? -1 : AbilityManager.getIdForAbility(getAbility());
        data.writeInt(abilityNumber);
        super.writeSpawnData(data);
    }
}
