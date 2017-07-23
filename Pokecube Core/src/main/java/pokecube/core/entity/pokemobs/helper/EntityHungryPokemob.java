package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public abstract class EntityHungryPokemob extends EntityAiPokemob
{
    int           fleeingTick;

    public EntityHungryPokemob(World world)
    {
        super(world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        float damage = i;

        Entity attacker = null;

        if (source instanceof EntityDamageSource)
        {
            attacker = source.getEntity();

            if ((attacker instanceof EntityArrow) && ((EntityArrow) attacker).shootingEntity != null)
            {// To test
                attacker = ((EntityArrow) attacker).shootingEntity;
            }
            if (attacker instanceof EntityLivingBase && attacker != getAttackTarget())
                this.setAttackTarget((EntityLivingBase) attacker);
        }

        if (getPokemonAIState(IMoveConstants.TAMED)
                && ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker) == getOwner()))) { return false; }
        setPokemonAIState(SITTING, false);

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
            if (!getPokemonAIState(IMoveConstants.TAMED))
            {
                if (attacker instanceof EntityPlayer)
                {
                    setPokemonAIState(ANGRY, true);
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

            if (attacker instanceof IPokemob)
            {
                IPokemob agres = (IPokemob) attacker;

                if (agres.getPokedexEntry().isFood(getPokedexEntry()) && agres.getPokemonAIState(HUNTING) && c != null)
                {
                    fleeingTick = 100;
                }

                if (((EntityPokemob) attacker).getLover() == this)
                {
                    this.setLover(attacker);
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

    @Override
    public void eat(Entity e)
    {
        pokemobCap.eat(e);
    }

    @Override
    public boolean eatsBerries()
    {
        return pokemobCap.eatsBerries();
    }

    @Override
    public boolean filterFeeder()
    {
        return pokemobCap.filterFeeder();
    }

    @Override
    public boolean floats()
    {
        return pokemobCap.floats();
    }

    @Override
    public boolean flys()
    {
        return pokemobCap.flys();
    }

    @Override
    public float getBlockPathWeight(IBlockAccess world, Vector3 location)
    {
        return pokemobCap.getBlockPathWeight(world, location);
    }

    @Override
    public double getFloatHeight()
    {
        return pokemobCap.getFloatHeight();
    }

    @Override
    public int getHungerCooldown()
    {
        return pokemobCap.getHungerCooldown();
    }

    @Override
    public int getHungerTime()
    {
        return pokemobCap.getHungerTime();
    }

    @Override
    public Vector3 getMobSizes()
    {
        return pokemobCap.getMobSizes();
    }

    @Override
    public int getPathTime()
    {
        return pokemobCap.getPathTime();
    }

    /** @return does this pokemon hunt for food */
    @Override
    public boolean isCarnivore()
    {
        return pokemobCap.isCarnivore();
    }

    @Override
    public boolean isElectrotroph()
    {
        return pokemobCap.isElectrotroph();
    }

    /** @return Does this pokemon eat grass */
    @Override
    public boolean isHerbivore()
    {
        return pokemobCap.isHerbivore();
    }

    @Override
    public boolean isLithotroph()
    {
        return pokemobCap.isLithotroph();
    }

    @Override
    public boolean isPhototroph()
    {
        return pokemobCap.isPhototroph();
    }

    @Override
    public boolean neverHungry()
    {
        return pokemobCap.neverHungry();
    }

    @Override
    public void noEat(Entity e)
    {
        pokemobCap.noEat(e);
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public void setHungerCooldown(int hungerCooldown)
    {
        pokemobCap.setHungerCooldown(hungerCooldown);
    }

    @Override
    public void setHungerTime(int hungerTime)
    {
        pokemobCap.setHungerTime(hungerTime);
    }

    @Override
    public boolean swims()
    {
        return pokemobCap.swims();
    }

    @Override
    public int getFlavourAmount(int index)
    {
        return pokemobCap.getFlavourAmount(index);
    }

    @Override
    public void setFlavourAmount(int index, int amount)
    {
        pokemobCap.setFlavourAmount(index, amount);
    }
}
