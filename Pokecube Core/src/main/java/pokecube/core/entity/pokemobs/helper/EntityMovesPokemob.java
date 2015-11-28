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
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.mod_Pokecube;
import pokecube.core.database.Database;
import pokecube.core.database.MoveEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
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
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.network.pokemobs.PokemobPacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageClient;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

/**
 * @author Manchou
 *
 */
public abstract class EntityMovesPokemob extends EntitySexedPokemob
{
	private PokemobMoveStats moveInfo = new PokemobMoveStats();
    /**
     * @param par1World
     */
    public EntityMovesPokemob(World world)
    {
        super(world);
    }
    
    @Override
    public void popFromPokecube() {
    	super.popFromPokecube();
    	healStatus();
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte(PokecubeSerializer.STATUS, getStatus());
        nbttagcompound.setBoolean("newMoves", getPokemonAIState(LEARNINGMOVE));
        nbttagcompound.setInteger("numberMoves", moveInfo.newMoves);
        String movesString = dataWatcher.getWatchableObjectString(MOVESDW);
        nbttagcompound.setString(PokecubeSerializer.MOVES, movesString);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setStatus(nbttagcompound.getByte(PokecubeSerializer.STATUS));
        this.setPokemonAIState(LEARNINGMOVE, nbttagcompound.getBoolean("newMoves"));
        moveInfo.newMoves = nbttagcompound.getInteger("numberMoves");
        String movesString = nbttagcompound.getString(PokecubeSerializer.MOVES);
        dataWatcher.updateObject(MOVESDW, movesString);
    }


