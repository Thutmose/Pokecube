/**
 * 
 */
package pokecube.core.moves;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

/** This class extends {@link EntityDamageSource} and only modifies the death
 * message.
 * 
 * @author Manchou */
public class PokemobDamageSource extends DamageSource
{

    private EntityLivingBase damageSourceEntity;
    // TODO use this for damage stuff
    public Move_Base         move;
    public IPokemob          user;

    /** @param par1Str
     * @param par2Entity */
    public PokemobDamageSource(String par1Str, EntityLivingBase par2Entity, Move_Base type)
    {
        super(par1Str);
        damageSourceEntity = par2Entity;
        user = CapabilityPokemob.getPokemobFor(par2Entity);
        move = type;
    }

    @Override
    public ITextComponent getDeathMessage(EntityLivingBase par1EntityPlayer)
    {
        ItemStack localObject = (this.damageSourceEntity != null) ? user.getHeldItem() : null;
        if ((localObject != null) && (localObject.hasDisplayName()))
            return new TextComponentTranslation("death.attack." + this.damageType,
                    new Object[] { par1EntityPlayer.getDisplayName(), this.damageSourceEntity.getDisplayName(),
                            localObject.getTextComponent() });
        IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getPokemonOwner() != null)
        {
            TextComponentTranslation message = new TextComponentTranslation("pokemob.killed.tame",
                    par1EntityPlayer.getDisplayName(), sourceMob.getPokemonOwner().getDisplayName(),
                    this.damageSourceEntity.getDisplayName());
            return message;
        }
        else if (sourceMob != null && sourceMob.getPokemonOwner() == null
                && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            TextComponentTranslation message = new TextComponentTranslation("pokemob.killed.wild",
                    par1EntityPlayer.getDisplayName(), this.damageSourceEntity.getDisplayName());
            return message;
        }
        return new TextComponentTranslation("death.attack." + this.damageType,
                new Object[] { par1EntityPlayer.getDisplayName(), this.damageSourceEntity.getDisplayName() });
    }

    @Override
    public Entity getTrueSource()
    {
        IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getOwner() != null) return sourceMob.getOwner();
        if (this.damageSourceEntity instanceof IEntityOwnable)
        {
            Entity owner = ((IEntityOwnable) this.damageSourceEntity).getOwner();
            return owner != null ? owner : this.damageSourceEntity;
        }
        return this.damageSourceEntity;
    }

    @Nullable
    @Override
    public Entity getImmediateSource()
    {
        return this.damageSourceEntity;
    }

    @Override
    /** Returns true if the damage is projectile based. */
    public boolean isProjectile()
    {
        return (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) != 0;
    }
}
