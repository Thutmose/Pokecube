/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
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
                armour = (int) ((getStat(Stats.SPDEFENSE, true)) / 12.5);
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
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        pokemobCap.executeMove(target, targetLocation, f);
    }

    @Override
    public int getExplosionState()
    {
        return pokemobCap.getExplosionState();
    }

    @Override
    public int getMoveIndex()
    {
        return pokemobCap.getMoveIndex();
    }

    @Override
    public String[] getMoves()
    {
        return super.getMoves();
    }

    @Override
    public PokemobMoveStats getMoveStats()
    {
        return pokemobCap.getMoveStats();
    }

    @Override
    public byte getStatus()
    {
        return pokemobCap.getStatus();
    }

    @Override
    public short getStatusTimer()
    {
        return pokemobCap.getStatusTimer();
    }

    @Override
    /** Returns the current armor value as determined by a call to
     * InventoryPlayer.getTotalArmorValue */
    public int getTotalArmorValue()
    {
        return (int) ((getStat(Stats.DEFENSE, true)) / 12.5);
    }

    @Override
    public Entity getTransformedTo()
    {
        return pokemobCap.getTransformedTo();
    }

    @Override
    public void healStatus()
    {
        pokemobCap.healStatus();
    }

    @Override
    public void setExplosionState(int i)
    {
        pokemobCap.setExplosionState(i);
    }

    @Override
    public void setMoveIndex(int moveIndex)
    {
        pokemobCap.setMoveIndex(moveIndex);
    }

    @Override
    public boolean setStatus(byte status)
    {
        return pokemobCap.setStatus(status);
    }

    @Override
    public void setStatusTimer(short timer)
    {
        pokemobCap.setStatusTimer(timer);
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        pokemobCap.setTransformedTo(to);
    }

    @Override
    public int getAttackCooldown()
    {
        return pokemobCap.getAttackCooldown();
    }

    @Override
    public void setAttackCooldown(int timer)
    {
        pokemobCap.setAttackCooldown(timer);;
    }

    @Override
    public String getLastMoveUsed()
    {
        return pokemobCap.getLastMoveUsed();
    }
}