    @Override
    public String[] getMoves()
    {
    	if(transformedTo instanceof IPokemob && transformedTo != this)
    	{
    		IPokemob to = (IPokemob) transformedTo;
    		if(to.getTransformedTo()!=this)
    			return to.getMoves();
    	}
        String movesString = dataWatcher.getWatchableObjectString(MOVESDW);
        String[] moves = new String[4];

        if (movesString != null && movesString.length() > 2)
        {
            String[] movesSplit = movesString.split(",");
            for (int i = 0; i < Math.min(4, movesSplit.length) ; i++)
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
    public String getMove(int index)
    {
    	if(transformedTo instanceof IPokemob && transformedTo != this)
    	{
    		IPokemob to = (IPokemob) transformedTo;
    		return to.getMove(index);
    	}
    	
        String[] moves = getMoves();

        if (index >= 0 && index < 4)
        {
            return moves[index];
        }
        if(index==4 && moves[3]!=null && getPokemonAIState(LEARNINGMOVE))
        {
        	List<String> list;
        	List<String> lastMoves = new ArrayList<String>();
        	int n = getLevel();
        	
        	while(n > 0)
        	{
        		list = getPokedexEntry().getMovesForLevel(this.getLevel(),--n);
        		if(!list.isEmpty())
        		{
        			list:
        			for(String s:list)
        			{
        				for(String s1:moves)
        				{
        					if(s.equals(s1))
        						continue list;
        				}
        				lastMoves.add(s);
        			}
        			break;
        		}
        	}
        	
        	if(!lastMoves.isEmpty())
        	{
        		return (String) lastMoves.get(moveInfo.num%lastMoves.size());
        	}
        }

        if(index==5)
        {
        	return IMoveNames.MOVE_NONE;
        }
        
        
        return null;
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
        dataWatcher.updateObject(MOVESDW, movesString);
    }
    
    @Override
    public void learn(String moveName)
    {
    	if(!MovesUtils.isMoveImplemented(moveName))
    	{
    		return;
    	}
    	
        String[] moves = getMoves();
        
        if (moveName == null)
        	return;
        
        // check it's not already known or forgotten
        for (String move : moves) {
			if (moveName.equals(move))
				return;
		}
        
        if(getPokemonOwner()!=null && !this.isDead)
        {
        	String message = StatCollector.translateToLocalFormatted(
            		"pokemob.move.notify.learn", 
            		getPokemonDisplayName(),
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
        	if (getPokemonAIState(IPokemob.TAMED))
        	{
	    		String[] current = getMoves();
	    		if(current[3]!=null)
	    		{
	    			for(String s: current)
	    			{
	    				for(String s1:moves)
	    				{
	    					if(s.equals(s1))
	    						return;
	    				}
	    			}
	    	    	for(String s:moves)
	    	    	{
	    	        	String message = StatCollector.translateToLocalFormatted(
	    	            		"pokemob.move.notify.learn", 
	    	            		getPokemonDisplayName(),
	    	            		MovesUtils.getTranslatedMove(s));
	    	        	displayMessageToOwner(message);
	    	        	moveInfo.newMoves++;
	    	    	}
	    	    	setPokemonAIState(LEARNINGMOVE, true);
	    			return;
	    		}
        	}
        	else {
        		int index = rand.nextInt(4);
                setMove(index, moveName);
        	}
        }
    }

    @Override
    public void exchangeMoves(int moveIndex0, int moveIndex1)
    {
        if (mod_Pokecube.isOnClientSide() && getPokemonAIState(IPokemob.TAMED))
        {
            String[] moves = getMoves();
            if(moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
            	moveInfo.num++;
            }
            else if(moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
            	
            }
        	
            try
            {
                String toSend = getEntityId() + "`m`";
                toSend += moveIndex0 + "`" + moveIndex1+"`"+moveInfo.num;
                byte[] message = toSend.getBytes();
                PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubePacketHandler.CHANNEL_ID_EntityPokemob, message);
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
            
            if(moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {
            	moveInfo.num++;
            }
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
            	if(getMove(4)==null)
            		return;
            	
            	moveInfo.newMoves--;
            	moves[3] = getMove(4);
            	setMoves(moves);
            	if(moveInfo.newMoves<=0)
                	this.setPokemonAIState(LEARNINGMOVE, false);
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
    public void setMove(int i, String moveName)
    {
        String[] moves = getMoves();
        moves[i] = moveName;
        setMoves(moves);
    }
    
    public boolean hasMove(String move)
    {
		for(String s: getMoves())
		{
			if(s!=null&&s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM))
				return true;
		}
    	return false;
    }

    @Override
    public void healStatus(){
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	value = value >> 8;
        value = (value << 8 )| STATUS_NON;
        dataWatcher.updateObject(STATUSMOVEINDEXDW, value);
    }
    
    @Override
    public boolean setStatus(byte status)
    {
    	if (getStatus() != STATUS_NON) {
    		return false;
    	}

    	if (status == STATUS_BRN && 
    			(getType1()==PokeType.fire || getType2()==PokeType.fire))
    		return false;
    	if (status == STATUS_PAR && 
    			(getType1()==PokeType.electric || getType2()==PokeType.electric))
    		return false;
    	if (status == STATUS_FRZ && 
    			(getType1()==PokeType.ice || getType2()==PokeType.ice))
    		return false;
    	if ((status == STATUS_PSN || status == STATUS_PSN2) && 
    			 (getType1()==PokeType.poison || getType2()==PokeType.poison 
    			|| getType1()==PokeType.steel || getType2()==PokeType.steel))
    		return false;
    	
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	value = value >> 8;
        value = (value << 8 )| status;
        dataWatcher.updateObject(STATUSMOVEINDEXDW, value);
        setStatusTimer((short) 100);
        return true;
    }


    @Override
    public void onEntityUpdate() {
    	super.onEntityUpdate();
    	
    	if(getMoves()[0]==null)
    	{
    		learn(MOVE_TACKLE);
    	}
    	
    	if(transformedTo!=null && getAttackTarget()==null && !getPokemonAIState(MATING))
    	{
    		transformedTo = null;
    	}
    	
    	if(transformedTo==null && getLover()!=null && hasMove(MOVE_TRANSFORM))
    	{
    		transformedTo = getLover();
    	//	System.out.println(lover);
    		Move_Base trans = MovesUtils.getMoveFromName(MOVE_TRANSFORM);
    		trans.notifyClient(this, here, getLover());;
    	}
    	
    	this.updateStatusEffect();
    	this.updateOngoingMoves();
    	if(moveInfo.ability!=null)
    	{
    		moveInfo.ability.onUpdate(this);
    	}
    	if(!this.isDead&&getHeldItem()!=null&&getHeldItem().getItem() instanceof ItemPokemobUseable)
    	{
    		((IPokemobUseable)getHeldItem().getItem()).itemUse(getHeldItem(), this, null);
    	}
    	moves:
    	if(this.getLevel()>0)
    	{
    		for(String s: getMoves())
    		{
    			if(MovesUtils.isMoveImplemented(s))
    			{
    				break moves;
    			}
    		}
    		oldLevel = 1;
    		levelUp(getLevel());
    	}
    }
    
    protected void spawnPoisonParticle(){
        for (int i = 0; i < 2; i++)
        {
            //TODO Poison Effects
        }
    }
    
    protected void updateOngoingMoves(){
    	if(this.ticksExisted%40==0)
    	{
    		Set<Move_Ongoing> effects = new HashSet<Move_Ongoing>();
	    	for(Move_Ongoing m: moveInfo.ongoingEffects.keySet())
	    	{
	    		effects.add(m);
	    	}
	    	for(Move_Ongoing m: effects)
	    	{
	    		m.doOngoingEffect(this);
	    		int duration = moveInfo.ongoingEffects.get(m);
	    		if(duration==0)
	    			moveInfo.ongoingEffects.remove(m);
	    		else if (duration > 0)
	    			moveInfo.ongoingEffects.put(m, duration-1);
	    	}
    	}
    	if(moveInfo.DEFENSECURLCOUNTER>0)
    		moveInfo.DEFENSECURLCOUNTER--;
    	if(moveInfo.SELFRAISECOUNTER>0)
    		moveInfo.SELFRAISECOUNTER--;
    	if(moveInfo.TARGETLOWERCOUNTER>0)
    		moveInfo.TARGETLOWERCOUNTER--;
    	if(moveInfo.SPECIALCOUNTER>0)
    		moveInfo.SPECIALCOUNTER--;
    }
    
    protected void updateStatusEffect(){
    	int duration = 10;
    	
    	short timer = getStatusTimer();

    	if(timer>0)
    		setStatusTimer((short) (timer-1));
    	byte status = getStatus();
    	
    	ItemStack held = getHeldItem();
    	if (held !=null && held.getItem() instanceof ItemBerry){
    		if (BerryManager.berryEffect(this, held))
    		{
    			HappinessType.applyHappiness(this, HappinessType.BERRY);
    			setHeldItem(null);
    		}
    	}
    	
    	if(this.ticksExisted%20==0)
    	{
    		int statusChange = getChanges();
    		
			if((statusChange&CHANGE_CURSE)!=0)
			{
		        String message = StatCollector.translateToLocalFormatted(
		        		"pokemob.status.curse", 
		        		getPokemonDisplayName());
		        displayMessageToOwner("\u00a7c"+message);
				setHealth(getHealth() - getMaxHealth() * 0.25f);
			}
    	
    	}
    	
    	if(status == STATUS_NON)
    	{
    		if(getPokemonAIState(SLEEPING))
    		{
        		this.addPotionEffect(new PotionEffect(Potion.blindness.id, duration, 2));
    			this.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, duration, 100));
    		}
    		return;
    	}
    	if(this.ticksExisted%20==0)
    	{
        	
    		if (status == IMoveConstants.STATUS_BRN)
    		{
    			this.setFire(1);
    		}
    		else if (status == IMoveConstants.STATUS_FRZ)
    		{
    			this.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, duration, 100));
    			if(Math.random()>0.9)
    			{
    				healStatus();
    			}
    		}
    		else if (status == IMoveConstants.STATUS_PSN){
    			this.attackEntityFrom(DamageSource.magic, getMaxHealth()/8f);
    			spawnPoisonParticle();
    			
    		}
    		else if (status == IMoveConstants.STATUS_PSN2){
    			this.attackEntityFrom(DamageSource.magic, (moveInfo.TOXIC_COUNTER + 1 )* getMaxHealth()/16f);
    			spawnPoisonParticle();
    			spawnPoisonParticle();
    			moveInfo.TOXIC_COUNTER++;
    		}
    		else if (status == IMoveConstants.STATUS_SLP){
        		this.addPotionEffect(new PotionEffect(Potion.blindness.id, duration, 2));
    			this.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, duration, 100));
    			
    			if(Math.random()>0.9 || timer <=0)
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
    public byte getStatus()
    {
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	
        return (byte) (value & 0xff);
    }

    @Override
    public void removeChanges(int changes)
    {
    	this.moveInfo.changes -= changes;
    }

    @Override
    public int getChanges()
    {
        return moveInfo.changes;
    }

    @Override
    public boolean addChange(int change){
    	int old = moveInfo.changes;
    	moveInfo.changes |= change;
    	return moveInfo.changes != old;
    }

    @Override
    public int getMoveIndex() {
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	
        return (value>>8) & 0xff;
	}

    @Override
	public void setMoveIndex(int moveIndex) {

    	if(getMove(moveIndex) == null)
    	{
    		setMoveIndex(5);
    	}
    	
		if (moveIndex == getMoveIndex())
			return;
		
		moveInfo.ROLLOUTCOUNTER = 0;
		moveInfo.FURYCUTTERCOUNTER = 0;
		
        if (mod_Pokecube.isOnClientSide()&&getPokemonAIState(IPokemob.TAMED))
        {
            try
            {
                String toSend = getEntityId() + "`i`";
                toSend += moveIndex;

                byte[] message = toSend.getBytes();
                PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubePacketHandler.CHANNEL_ID_EntityPokemob, message);
                PokecubePacketHandler.sendToServer(packet);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
	    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
	    	int toSet = moveIndex << 8;
	        value = (value & 0xffff00ff) | toSet;
	        dataWatcher.updateObject(STATUSMOVEINDEXDW, value);
        }
	}

    @Override
	public void setStatusTimer(short timer){
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	
    	timer = (short) Math.max(0, timer);
    	
    	int toSet = (timer) << 16;
        value = (value & 0x0000ffff) | toSet;
        dataWatcher.updateObject(STATUSMOVEINDEXDW, value);
    }
    
    @Override
	public short getStatusTimer(){
    	int value = dataWatcher.getWatchableObjectInt(STATUSMOVEINDEXDW);
    	
        return (short) ((value>>16) & 0xffff);
    }
    
	private int moveIndexCounter = 0;

    @Override
	public void executeMove(Entity target, Vector3 targetLocation, float f) {
    	
		if(getMove(getMoveIndex()) == MOVE_NONE)
		{
			return;
		}
		updatePos();
		
		if(target instanceof EntityLiving)
		{
			EntityLiving t = (EntityLiving) target;
			if(t.getAttackTarget()==null)
			{
				t.setAttackTarget(this);
			}
		}
		int statusChange = getChanges();
		
		if((statusChange&CHANGE_FLINCH)!=0)
		{
	        String message = StatCollector.translateToLocalFormatted(
	        		"pokemob.status.flinch", 
	        		getPokemonDisplayName());
	        displayMessageToOwner("\u00a7c"+message);
			
			removeChanges(CHANGE_FLINCH);
			return;
		}
		
		if((statusChange&CHANGE_CONFUSED)!=0)
		{
			if(Math.random()>0.75)
			{
				removeChanges(CHANGE_CONFUSED);
		        String message = StatCollector.translateToLocalFormatted(
		        		"pokemob.status.confuse.remove", 
		        		getPokemonDisplayName());
		        displayMessageToOwner("\u00a7a"+message);
			}
			else if(Math.random()>0.5)
			{
	        	MovesUtils.doAttack("pokemob.status.confusion", this, this, f);
				return;
			}
		}
		
    	if (this.getPokemonAIState(IPokemob.TAMED))
        {
        	// A tamed pokemon should not attack a player
        	// but it must keep it as a target.
    		
    		String attack = getMove(getMoveIndex());
    		
    		if(attack==null)
    		{
    			new Exception().getStackTrace();
    			return;
    		}
    		
    		if(attack.equalsIgnoreCase(MOVE_METRONOME))
    		{
        		attack = null;
        		ArrayList<MoveEntry> moves = new ArrayList<MoveEntry>(MoveEntry.values());
        		while(attack==null)
        		{
        			Collections.shuffle(moves);
        			MoveEntry move = moves.iterator().next();
        			if(move!=null)
        				attack = move.name;
        			
        		}
    		}
    		
        	MovesUtils.doAttack(attack, this, target, f);
        }
        else
        {
        	if (moveIndexCounter++ > rand.nextInt(30)) {
	            int nb = rand.nextInt(5);
	            String move = getMove(nb);
	
	            while (move == null && nb > 0)
	            {
	                nb = rand.nextInt(nb);
	            }
	            moveIndexCounter = 0;
	            setMoveIndex(nb);
        	}
            MovesUtils.doAttack(getMove(getMoveIndex()), this, target, f);
        }
	}
    
    @Override
	public void addOngoingEffect(Move_Base effect)
    {
    	if(effect instanceof Move_Ongoing)
    		moveInfo.ongoingEffects.put((Move_Ongoing) effect, ((Move_Ongoing)effect).getDuration());
    }
    
    
    @Override
	public void onMoveUse(MovePacket move)
    {
    	Move_Base attack = move.getMove();
    	
    	IPokemob attacker = move.attacker;
    	Entity attacked = move.attacked;
    	

    	if(moveInfo.substituteHP>0 && attacked == this)
    	{
    		float damage = MovesUtils.getAttackStrength(attacker, (IPokemob) attacked, move.getMove().move.category, move.PWR, attack);
    		attacker.displayMessageToOwner("\u00a7c"+"Move Absorbed by substitute");
    		displayMessageToOwner("\u00a7a"+"move absorbed by substitute");
    		moveInfo.substituteHP -= damage;
    		if(moveInfo.substituteHP<0)
    		{
        		attacker.displayMessageToOwner("\u00a7a"+"substitute broke");
        		displayMessageToOwner("\u00a7c"+"substitute broke");
    		}
    		move.failed = true;
    		move.PWR = 0;
    		move.changeAddition = 0;
    		move.statusChange = 0;
    	}
    	
    	if(attacker==this && attack.getName().equals(MOVE_SUBSTITUTE))
    	{
    		moveInfo.substituteHP = getMaxHealth()/4;
    	}
    	
    	if(moveInfo.ability!=null)
    	{
    		moveInfo.ability.onMoveUse(this, move);
    	}
    	
    	if(attacker == this && this.isType(move.attackType))
    	{
    		move.PWR *= move.stabFactor;
    	}
    	
    	if(attack.getName().equals(MOVE_FALSESWIPE))
    	{
    		move.noFaint = true;
    	}
    	
    	if(attack.getName().equals(MOVE_PROTECT)||attack.getName().equals(MOVE_DETECT)&&!moveInfo.blocked)
    	{
    		moveInfo.blockTimer = 30;
    		moveInfo.blocked = true;
    		moveInfo.BLOCKCOUNTER++;
    	}
    	boolean blockMove = false;
    	
    	for(String s: MoveEntry.protectionMoves)
    		if(s.equals(move.attack))
    		{
    			blockMove = true;
    			break;
    		}
    	
    	if(move.attacker==this&&!blockMove&&moveInfo.blocked)
    	{
    		moveInfo.blocked = false;
    		moveInfo.blockTimer = 0;
    		moveInfo.BLOCKCOUNTER = 0;
    	}
    		
    	boolean unblockable = false;
    	for(String s: MoveEntry.unBlockableMoves)
    		if(s.equals(move.attack))
    		{
    			unblockable = true;
    			System.out.println("Unblockable");
    			break;
    		}
    	
    	if(moveInfo.blocked&&move.attacked!=move.attacker&&!unblockable)
    	{
    		float count = Math.min(0, moveInfo.BLOCKCOUNTER-1);
    		float chance = count!=0?Math.max(0.125f, ((1/(count*2)))):1;
    		if(chance>Math.random())
    		{
    			move.canceled = true;
    		}
    		else
    		{
    			move.failed = true;
    		}
    	}
    	if(moveInfo.BLOCKCOUNTER>0)
    		moveInfo.BLOCKCOUNTER--;
    }
    
    @Override
	public void levelUp(int level) 
    {
    	List<String> moves = Database.getLevelUpMoves(this.getPokedexNb(), level, oldLevel);
    	Collections.shuffle(moves);
		HappinessType.applyHappiness(this, HappinessType.LEVEL);
    	if(moves!=null)
    	{
    		if(this.getPokemonAIState(IPokemob.TAMED))
    		{
	    		String[] current = getMoves();
	    		if(current[3]!=null)
	    		{
	    			for(String s: current)
	    			{
	    				for(String s1:moves)
	    				{
	    					if(s.equals(s1))
	    						return;
	    				}
	    			}
	    	    	for(String s:moves)
	    	    	{
	    	        	String message = StatCollector.translateToLocalFormatted(
	    	            		"pokemob.move.notify.learn", 
	    	            		getPokemonDisplayName(),
	    	            		MovesUtils.getTranslatedMove(s));
	    	        	displayMessageToOwner(message);
	    	        	moveInfo.newMoves++;
	    	    	}
	    	    	setPokemonAIState(LEARNINGMOVE, true);
	    			return;
	    		}
    		}
	    	for(String s:moves)
	    	{
	    		((EntityPokemob)this).learn(s);
	    	}
    	}
    }
	
    public int getExplosionState()
    {
        return dataWatcher.getWatchableObjectByte(BOOMSTATEDW);
    }
    public void setExplosionState(int i)
    {
    	if(i==0)
    		moveInfo.Exploding = true;
        dataWatcher.updateObject(BOOMSTATEDW, Byte.valueOf((byte) i));
    }
	
    @SideOnly(Side.CLIENT)
    /**
     * Params: (Float)Render tick. Returns the intensity of the creeper's flash when it is ignited.
     */
    public float getCreeperFlashIntensity(float par1)
    {
        return (this.moveInfo.lastActiveTime + (this.moveInfo.timeSinceIgnited - this.moveInfo.lastActiveTime) * par1) / (this.moveInfo.fuseTime - 2);
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
                worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
            }
            
            moveInfo.timeSinceIgnited += i;

            if (moveInfo.timeSinceIgnited < 0)
            {
            	moveInfo.timeSinceIgnited = 0;
            }

            if (moveInfo.timeSinceIgnited >= 30)
            {
            	moveInfo.timeSinceIgnited = 30;
            }
        }
        if (getAttackTarget() == null && moveInfo.timeSinceIgnited > 0)//
        {
            setExplosionState(-1);
            moveInfo.timeSinceIgnited--;

            if (moveInfo.timeSinceIgnited < 0)
            {
            	moveInfo.timeSinceIgnited = 0;
            }
        }
    }
    
