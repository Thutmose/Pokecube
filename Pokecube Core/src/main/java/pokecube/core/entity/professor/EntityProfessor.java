package pokecube.core.entity.professor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class EntityProfessor extends EntityAgeable
{
	public ItemStack[] pokecubes = new ItemStack[6];
	public int[] attackCooldown = new int[6];
	public int cooldown = 0;
	public String name = "";
	public int out = -1;
	public boolean male = true;
	public boolean stationary = false;
	
    public EntityProfessor(World par1World) {
		this(par1World, null);
	}
    
    public EntityProfessor(World world, Vector3 location)
    {
    	this(world, location, false);
    }
	
    public EntityProfessor(World par1World, Vector3 location, boolean stationary) {
		super(par1World);

        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIMoveTowardsTarget(this, 0.6, 10));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        if(location!=null)
        {
        	location.moveEntity(this);
        
	    	setStationary(location);
        }
	}
    
    public void setStationary(Vector3 location)
    {
    	stationary = true;
    	this.tasks.addTask(2, new GuardAI(this, new BlockPos(location.intX(), location.intY(), location.intZ()),
				1.0f, 48.0f, new TimePeriod(0.00, 1), false));
    }
    
    public void setStationary(boolean stationary)
    {
    	if(stationary&&!this.stationary)
    		setStationary(Vector3.getNewVectorFromPool().set(this));
    	else if(!stationary && this.stationary)
    	{
    		for(Object o: this.tasks.taskEntries)
    			if(o instanceof GuardAI)
    				this.tasks.removeTask((EntityAIBase) o);
    		this.stationary = stationary;
    	}
    }
    
    @Override
    protected void entityInit()
    {
        super.entityInit();
    }
    
    @Override
	public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        int n = 0;
        for(ItemStack i: pokecubes)
        {
        	if(i!=null)
        	{
        		NBTTagCompound tag = new NBTTagCompound();
        		i.writeToNBT(tag);
        		nbt.setTag("slot"+n, tag);
        		n++;
        	}
        }
        nbt.setBoolean("gender", male);
        nbt.setString("name", name);
        nbt.setInteger("pokemob out", out);
        nbt.setBoolean("stationary", stationary);
        nbt.setInteger("cooldown", cooldown);
        nbt.setIntArray("cooldowns", attackCooldown);
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        for(int n = 0; n<6; n++)
        {
        	NBTBase temp = nbt.getTag("slot"+n);
        	if(temp instanceof NBTTagCompound)
        	{
        		NBTTagCompound tag = (NBTTagCompound) temp;
        		pokecubes[n] = ItemStack.loadItemStackFromNBT(tag);
        	}
        }
        out = nbt.getInteger("pokemob out");
        stationary = nbt.getBoolean("stationary");
        cooldown = nbt.getInteger("cooldown");
        attackCooldown = nbt.getIntArray("cooldowns");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        if(attackCooldown.length!=6)
        	attackCooldown = new int[6];
        
    }
    
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
    	Entity e = source.getSourceOfDamage();
    	if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
    	{
    		this.setDead();
    	}
    	return false;
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }
  
    @Override
	public boolean interact(EntityPlayer player)
    {
        if (!worldObj.isRemote)
        {
        	if(!PokecubeSerializer.getInstance().hasStarter(player))
        	{
	        	PokecubeClientPacket packet = new PokecubeClientPacket(new byte[] {PokecubePacketHandler.CHANNEL_ID_ChooseFirstPokemob});
	        	PokecubePacketHandler.sendToClient(packet, player);
        	}
        	else
        	{
        		player.addChatMessage(new ChatComponentText("Professor: You have already recieved a pokemon"));
        	}
        }
    	
    	return false;//super.interact(entityplayer);
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }
    
    @Override
    protected boolean canDespawn()
    {
    	return false;
    }

	@Override
	public EntityAgeable createChild(EntityAgeable p_90011_1_) {
		return null;
	}
	
}
