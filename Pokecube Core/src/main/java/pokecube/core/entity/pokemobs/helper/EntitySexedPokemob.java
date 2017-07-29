/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.Tools;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntitySexedPokemob extends EntityStatsPokemob
{
    protected Entity               egg   = null;
    private Entity                 lover;
    protected int                  loveTimer;
    protected Vector<IBreedingMob> males = new Vector<>();

    /** @param par1World */
    public EntitySexedPokemob(World world)
    {
        super(world);
    }

    @Override
    public boolean canMate(EntityAnimal entityAnimal)
    {
        return pokemobCap.canMate(entityAnimal);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return this.spawnBabyAnimal((EntityAnimal) var1);
    }

    @Override
    public Object getChild(IBreedingMob male)
    {
        return pokemobCap.getChild(male);
    }

    @Override
    /** Which entity is this pokemob trying to breed with
     * 
     * @return */
    public Entity getLover()
    {
        return pokemobCap.getLover();
    }

    @Override
    public int getLoveTimer()
    {
        return pokemobCap.getLoveTimer();
    }

    @Override
    public Vector<IBreedingMob> getMalesForBreeding()
    {
        return pokemobCap.getMalesForBreeding();
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        resetInLove();
    }

    @Override
    public boolean isInLove()
    {
        return loveTimer > 0 || lover != null;
    }

    public void lay(IPokemob male)
    {
        if (PokecubeMod.debug) PokecubeMod.log(this + " lay()");
        if (worldObj.isRemote) { return; }
        int num = Tools.countPokemon(worldObj, here, PokecubeMod.core.getConfig().maxSpawnRadius);
        if (!(getOwner() instanceof EntityPlayer) && num > PokecubeMod.core.getConfig().mobSpawnNumber * 1.25) return;
        Vector3 pos = Vector3.getNewVector().set(this).addTo(0, Math.max(this.height / 4, 0.5f), 0);
        if (pos.isClearOfBlocks(getEntityWorld()))
        {
            Entity eggItem = null;
            try
            {
                eggItem = new EntityPokemobEgg(worldObj, posX, posY, posZ, this, male);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            EggEvent.Lay event;
            try
            {
                event = new EggEvent.Lay(eggItem);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled())
                {
                    egg = eggItem;
                    worldObj.spawnEntityInWorld(egg);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return;
        }
    }

    protected void mate(IBreedingMob male)
    {
        if (male == null || ((Entity) male).isDead) return;
        if (this.getSexe() == MALE || male.getSexe() == FEMALE && male != this)
        {
            ((EntityPokemob) male).mateWith(this);
            return;
        }
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 2;
        if (male instanceof IHungrymob)
        {
            IHungrymob hungry = (IHungrymob) male;
            hungry.setHungerTime(hungry.getHungerTime() + hungerValue);
        }
        setHungerTime(getHungerTime() + hungerValue);
        ((EntityPokemob) male).setLover(null);
        ((EntityPokemob) male).resetInLove();
        setAttackTarget(null);
        ((EntityPokemob) male).setAttackTarget(null);
        lay((IPokemob) male);
        resetInLove();
        lover = null;
    }

    @Override
    public void mateWith(final IBreedingMob male)
    {
        pokemobCap.mateWith(male);
    }

    @Override
    public void resetInLove()
    {
        resetLoveStatus();
    }

    @Override
    public void resetLoveStatus()
    {
        super.resetInLove();
        pokemobCap.resetLoveStatus();
    }

    @Override
    /** Sets the entity to try to breed with
     * 
     * @param lover */
    public void setLover(final Entity newLover)
    {
        pokemobCap.setLover(newLover);
    }

    @Override
    public void setLoveTimer(final int value)
    {
        pokemobCap.setLoveTimer(value);
    }

    // @Override
    public EntityAnimal spawnBabyAnimal(EntityAnimal entityanimal)
    {
        if (entityanimal instanceof IPokemob)
        {
            lay((IPokemob) entityanimal);
        }
        return null;
    }

    @Override
    public boolean tryToBreed()
    {
        return pokemobCap.tryToBreed();
    }
}