    public String[] getLearnableMoves()
    {
    	List<String> moves = Database.getLearnableMoves(this.getPokedexNb());
    	return moves.toArray(new String[0]);
    };
    
    /**
     * Teleport the enderman to a random nearby position
     */
    public boolean teleportRandomly()
    {
        double var1 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double var3 = this.posY + (this.rand.nextInt(64) - 32);
        double var5 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(var1, var3, var5);
    }
    
    /**
     * Teleport the enderman
     */
    protected boolean teleportTo(double par1, double par3, double par5)
    {
        double var7 = this.posX;
        double var9 = this.posY;
        double var11 = this.posZ;
        this.posX = par1;
        this.posY = par3;
        this.posZ = par5;
        boolean var13 = false;
        int var14 = MathHelper.floor_double(this.posX);
        int var15 = MathHelper.floor_double(this.posY);
        int var16 = MathHelper.floor_double(this.posZ);
        Block var18;
        BlockPos pos = new BlockPos(var14, var15, var16);
        
        if (this.worldObj.isAreaLoaded(pos, 1))
        {
            boolean var17 = false;

            while (!var17 && var15 > 0)
            {
                var18 = this.worldObj.getBlockState(pos.down()).getBlock();

                if (!var18.isAir(worldObj, pos.down()) && var18.getMaterial().blocksMovement())
                {
                    var17 = true;
                }
                else
                {
                    --this.posY;
                    --var15;
                }
            }

            if (var17)
            {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.worldObj.isAnyLiquid(this.getEntityBoundingBox()))
                {
                    var13 = true;
                }
            }
        }

