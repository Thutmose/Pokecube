package pokecube.adventures.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityTarget extends EntityLiving
{

    int time = 0;

    public EntityTarget(World par1World)
    {
        super(par1World);
        this.setSize(0.25f, 0.25f);
        this.setAlwaysRenderNameTag(false);
    }

    /** Deals damage to the entity. If its a EntityPlayer then will take damage
     * from the armor first and then health second with the reduced value. Args:
     * damageAmount */
    @Override
    protected void damageEntity(DamageSource par1DamageSource, float par2)
    {
        Entity source = par1DamageSource.getTrueSource();
        if (source != null && source instanceof EntityLiving)
        {
            EntityLiving ent = (EntityLiving) source;
            ent.setAttackTarget(null);
        }
        if (source != null && source.isSneaking())
        {
            this.setDead();
        }
    }

    /** Returns the current armor value as determined by a call to
     * InventoryPlayer.getTotalArmorValue */
    @Override
    public int getTotalArmorValue()
    {
        return 0;
    }

    @Override
    public void onUpdate()
    {
        time++;
        this.lastDamage = 0;
        if (time > 1000) this.setDead();
    }

}
