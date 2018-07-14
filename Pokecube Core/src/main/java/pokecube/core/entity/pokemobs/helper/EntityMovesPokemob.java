/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.PokemobDamageSource;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityMovesPokemob extends EntitySexedPokemob
{
    /** @param par1World */
    public EntityMovesPokemob(World world)
    {
        super(world);
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
                armour = (int) ((pokemobCap.getStat(Stats.SPDEFENSE, true)) / 12.5);
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
            float distanceToEntity = this.getAttackTarget().getDistance(this);
            attackEntityAsPokemob(par1Entity, distanceToEntity);
        }
        return super.attackEntityAsMob(par1Entity);
    }

    protected void attackEntityAsPokemob(Entity entity, float f)
    {
        if (pokemobCap.getLover() == entity) return;
        Vector3 v = Vector3.getNewVector().set(entity);
        pokemobCap.executeMove(entity, v, f);
    }

    @Override
    /** Returns the current armor value as determined by a call to
     * InventoryPlayer.getTotalArmorValue */
    public int getTotalArmorValue()
    {
        return (int) ((pokemobCap.getStat(Stats.DEFENSE, true)) / 12.5);
    }
}