        if (!var13)
        {
            this.setPosition(var7, var9, var11);
            return false;
        }
        else
        {
            short var30 = 128;
            int num;

            for (num = 0; num < var30; ++num)
            {
                double var19 = num / (var30 - 1.0D);
                float var21 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float var22 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float var23 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double var24 = var7 + (this.posX - var7) * var19 + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D;
                double var26 = var9 + (this.posY - var9) * var19 + this.rand.nextDouble() * this.height;
                double var28 = var11 + (this.posZ - var11) * var19 + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D;
                this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, var24, var26, var28, var21, var22, var23);
            }

            this.worldObj.playSoundEffect(var7, var9, var11, "mob.endermen.portal", 1.0F, 1.0F);
            this.playSound("mob.endermen.portal", 1.0F, 1.0F);
            return true;
        }
    }
    int attackTime;
    public void setHasAttacked(String move)
    {
    	attackTime = MovesUtils.getDelayBetweenAttacks(this, move);
    }
    
    public int getLastAttackTick()
    {
    	return attackTime;
    }
    
    public void setLastAttackTick(int tick)
    {
    	attackTime = tick;
    }
    
    @Override
    /**
     * Reduces damage, depending on armor
     */
    protected float applyArmorCalculations(DamageSource source, float damage)
    {
        if (!source.isUnblockable())
        {
        	int armour = source instanceof PokemobDamageSource ? super.getTotalArmorValue() : this.getTotalArmorValue();
            int i = 25 - armour;
            float f1 = damage * (float)i;
            this.damageArmor(damage);
            damage = f1 / 25.0F;
        }

        return damage;
    }
    
    @Override
    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue()
    {
        EnumDifficulty diff = worldObj.getDifficulty();
        int comp = diff.compareTo(EnumDifficulty.NORMAL);
        if(comp < 0)
        {
        	return super.getTotalArmorValue();
        }
        int i = (getActualStats()[2] + getActualStats()[4])/25;
        return i;
    }

