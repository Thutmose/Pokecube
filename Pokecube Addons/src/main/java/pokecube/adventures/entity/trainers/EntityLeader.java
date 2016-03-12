package pokecube.adventures.entity.trainers;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import pokecube.adventures.ai.trainers.EntityAITrainer;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityLeader extends EntityTrainer {

	public ArrayList<String> defeaters = new ArrayList<String>();
	String badge = "";
	
	public EntityLeader(World world) {
		super(world);
		setAIState(STATIONARY, true);
	}
    
    public EntityLeader(World world, TypeTrainer type, int maxXp, Vector3 location)
    {
    	this(world, location);
    	setId(PASaveHandler.getInstance().getNewId());
    	initTrainer(type, maxXp);
    }
	
    public EntityLeader(World par1World, Vector3 location) {
		super(par1World);

        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAITrainer(this, EntityPlayer.class));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        if(location!=null)
        {
        	location.moveEntity(this);
	    	setStationary(location);
        }
	}
	
    public boolean hasDefeated(Entity e)
	{
		if(e==null)
			return false;
		String name = e.getUniqueID().toString();
		for(String s: defeaters)
		{
			if(s.equals(name))
				return true;
		}
		return false;
	}
    
    @Override
    public void initTrainer(TypeTrainer type, int level)
    {
		int alevel = Tools.xpToLevel(level, Database.getEntry(1).getEvolutionMode());
		level = Tools.levelToXp(Database.getEntry(1).getEvolutionMode(), alevel+5);
		
    	this.type = type;
    	byte genders = type.genders;
    	if(genders==1)
    		male = true;
    	if(genders==2)
    		male = false;
    	if(genders==3)
    		male = Math.random()<0.5;

    	TypeTrainer.getRandomTeam(this, level, pokecubes, worldObj);
    	setTypes();
    }

	
	@Override
	public boolean interact(EntityPlayer entityplayer)
    {
    	if(!entityplayer.capabilities.isCreativeMode)
    		return false;

    	if(entityplayer.getHeldItem()!=null && entityplayer.getHeldItem().getUnlocalizedName().toLowerCase().contains("badge"))
    	{
    		this.badge = entityplayer.getHeldItem().getUnlocalizedName().replace("item.", "");
            this.setCurrentItemOrArmor(1, PokecubeItems.getStack(badge));
            entityplayer.addChatMessage(new ChatComponentText("Badge set to "+this.getEquipmentInSlot(1)));
    	}
    	
    	return super.interact(entityplayer);
    }
	
	@Override
	public void onDefeated(Entity defeater)
	{
		if(hasDefeated(defeater)) return;
		
		defeaters.add(defeater.getUniqueID().toString());
		
		for(int i = 1; i<5; i++)
		{
			ItemStack stack = getEquipmentInSlot(i);
			if(stack!=null)
				this.entityDropItem(stack, 0.5f);
		}
	}
	
	@Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        NBTTagCompound names = nbt.getCompoundTag("defeaters");
        int n = defeaters.size();
        for(int i = 0; i<n; i++)
        {
     	   defeaters.add(names.getString(""+i));
        }
        badge = nbt.getString("badge");
        this.setCurrentItemOrArmor(1, PokecubeItems.getStack(badge));
    }
	
    @Override
	public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
       NBTTagCompound names = new NBTTagCompound();
       int n = defeaters.size();
       for(int i = 0; i<n; i++)
       {
    	   names.setString(""+i, defeaters.get(i));
       }
       nbt.setTag("defeaters", names);
       nbt.setString("badge", badge);
        
    }
}
