/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.ConfigHandler;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntitySexedPokemob extends EntityStatsPokemob
{
    protected Entity               egg   = null;
    private Entity                 lover;
    protected int                  inLove;
    protected byte                 sexe  = 0;
    protected Vector<IBreedingMob> males = new Vector<>();

    /** @param par1World */
    public EntitySexedPokemob(World world)
    {
        super(world);
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        setSexe(Tools.getSexe(getPokedexEntry().getSexeRatio(), new Random()));
        resetInLove();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public void specificSpawnInit()
    {
        super.specificSpawnInit();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger(PokecubeSerializer.SEXE, getSexe());
        nbttagcompound.setInteger("InLove2", inLove);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setSexe((byte) nbttagcompound.getInteger(PokecubeSerializer.SEXE));
        inLove = nbttagcompound.getInteger("InLove2");
    }

    @Override
    public byte getSexe()
    {
        return sexe;// dataWatcher.getWatchableObjectByte(21);
    }

    @Override
    public void setSexe(byte sexe)
    {
        if (sexe == NOSEXE || sexe == FEMALE || sexe == MALE || sexe == SEXLEGENDARY)
        {
            this.sexe = sexe;// dataWatcher.updateObject(21, sexe);
        }
        else
        {
            System.err.println("Illegal argument. Sexe cannot be " + sexe);
            new Exception().printStackTrace();
        }
    }

    @Override
    public void mateWith(IBreedingMob male)
    {
        new MateTask(this, male);
    }

    protected void mate(IBreedingMob male)
    {
        if (male == null || ((Entity) male).isDead) return;
        if (this.getSexe() == MALE || male.getSexe() == FEMALE)
        {
            ((EntityPokemob) male).mateWith(this);
            return;
        }
        int childPokedexNb = (int) getChild(male);

        if (childPokedexNb > 0)
        {
            ((EntityPokemob) male).setLover(null);
            ((EntityPokemob) male).resetInLove();

            setAttackTarget(null);
            ((EntityPokemob) male).setAttackTarget(null);

            lay(childPokedexNb, (IPokemob) male);
        }
        resetInLove();
        lover = null;
    }

    public void lay(int pokedexNb, IPokemob male)
    {
        // Adding some code that prevents egg laying it too many pokemobs
        // nearby, this will prevent small caves willing with
        // a very large number of geodude and zubat.

        if (PokecubeMod.debug) System.out.println(this + " lay()");
        int i = (int) this.posX;
        int j = (int) this.posY;
        int k = (int) this.posZ;

        if (worldObj.isRemote) { return; }

        List<EntityPokemob> near = worldObj.getEntitiesWithinAABB(EntityPokemob.class,
                new AxisAlignedBB(i - 8, j - 8, k - 8, i + 8, j + 8, k + 8));

        if (near.size() >= 5)
        {
            if (PokecubeMod.debug) System.out.println("Too many pokemobs nearby, aborting Lay");
            return;
        }

        if (worldObj.isAirBlock(new BlockPos(i, j, k)))
        {
            ItemStack eggItemStack = ItemPokemobEgg.getEggStack(pokedexNb);
            Entity eggItem = new EntityPokemobEgg(worldObj, posX, posY, posZ, eggItemStack, this, male);
            EggEvent.Lay event = new EggEvent.Lay(eggItem);
            MinecraftForge.EVENT_BUS.post(event);

            if (!event.isCanceled())
            {
                egg = eggItem;
                worldObj.spawnEntityInWorld(egg);
            }
            return;
        }
    }

    @Override
    public boolean isInLove()
    {
        return inLove > 0 || lover != null;
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
    public Object getChild(IBreedingMob male)
    {

        boolean transforms = false;
        boolean otherTransforms = false;
        String movesString = dataWatcher.getWatchableObjectString(30);
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

        for (String s : ((IPokemob) male).getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
        }
        if (transforms && !otherTransforms
                && ((IPokemob) male).getTransformedTo() != this) { return ((IBreedingMob) male).getChild(male); }
        return getPokedexEntry().getChildNb(((IPokemob) male).getPokedexNb());
    }

    // @Override
    public EntityAnimal spawnBabyAnimal(EntityAnimal entityanimal)
    {
        if (entityanimal instanceof IPokemob)
        {
            lay((int) getChild((IBreedingMob) entityanimal), (IPokemob) entityanimal);
        }

        return null;
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeByte(sexe);
        super.writeSpawnData(data);
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void readSpawnData(ByteBuf data)
    {
        sexe = data.readByte();
        super.readSpawnData(data);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return this.spawnBabyAnimal((EntityAnimal) var1);
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

    public int getBreedingDelay(IPokemob mate)
    {
        return ConfigHandler.BREEDINGDELAY;
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
    /** Sets the entity to try to breed with
     * 
     * @param lover */
    public void setLover(final Entity newLover)
    {
        this.lover = newLover;
    }

    @Override
    public int getLoveTimer()
    {
        return inLove;
    }

    @Override
    public void setLoveTimer(final int value)
    {
        inLove = value;
    }

    @Override
    public Vector<IBreedingMob> getMalesForBreeding()
    {
        return males;
    }

    @Override
    public boolean tryToBreed()
    {
        return isInLove();
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();

        if (!isServerWorld()) return;

        setLoveTimer(getLoveTimer() + 1 * ConfigHandler.mateMultiplier);
        if (isInLove() && lover == null)
        {
            findLover();
        }
        boolean transforms = false;
        for (String s : getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if ((lover != null || !males.isEmpty()) && (transforms || sexe != MALE))
        {
            if (males.size() == 1 && lover == null) setLover((Entity) males.get(0));
            if (males.size() <= 1) tryFindMate();
            else initiateMateFight();
        }
    }

    public Entity findLover()
    {
        boolean transforms = false;
        for (String s : getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if (getSexe() == SEXLEGENDARY && !transforms) return null;

        if (getLover() != null) { return lover = getLover(); }

        if ((getSexe() == MALE && !transforms) || males.size() > 0) { return null; }

        if (getLoveTimer() > 0)
        {
            float searchingLoveDist = 5F;
            AxisAlignedBB bb = here.getAABB().expand(searchingLoveDist, searchingLoveDist, searchingLoveDist);
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, bb);

            if (list.size() >= 30)
            {
                resetLoveStatus();
                return null;
            }
            for (int i = 0; i < list.size(); i++)
            {
                if (!(list.get(i) instanceof IPokemob)) continue;

                IPokemob entityanimal = (IPokemob) list.get(i);
                EntityAnimal animal = (EntityAnimal) list.get(i);
                if (entityanimal == this || entityanimal.getPokemonAIState(TAMED) != getPokemonAIState(TAMED)) continue;

                boolean validMate = canMate((EntityAnimal) entityanimal);

                if (!validMate || getDistanceSqToEntity((Entity) entityanimal) > searchingLoveDist * searchingLoveDist)
                    continue;

                if (!Vector3.isVisibleEntityFromEntity(this, (Entity) entityanimal)
                        || entityanimal.getPokemonAIState(ANGRY))
                    continue;

                if (entityanimal != this && animal.getHealth() > animal.getMaxHealth() / 1.5f)
                {
                    if (!males.contains(entityanimal))
                    {
                        ((IBreedingMob) animal).setLover(this);
                        if (transforms) setLover(animal);
                        males.add((IBreedingMob) entityanimal);
                        if (entityanimal instanceof IBreedingMob)
                        {
                            ((IBreedingMob) entityanimal).setLoveTimer(200);
                        }
                    }
                }
            }
        }
        return null;
    }

    int spawnBabyDelay = 0;

    public void tryFindMate()
    {
        if (getLover() == null) return;
        if (this.getPokemonAIState(SITTING)) this.setPokemonAIState(SITTING, false);

        double dist = width * width + getLover().width * getLover().width;
        dist = Math.max(dist, 1);
        this.getNavigator().tryMoveToEntityLiving(getLover(), 1.5);
        if (lover instanceof EntityLiving)
        {
            ((EntityLiving) lover).getNavigator().tryMoveToEntityLiving(this, 1.5);
        }
        this.spawnBabyDelay++;
        if (this.spawnBabyDelay >= 50 && this.getDistanceSqToEntity(getLover()) < dist)
        {
            mate((IBreedingMob) getLover());
            this.spawnBabyDelay = 0;
        }
    }

    public void initiateMateFight()
    {
        if (getSexe() == IPokemob.MALE && getLover() != null)
        {
            Entity targetMate = getLover();
            getLookHelper().setLookPositionWithEntity(getLover(), 10.0F, getVerticalFaceSpeed());
            if (((IBreedingMob) targetMate).getMalesForBreeding().size() > 1)
            {
                IPokemob[] males = ((IBreedingMob) targetMate).getMalesForBreeding().toArray(new IPokemob[0]);
                Arrays.sort(males, new Comparator<IPokemob>()
                {
                    @Override
                    public int compare(IPokemob o1, IPokemob o2)
                    {
                        if (o2.getLevel() == o1.getLevel())
                            return (o1.getPokemonDisplayName().compareTo(o2.getPokemonDisplayName()));
                        return o2.getLevel() - o1.getLevel();
                    }
                });
                int level = males[0].getLevel();
                int n = 0;
                for (int i = 0; i < males.length; i++)
                {
                    if (males[i].getLevel() < level
                            || ((EntityAnimal) males[i]).getHealth() < ((EntityAnimal) males[i]).getMaxHealth() / 1.5f)
                    {
                        ((IBreedingMob) targetMate).getMalesForBreeding().remove(males[i]);
                        ((IBreedingMob) males[i]).resetLoveStatus();
                        n++;
                    }
                }
                if (n == 0 && ((IBreedingMob) targetMate).getMalesForBreeding().size() > 1)
                {
                    ((IBreedingMob) ((IBreedingMob) targetMate).getMalesForBreeding().get(0)).resetLoveStatus();
                    ((IBreedingMob) ((IBreedingMob) targetMate).getMalesForBreeding().get(1)).resetLoveStatus();
                    ((IPokemob) ((IBreedingMob) targetMate).getMalesForBreeding().get(0))
                            .setPokemonAIState(IPokemob.MATEFIGHT, true);
                    ((IPokemob) ((IBreedingMob) targetMate).getMalesForBreeding().get(1))
                            .setPokemonAIState(IPokemob.MATEFIGHT, true);
                    ((EntityAnimal) ((IBreedingMob) targetMate).getMalesForBreeding().get(0))
                            .setAttackTarget(((EntityAnimal) ((IBreedingMob) targetMate).getMalesForBreeding().get(1)));
                }

            }

            if (((IBreedingMob) targetMate).getMalesForBreeding().size() > 1) return;
            else if (((IBreedingMob) targetMate).getMalesForBreeding().size() == 0)
            {
                ((IBreedingMob) targetMate).resetLoveStatus();
                resetLoveStatus();
            }
        }
        else if (getMalesForBreeding().size() == 1)
        {
            IBreedingMob lover = getMalesForBreeding().get(0);
            setLover((Entity) lover);
            lover.setLover(this);
        }
    }

    public static class MateTask
    {
        public MateTask(final EntitySexedPokemob pokemob, final IBreedingMob male)
        {
            Runnable run = new Runnable()
            {
                public void run()
                {
                    pokemob.mate(male);
                }
            };
            PokecubeCore.proxy.getMainThreadListener().addScheduledTask(run);
        }
    }
}
