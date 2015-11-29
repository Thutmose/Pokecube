package pokecube.adventures.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;

public class EntityTarget extends EntityLiving {

	public EntityTarget(World par1World) {
		super(par1World);
		this.setSize(0.25f, 0.25f);
	}
	
	int time = 0;
	
    @Override
    public void onUpdate()
    {
      //  super.onUpdate();
//        time++;
//        rotationYaw = rotationYaw%360;
//        prevRotationYawHead = rotationYaw;
//        prevRotationYaw = rotationYaw;
//        rotationYaw += 1;
//        rotationYawHead = rotationYaw;
    	this.lastDamage = 0;
//        worldObj.spawnParticle(EnumParticleTypes.CRIT_MAGIC, posX, posY, posZ, 0, 0, 0);
        
        if(time>1000)
        	this.setDead();
    }
	
    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    @Override
	protected void damageEntity(DamageSource par1DamageSource, float par2)
    {
    	Entity source = par1DamageSource.getEntity();
    	if(source!=null && source instanceof EntityLiving)
    	{
    		EntityLiving ent = (EntityLiving) source;
    		ent.setAttackTarget(null);
    		if(ent instanceof IPokemob)
    		{
    			((IPokemob)ent).setPokemonAIState(IPokemob.ANGRY, false);
    		}
    	}
    	if(source!=null && source.isSneaking())
    	{
    		this.setDead();
    	}
    }
	
    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    @Override
	public int getTotalArmorValue()
    {
    	return 0;
    }

}