//    @Override
//	protected void attackEntity(Entity entity, float f){
//    	attackEntityAsPokemob(entity, f);
//    }
    
    @Override
    public boolean attackEntityAsMob(Entity par1Entity) {
    	if (this.getAttackTarget() instanceof EntityLivingBase){
            float distanceToEntity = this.getAttackTarget().getDistanceToEntity(this);
        	attackEntityAsPokemob(par1Entity, distanceToEntity);
    	}
    	return super.attackEntityAsMob(par1Entity);
    }
    
    protected void attackEntityAsPokemob(Entity entity, float f)
    {
    	if(getLover()==entity)
    		return;
    	Vector3 v = Vector3.getNewVectorFromPool().set(entity);
        executeMove(entity, v, f);
        v.freeVectorFromPool();
    }
    
    public void setTransformedTo(Entity to)
    {
    	transformedTo = to;
    }
    
    public Entity getTransformedTo()
    {
    	return transformedTo;
    }
    
    /**
     * Use this for anything that does not change or need to be updated.
     */
	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(abilityNumber);
		moveInfo.ability = getPokedexEntry().getAbility(abilityNumber);
		super.writeSpawnData(data);
	}
	
    /**
     * Use this for anything that does not change or need to be updated.
     */
	@Override
	public void readSpawnData(ByteBuf data) {
		abilityNumber = data.readInt();
		moveInfo.ability = getPokedexEntry().getAbility(abilityNumber);
		super.readSpawnData(data);
	}
	
	private void updatePos()
	{
		if(!worldObj.isRemote)
		{
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeByte(PokemobPacketHandler.MESSAGEPOSUPDATE);
			buffer.writeInt(getEntityId());
			buffer.writeByte(0);
			buffer.writeFloat((float) posX);
			buffer.writeFloat((float) posY);
			buffer.writeFloat((float) posZ);
			MessageClient message = new MessageClient(buffer);
			PokecubePacketHandler.sendToAllNear(message, here, dimension, 32);
		}
	}
	
	@Override
	public HashMap<Move_Ongoing, Integer> getOngoingEffects()
	{
		return moveInfo.ongoingEffects;
	}
	
	@Override
	public PokemobMoveStats getMoveStats()
	{
		return moveInfo;
	}
	
	@Override
	public void setWeapon(int index, Entity weapon)
	{
		if(index==0)
			moveInfo.weapon1 = weapon;
		else
			moveInfo.weapon2 = weapon;
	}

	@Override
	public Entity getWeapon(int index)
	{
		return index==0?moveInfo.weapon1:moveInfo.weapon2;
	}

	@Override
	public void setLeaningMoveIndex(int num)
	{
		this.moveInfo.num = num;
	}
}
