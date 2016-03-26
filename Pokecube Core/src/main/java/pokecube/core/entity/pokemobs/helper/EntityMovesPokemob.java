/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.MoveEntry;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.items.ItemPokemobUseable;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityMovesPokemob extends EntitySexedPokemob
{
    private PokemobMoveStats moveInfo         = new PokemobMoveStats();

    private int              moveIndexCounter = 0;

    int                      attackTime;

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
    public void addOngoingEffect(Move_Base effect)
    {
        if (effect instanceof Move_Ongoing)
            moveInfo.ongoingEffects.put((Move_Ongoing) effect, ((Move_Ongoing) effect).getDuration());
    }

    @Override
    /** Reduces damage, depending on armor */
    protected float applyArmorCalculations(DamageSource source, float damage)
    {
        if (!source.isUnblockable())
        {
            int armour = source instanceof PokemobDamageSource ? super.getTotalArmorValue() : this.getTotalArmorValue();
            int i = 25 - armour;
            float f1 = damage * i;
            this.damageArmor(damage);
            damage = f1 / 25.0F;
        }

        return damage;
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        if (this.getAttackTarget() instanceof EntityLivingBase)
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

        if (getMove(getMoveIndex()) == MOVE_NONE) { return; }

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
            String message = I18n.translateToLocalFormatted("pokemob.status.flinch", getPokemonDisplayName());
            displayMessageToOwner("\u00a7c" + message);

            removeChanges(CHANGE_FLINCH);
            return;
        }

        if ((statusChange & CHANGE_CONFUSED) != 0)
        {
            if (Math.random() > 0.75)
            {
                removeChanges(CHANGE_CONFUSED);
                String message = I18n.translateToLocalFormatted("pokemob.status.confuse.remove",
                        getPokemonDisplayName());
                displayMessageToOwner("\u00a7a" + message);
            }
            else if (Math.random() > 0.5)
            {
                MovesUtils.doAttack("pokemob.status.confusion", this, this, f);
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
                String message = I18n.translateToLocalFormatted("pokemob.status.infatuate", getPokemonDisplayName());
                displayMessageToOwner("\u00a7c" + message);
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
        if (!move.move.notIntercepable) MovesUtils.doAttack(attack, this, targetLocation, f);
        else MovesUtils.doAttack(attack, this, target, f);
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
        return (int) dataWatcher.get(BOOMSTATEDW);
    }

    public int getLastAttackTick()
    {
        return attackTime;
    }

    public String[] getLearnableMoves()
    {
        List<String> moves = Database.getLearnableMoves(this.getPokedexNb());
        return moves.toArray(new String[0]);
    }

    @Override
    public String getMove(int index)
    {
        if (transformedTo instanceof IPokemob && transformedTo != this)
        {
            IPokemob to = (IPokemob) transformedTo;
            return to.getMove(index);
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
        int value = dataWatcher.get(STATUSMOVEINDEXDW);

        return (value >> 8) & 0xff;
    }

    @Override
    public String[] getMoves()
    {
        if (transformedTo instanceof IPokemob && transformedTo != this)
        {
            IPokemob to = (IPokemob) transformedTo;
            if (to.getTransformedTo() != this) return to.getMoves();
        }
        String movesString = dataWatcher.get(MOVESDW);
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
        int value = dataWatcher.get(STATUSMOVEINDEXDW);

        return (byte) (value & 0xff);
    }

    @Override
    public short getStatusTimer()
    {
        int value = dataWatcher.get(STATUSMOVEINDEXDW);

        return (short) ((value >> 16) & 0xffff);
    }

    @Override
    /** Returns the current armor value as determined by a call to
     * InventoryPlayer.getTotalArmorValue */
    public int getTotalArmorValue()
    {
        EnumDifficulty diff = worldObj.getDifficulty();
        int comp = diff.compareTo(EnumDifficulty.NORMAL);
        if (comp < 0) { return super.getTotalArmorValue(); }
        int i = (getActualStats()[2] + getActualStats()[4]) / 25;
        return i;
    }

    @Override
    public Entity getTransformedTo()
    {
        return transformedTo;
    }

    @Override
    public Entity getWeapon(int index)
    {
        return index == 0 ? moveInfo.weapon1 : moveInfo.weapon2;
    }

    public boolean hasMove(String move)
    {
        for (String s : getMoves())
        {
            if (s != null && s.equalsIgnoreCase(move)) return true;
        }
        return false;
    }

    @Override
    public void healStatus()
    {
        int value = dataWatcher.get(STATUSMOVEINDEXDW);
        value = value >> 8;
        value = (value << 8) | STATUS_NON;
        dataWatcher.set(STATUSMOVEINDEXDW, value);
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
            String message = I18n.translateToLocalFormatted("pokemob.info.levelup", getPokemonDisplayName(),
                    MovesUtils.getTranslatedMove(moveName));
            displayMessageToOwner(message);
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
                        String message = I18n.translateToLocalFormatted("pokemob.move.notify.learn",
                                getPokemonDisplayName(), MovesUtils.getTranslatedMove(s));
                        displayMessageToOwner(message);
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
    public void levelUp(int level)
    {
        List<String> moves = Database.getLevelUpMoves(this.getPokedexNb(), level, oldLevel);
        Collections.shuffle(moves);
        if (!worldObj.isRemote)
        {
            String message = I18n.translateToLocalFormatted("pokemob.move.notify.learn", getPokemonDisplayName(),
                    level);
            displayMessageToOwner(message);
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
                            if (s.equals(s1)) return;
                        }
                    }
                    for (String s : moves)
                    {
                        String message = I18n.translateToLocalFormatted("pokemob.move.notify.learn",
                                getPokemonDisplayName(), MovesUtils.getTranslatedMove(s));
                        displayMessageToOwner(message);
                        moveInfo.newMoves++;
                    }
                    setPokemonAIState(LEARNINGMOVE, true);
                    return;
                }
            }
            for (String s : moves)
            {
                ((EntityPokemob) this).learn(s);
            }
        }
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();

        if (getMoves()[0] == null)
        {
            learn(MOVE_TACKLE);
        }

        if (isServerWorld() && transformedTo != null && getAttackTarget() == null
                && !(getPokemonAIState(MATING) || isInLove() || getLover() != null))
        {
            setTransformedTo(null);
        }

        if (transformedTo == null && getLover() != null && hasMove(MOVE_TRANSFORM))
        {
            setTransformedTo(getLover());
            Move_Base trans = MovesUtils.getMoveFromName(MOVE_TRANSFORM);
            trans.notifyClient(this, here, getLover());
        }

        this.updateStatusEffect();
        this.updateOngoingMoves();
        if (getAbility() != null)
        {
            getAbility().onUpdate(this);
        }
        if (!this.isDead && getHeldItemMainhand() != null
                && getHeldItemMainhand().getItem() instanceof ItemPokemobUseable)
        {
            ((IPokemobUseable) getHeldItemMainhand().getItem()).itemUse(getHeldItemMainhand(), this, null);
        }
        moves:
        if (this.getLevel() > 0)
        {
            for (String s : getMoves())
            {
                if (MovesUtils.isMoveImplemented(s))
                {
                    break moves;
                }
            }
            oldLevel = 1;
            levelUp(getLevel());
        }
    }

    @Override
    public void onMoveUse(MovePacket move)
    {
        Move_Base attack = move.getMove();

        IPokemob attacker = move.attacker;
        Entity attacked = move.attacked;

        if (moveInfo.substituteHP > 0 && attacked == this)
        {
            float damage = MovesUtils.getAttackStrength(attacker, (IPokemob) attacked, move.getMove().move.category,
                    move.PWR, move);
            attacker.displayMessageToOwner("\u00a7c" + "Move Absorbed by substitute");
            displayMessageToOwner("\u00a7a" + "move absorbed by substitute");
            moveInfo.substituteHP -= damage;
            if (moveInfo.substituteHP < 0)
            {
                attacker.displayMessageToOwner("\u00a7a" + "substitute broke");
                displayMessageToOwner("\u00a7c" + "substitute broke");
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
    public void onUpdate()
    {
        super.onUpdate();

        moveInfo.lastActiveTime = moveInfo.timeSinceIgnited;

        if (true)
        {
            int i = getExplosionState();

            if (i > 0 && moveInfo.timeSinceIgnited == 0 && worldObj.isRemote)
            {
                playSound(SoundEvents.entity_creeper_primed, 1.0F, 0.5F);
            }
            moveInfo.timeSinceIgnited += i;

            if (moveInfo.timeSinceIgnited < 0)
            {
                moveInfo.timeSinceIgnited = 0;
            }
        }
        if (getAttackTarget() == null && moveInfo.timeSinceIgnited > 50) //
        {
            setExplosionState(-1);
            moveInfo.timeSinceIgnited--;

            if (moveInfo.timeSinceIgnited < 0)
            {
                moveInfo.timeSinceIgnited = 0;
            }
        }
    }

    @Override
    public void popFromPokecube()
    {
        super.popFromPokecube();
        healStatus();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setStatus(nbttagcompound.getByte(PokecubeSerializer.STATUS));
        this.setPokemonAIState(LEARNINGMOVE, nbttagcompound.getBoolean("newMoves"));
        moveInfo.newMoves = nbttagcompound.getInteger("numberMoves");
        String movesString = nbttagcompound.getString(PokecubeSerializer.MOVES);
        dataWatcher.set(MOVESDW, movesString);
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
        dataWatcher.set(BOOMSTATEDW, Byte.valueOf((byte) i));
    }

    public void setHasAttacked(String move)
    {
        attackTime = MovesUtils.getDelayBetweenAttacks(this, move);
    }

    public void setLastAttackTick(int tick)
    {
        attackTime = tick;
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
        if (getMove(moveIndex) == null)
        {
            setMoveIndex(5);
        }

        if (moveIndex == getMoveIndex()) return;

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
            int value = dataWatcher.get(STATUSMOVEINDEXDW);
            int toSet = moveIndex << 8;
            value = (value & 0xffff00ff) | toSet;
            dataWatcher.set(STATUSMOVEINDEXDW, value);
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
        dataWatcher.set(MOVESDW, movesString);
    }

    @Override
    public boolean setStatus(byte status)
    {
        if (getStatus() != STATUS_NON) { return false; }

        if (status == STATUS_BRN && (getType1() == PokeType.fire || getType2() == PokeType.fire)) return false;
        if (status == STATUS_PAR && (getType1() == PokeType.electric || getType2() == PokeType.electric)) return false;
        if (status == STATUS_FRZ && (getType1() == PokeType.ice || getType2() == PokeType.ice)) return false;
        if ((status == STATUS_PSN || status == STATUS_PSN2) && (getType1() == PokeType.poison
                || getType2() == PokeType.poison || getType1() == PokeType.steel || getType2() == PokeType.steel))
            return false;

        int value = dataWatcher.get(STATUSMOVEINDEXDW);
        value = value >> 8;
        value = (value << 8) | status;
        dataWatcher.set(STATUSMOVEINDEXDW, value);
        setStatusTimer((short) 100);
        return true;
    }

    @Override
    public void setStatusTimer(short timer)
    {
        int value = dataWatcher.get(STATUSMOVEINDEXDW);

        timer = (short) Math.max(0, timer);

        int toSet = (timer) << 16;
        value = (value & 0x0000ffff) | toSet;
        dataWatcher.set(STATUSMOVEINDEXDW, value);
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        if (isServerWorld())
        {
            MovesUtils.getMoveFromName(MOVE_TRANSFORM).notifyClient(this, here, to);
        }
        transformedTo = to;
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

    protected void spawnPoisonParticle()
    {
        for (int i = 0; i < 2; i++)
        {
            // TODO Poison Effects
        }
    }

    protected void updateOngoingMoves()
    {
        if (this.ticksExisted % 40 == 0)
        {
            Set<Move_Ongoing> effects = new HashSet<Move_Ongoing>();
            for (Move_Ongoing m : moveInfo.ongoingEffects.keySet())
            {
                effects.add(m);
            }
            for (Move_Ongoing m : effects)
            {
                m.doOngoingEffect(this);
                int duration = moveInfo.ongoingEffects.get(m);
                if (duration == 0) moveInfo.ongoingEffects.remove(m);
                else if (duration > 0) moveInfo.ongoingEffects.put(m, duration - 1);
            }
        }
        if (moveInfo.DEFENSECURLCOUNTER > 0) moveInfo.DEFENSECURLCOUNTER--;
        if (moveInfo.SELFRAISECOUNTER > 0) moveInfo.SELFRAISECOUNTER--;
        if (moveInfo.TARGETLOWERCOUNTER > 0) moveInfo.TARGETLOWERCOUNTER--;
        if (moveInfo.SPECIALCOUNTER > 0) moveInfo.SPECIALCOUNTER--;
    }

    protected void updateStatusEffect()
    {
        int duration = 10;

        short timer = getStatusTimer();

        if (timer > 0) setStatusTimer((short) (timer - 1));
        byte status = getStatus();

        ItemStack held = getHeldItemMainhand();
        if (held != null && held.getItem() instanceof ItemBerry)
        {
            if (BerryManager.berryEffect(this, held))
            {
                HappinessType.applyHappiness(this, HappinessType.BERRY);
                setHeldItem(null);
            }
        }

        if (this.ticksExisted % 20 == 0)
        {
            int statusChange = getChanges();

            if ((statusChange & CHANGE_CURSE) != 0)
            {
                String message = I18n.translateToLocalFormatted("pokemob.status.curse", getPokemonDisplayName());
                displayMessageToOwner("\u00a7c" + message);
                setHealth(getHealth() - getMaxHealth() * 0.25f);
            }

        }

        if (status == STATUS_NON)
        {
            if (getPokemonAIState(SLEEPING))
            {
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
            }
            return;
        }
        if (this.ticksExisted % 20 == 0)
        {

            if (status == IMoveConstants.STATUS_BRN)
            {
                this.setFire(1);
            }
            else if (status == IMoveConstants.STATUS_FRZ)
            {
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                if (Math.random() > 0.9)
                {
                    healStatus();
                }
            }
            else if (status == IMoveConstants.STATUS_PSN)
            {
                this.attackEntityFrom(DamageSource.magic, getMaxHealth() / 8f);
                spawnPoisonParticle();

            }
            else if (status == IMoveConstants.STATUS_PSN2)
            {
                this.attackEntityFrom(DamageSource.magic, (moveInfo.TOXIC_COUNTER + 1) * getMaxHealth() / 16f);
                spawnPoisonParticle();
                spawnPoisonParticle();
                moveInfo.TOXIC_COUNTER++;
            }
            else if (status == IMoveConstants.STATUS_SLP)
            {
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                if (Math.random() > 0.9 || timer <= 0)
                {
                    healStatus();
                }
            }
            else
            {
                moveInfo.TOXIC_COUNTER = 0;
            }
        }

    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte(PokecubeSerializer.STATUS, getStatus());
        nbttagcompound.setBoolean("newMoves", getPokemonAIState(LEARNINGMOVE));
        nbttagcompound.setInteger("numberMoves", moveInfo.newMoves);
        String movesString = dataWatcher.get(MOVESDW);
        nbttagcompound.setString(PokecubeSerializer.MOVES, movesString);
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
