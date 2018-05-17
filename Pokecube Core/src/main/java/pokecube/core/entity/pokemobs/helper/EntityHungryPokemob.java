package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.ChunkCoordinate;

public abstract class EntityHungryPokemob extends EntityAiPokemob
{
    int fleeingTick;

    public EntityHungryPokemob(World world)
    {
        super(world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        float damage = i;

        Entity attacker = null;

        if (source instanceof EntityDamageSource && source.getTrueSource() != this.getOwner())
        {
            attacker = source.getTrueSource();

            if ((attacker instanceof EntityArrow) && ((EntityArrow) attacker).shootingEntity != null)
            {// To test
                attacker = ((EntityArrow) attacker).shootingEntity;
            }
            if (attacker instanceof EntityLivingBase && attacker != getAttackTarget())
                this.setAttackTarget((EntityLivingBase) attacker);
        }

        if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED) && ((attacker instanceof EntityPlayer
                && ((EntityPlayer) attacker) == pokemobCap.getOwner()))) { return false; }
        pokemobCap.setPokemonAIState(IMoveConstants.SITTING, false);

        EntityLivingBase oldTarget = getAttackTarget();
        if (super.attackEntityFrom(source, damage))
        {
            fleeingTick = 0;
            if (oldTarget != null && getAttackTarget() != oldTarget) setAttackTarget(oldTarget);
            ChunkCoordinate c = null;
            if (oldTarget != null)
            {
                c = new ChunkCoordinate(vec.set(oldTarget), dimension);
            }
            if (!pokemobCap.getPokemonAIState(IMoveConstants.TAMED))
            {
                if (attacker instanceof EntityPlayer)
                {
                    pokemobCap.setPokemonAIState(IMoveConstants.ANGRY, true);
                }
                if (attacker instanceof EntityLivingBase && getAttackTarget() != attacker)
                {
                    setAttackTarget((EntityLivingBase) attacker);
                    fleeingTick = 0;
                }
            }
            if (attacker != this && attacker instanceof EntityLivingBase && getAttackTarget() != attacker)
            {
                setAttackTarget((EntityLivingBase) attacker);
                fleeingTick = 0;
            }
            IPokemob agres = CapabilityPokemob.getPokemobFor(attacker);
            if (agres != null)
            {
                if (agres.getPokedexEntry().isFood(pokemobCap.getPokedexEntry())
                        && agres.getPokemonAIState(IMoveConstants.HUNTING) && c != null)
                {
                    fleeingTick = 100;
                }
                if (agres.getLover() == this)
                {
                    agres.setLover(attacker);
                }
                if (getAttackTarget() != attacker) setAttackTarget((EntityLivingBase) attacker);
                fleeingTick = 0;
            }

            return true;
        }
        else if (oldTarget != null && oldTarget != getAttackTarget())
        {
            setAttackTarget(oldTarget);
            fleeingTick = 0;
            return false;
        }
        return false;
    }
}
