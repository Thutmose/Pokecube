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
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MovesUtils;
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
        if (entityAnimal instanceof IPokemob)
        {
            PokedexEntry thisEntry = getPokedexEntry();
            PokedexEntry thatEntry = ((IPokemob) entityAnimal).getPokedexEntry();

            boolean transforms = false;
            boolean otherTransforms = false;
            for (String s : getMoves())
            {
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
            }
            for (String s : ((IPokemob) entityAnimal).getMoves())
            {
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
            }

            // can't breed two transformers
            if (transforms && otherTransforms) return false;
            else if (transforms || otherTransforms) // Anything else will mate
                                                    // with ditto
                return true;

            return thisEntry.areRelated(thatEntry) && ((IPokemob) entityAnimal).getSexe() != this.getSexe();

        }

        return false;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return this.spawnBabyAnimal((EntityAnimal) var1);
    }

    public int getBreedingDelay(IPokemob mate)
    {
        return PokecubeMod.core.getConfig().breedingDelay;
    }

    @Override
    public Object getChild(IBreedingMob male)
    {
        boolean transforms = false;
        boolean otherTransforms = ((IPokemob) male).getTransformedTo() != null;
        String movesString = dataManager.get(MOVESDW);
        String[] moves = new String[5];

        if (movesString != null && movesString.length() > 2)
        {
            String[] movesSplit = movesString.split(",");
            for (int i = 0; i < Math.min(5, movesSplit.length); i++)
            {
                String move = movesSplit[i];

                if (move != null && move.length() > 1 && MovesUtils.isMoveImplemented(move))
                {
                    moves[i] = move;
                }
            }
        }
        for (String s : moves)
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if (!otherTransforms) for (String s : ((IPokemob) male).getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
        }
        if (transforms && !otherTransforms
                && ((IPokemob) male).getTransformedTo() != this) { return male.getChild(this); }
        return getPokedexEntry().getChild(((IPokemob) male).getPokedexEntry());
    }

    @Override
    /** Which entity is this pokemob trying to breed with
     * 
     * @return */
    public Entity getLover()
    {
        return lover;
    }

    @Override
    public int getLoveTimer()
    {
        return loveTimer;
    }

    @Override
    public Vector<IBreedingMob> getMalesForBreeding()
    {
        return males;
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
        if (PokecubeMod.debug) System.out.println(this + " lay()");
        if (worldObj.isRemote) { return; }
        int num = Tools.countPokemon(worldObj, here, PokecubeMod.core.getConfig().maxSpawnRadius);
        if (!(getOwner() instanceof EntityPlayer) && num > PokecubeMod.core.getConfig().mobSpawnNumber * 1.25) return;
        Vector3 pos = Vector3.getNewVector().set(this);
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
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                mate(male);
            }
        });
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
        setLoveTimer(rand.nextInt(600) - getBreedingDelay(null));
        setLover(null);
        setPokemonAIState(MATING, false);
        setPokemonAIState(ANGRY, false);
        if (males != null) males.clear();
    }

    @Override
    /** Sets the entity to try to breed with
     * 
     * @param lover */
    public void setLover(final Entity newLover)
    {
        this.lover = newLover;
    }

    @Override
    public void setLoveTimer(final int value)
    {
        loveTimer = value;
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
        return isInLove();
    }
}
