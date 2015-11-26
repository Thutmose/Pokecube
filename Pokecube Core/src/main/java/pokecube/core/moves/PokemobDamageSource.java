/**
 * 
 */
package pokecube.core.moves;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IChatComponent;
import pokecube.core.utils.PokeType;

/**
 * This class extends {@link EntityDamageSource} and only modifies the death message.
 * 
 * @author Manchou
 *
 */
public class PokemobDamageSource extends DamageSource {

	private EntityLivingBase damageSourceEntity;
	//TODO use this for damage stuff
	public PokeType moveType;

	/**
	 * @param par1Str
	 * @param par2Entity
	 */
	public PokemobDamageSource(String par1Str, EntityLivingBase par2Entity, PokeType type) {
		super(par1Str);
		damageSourceEntity = par2Entity;
		moveType = type;
	}
	
	@Override
	public IChatComponent getDeathMessage(EntityLivingBase par1EntityPlayer)
    {
		ItemStack localObject = (this.damageSourceEntity instanceof EntityLivingBase) ? this.damageSourceEntity.getHeldItem() : null;
		if ((localObject != null) && (localObject.hasDisplayName()))
		return new ChatComponentTranslation("death.attack." + this.damageType, new Object[] { par1EntityPlayer.getDisplayName(), this.damageSourceEntity.getDisplayName(), localObject.getChatComponent() });
		
		return new ChatComponentTranslation("death.attack." + this.damageType, new Object[] { par1EntityPlayer.getDisplayName(), this.damageSourceEntity.getDisplayName() });
    }
    @Override
	public Entity getEntity()
    {
        return this.damageSourceEntity;
    }
}
