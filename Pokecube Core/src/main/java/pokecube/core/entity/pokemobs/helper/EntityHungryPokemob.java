package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public abstract class EntityHungryPokemob extends EntityAiPokemob
{

    public static int HUNGERDELAY = 6000;
    boolean           sleepy      = false;
    Vector3           sizes       = Vector3.getNewVector();

    protected int hungerCooldown = 0;

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
        Block block = location.getBlock(world);
        boolean water = getPokedexEntry().swims();
        boolean air = getPokedexEntry().flys() || getPokedexEntry().floats();

        if (getPokedexEntry().hatedMaterial != null)
        {
            String material = getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("water") && block.getMaterial() == Material.water) { return 100; }
        }

        if (block.getMaterial() == Material.water) return water ? 1 : air ? 100 : 40;
        if (block == Blocks.gravel) return water ? 40 : 5;

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
        return getDataManager().getWatchableObjectInt(HUNGERDW);
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
        if (getPokemonAIState(TAMED)) time *= 3;
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

    // 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999
    public boolean isGoodSleepingSpot(ChunkCoordinate c)
    {
        float light = this.getBrightness(0);
        List<TimePeriod> active = getPokedexEntry().activeTimes();
        if (this.hasHome() && this.getPosition().distanceSq(getHome()) > 10) return false;
        
        //TODO refine timing
        for (TimePeriod p : active)
        {
            if (p.contains(18000)) { return light < 0.1; }
        }

        return true;
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
        int hungerTime = getHungerTime();
        sleepy = true;
        for (TimePeriod p : getPokedexEntry().activeTimes())
        {
            if (p != null && p.contains(worldObj.getWorldTime()))
            {
                sleepy = false;
                break;
            }
        }
        Vector3 v = here.set(this);
        ChunkCoordinate c = new ChunkCoordinate(v, dimension);
        if (!this.neverHungry() && hungerCooldown < 0)
        {
            if (hungerTime > HUNGERDELAY + getRNG().nextInt((int) (0.5 * HUNGERDELAY)) && !getPokemonAIState(HUNTING))
            {
                this.setPokemonAIState(HUNTING, true);
            }
        }

        double hurtTime = HUNGERDELAY * 1.5 + getRNG().nextInt((int) (0.5 * HUNGERDELAY));
        if (hungerTime > hurtTime && !worldObj.isRemote && this.getAttackTarget() == null && !this.neverHungry()
                && ticksExisted % 100 == 0)
        {
            this.setHealth(getHealth() - getMaxHealth() * 0.05f);
        }
        boolean ownedSleepCheck = getPokemonAIState(IMoveConstants.TAMED)
                && !(getPokemonAIState((byte) (STAYING)) || getPokemonAIState((byte) (SITTING)));
        if (sleepy && hungerTime < 0.85 * PokecubeMod.core.getConfig().pokemobLifeSpan)
        {
            if (!isGoodSleepingSpot(c))
            {

            }
            else if (getAttackTarget() == null && !ownedSleepCheck && getNavigator().noPath())
            {
                setPokemonAIState(SLEEPING, true);
                setPokemonAIState(HUNTING, false);
            }
            else if (!getNavigator().noPath() || getAttackTarget() != null)
            {
                setPokemonAIState(SLEEPING, false);
            }
        }
        else if (!getPokemonAIState(TIRED))
        {
            setPokemonAIState(SLEEPING, false);
        }

        if (this.getAttackTarget() == null && !this.isDead && ticksExisted % 100 == 0 && !worldObj.isRemote
                && hungerCooldown < 0)
        {
            float dh = Math.max(1, getMaxHealth() * 0.05f);

            float toHeal = this.getHealth() + dh;
            this.setHealth(Math.min(toHeal, getMaxHealth()));
        }
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
        getDataManager().updateObject(HUNGERDW, hungerTime);
    }

    @Override
    public boolean swims()
    {
        return getPokedexEntry().swims();
    }
}
