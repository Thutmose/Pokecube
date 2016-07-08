package pokecube.core.entity.pokemobs.helper;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public abstract class EntityHungryPokemob extends EntityAiPokemob
{

    Vector3       sizes          = Vector3.getNewVector();

    protected int hungerCooldown = 0;

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
        ;

        if (attacker instanceof EntityPlayer && !worldObj.isRemote)
        {
            damage *= 2;
        }

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
        vBak.set(vec);
        if (e instanceof EntityItem)
        {
            HappinessType.applyHappiness(this, HappinessType.EVBERRY);
            // Make wild pokemon level up naturally to their cap, to allow wild
            // hatches
            if (!getPokemonAIState(IMoveConstants.TAMED))
            {
                int exp = SpawnHandler.getSpawnXp(worldObj, v1.set(this), getPokedexEntry());
                if (getExp() < exp)
                {
                    int n = new Random().nextInt(exp) / 3 + 1;
                    setExp(getExp() + n, false, false);
                }
            }
        }
        vec.set(vBak);

        setHungerTime(0);
        hungerCooldown = 0;

        setPokemonAIState(HUNTING, false);

        if (this.isDead) return;

        float missingHp = this.getMaxHealth() - this.getHealth();
        float toHeal = this.getHealth() + Math.max(1, missingHp * 0.25f);
        this.setHealth(Math.min(toHeal, getMaxHealth()));
    }

    @Override
    public boolean eatsBerries()
    {
        return getPokedexEntry().foods[5];
    }

    @Override
    public boolean filterFeeder()
    {
        return getPokedexEntry().foods[6];
    }

    @Override
    public boolean floats()
    {
        return getPokedexEntry().floats();
    }

    @Override
    public boolean flys()
    {
        return getPokedexEntry().flys();
    }

    @Override
    public float getBlockPathWeight(IBlockAccess world, Vector3 location)
    {
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        boolean water = getPokedexEntry().swims();
        boolean air = getPokedexEntry().flys() || getPokedexEntry().floats();
        if (getPokedexEntry().hatedMaterial != null)
        {
            String material = getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("water") && state.getMaterial() == Material.WATER) { return 100; }
        }
        if (state.getMaterial() == Material.WATER) return water ? 1 : air ? 100 : 40;
        if (block == Blocks.GRAVEL) return water ? 40 : 5;
        if (!this.isImmuneToFire() && (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.FIRE))
            return 1;
        return water ? 40 : 20;
    }

    @Override
    public double getFloatHeight()
    {
        return getPokedexEntry().preferedHeight;
    }

    @Override
    public int getHungerCooldown()
    {
        return hungerCooldown;
    }

    @Override
    public int getHungerTime()
    {
        return getDataManager().get(HUNGERDW);
    }

    @Override
    public Vector3 getMobSizes()
    {
        return sizes.set(getPokedexEntry().width, getPokedexEntry().height, getPokedexEntry().length)
                .scalarMult(getSize());
    }

    @Override
    public int getPathTime()
    {
        int time = 2500000;
        if (getPokemonAIState(IMoveConstants.TAMED)) time *= 3;
        return time;
    }

    /** @return does this pokemon hunt for food */
    @Override
    public boolean isCarnivore()
    {
        return this.getPokedexEntry().hasPrey();
    }

    @Override
    public boolean isElectrotroph()
    {
        return getPokedexEntry().foods[2];
    }

    /** @return Does this pokemon eat grass */
    @Override
    public boolean isHerbivore()
    {
        return getPokedexEntry().foods[3];
    }

    @Override
    public boolean isLithotroph()
    {
        return getPokedexEntry().foods[1];
    }

    @Override
    public boolean isPhototroph()
    {
        return getPokedexEntry().foods[0];
    }

    @Override
    public boolean neverHungry()
    {
        return getPokedexEntry().foods[4];
    }

    @Override
    public void noEat(Entity e)
    {
        vBak.set(vec);
        if (e != null)
        {
            addHappiness(-10);
        }
        vec.set(vBak);
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
        this.hungerCooldown = hungerCooldown;
    }

    @Override
    public void setHungerTime(int hungerTime)
    {
        getDataManager().set(HUNGERDW, hungerTime);
    }

    @Override
    public boolean swims()
    {
        return getPokedexEntry().swims();
    }
}
