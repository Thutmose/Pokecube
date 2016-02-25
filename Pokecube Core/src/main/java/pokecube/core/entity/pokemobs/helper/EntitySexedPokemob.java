/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
    protected Entity               egg  = null;
    private Entity                 lover;
    protected int                  inLove;
    protected byte                 sexe = 0;
    protected Vector<IBreedingMob> males;

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
            resetInLove();
            lover = null;

            ((EntityPokemob) male).setLover(null);
            ((EntityPokemob) male).resetInLove();

            setAttackTarget(null);
            ((EntityPokemob) male).setAttackTarget(null);

            lay(childPokedexNb, (IPokemob) male);
        }
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

    public Entity findLover()
    {
        boolean transforms = false;
        for (String s : getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }

        if (getSexe() == SEXLEGENDARY && !transforms) return null;// legendaries
                                                                  // cannot mate

        if (lover != null) { return lover; }

        if ((getSexe() == MALE && !transforms) || males.size() > 0) { return null; }

        if (inLove > 0)
        {
            float searchingLoveDist = 5F;
            List<EntityPokemob> list = worldObj.getEntitiesWithinAABB(EntityPokemob.class,
                    getEntityBoundingBox().expand(searchingLoveDist, searchingLoveDist, searchingLoveDist));

            if (list.size() >= 30)
            {
                resetInLove();
                return null;
            }
            for (int i = 0; i < list.size(); i++)
            {
                EntityPokemob entityanimal = (EntityPokemob) list.get(i);
                if (entityanimal == this
                        || entityanimal.getPokemonAIState(IPokemob.TAMED) != this.getPokemonAIState(IPokemob.TAMED))
                    continue;

                boolean validMate = this.canMate(entityanimal);

                if (!validMate || getDistanceSqToEntity(entityanimal) > searchingLoveDist * searchingLoveDist) continue;

                if (!Vector3.isVisibleEntityFromEntity(this, entityanimal)
                        || entityanimal.getPokemonAIState(IPokemob.ANGRY))
                    continue;

                if (entityanimal != this && entityanimal.getHealth() > entityanimal.getMaxHealth() / 1.5f)
                {
                    if (!males.contains(entityanimal))
                    {
                        entityanimal.setLover(this);
                        if (transforms) lover = entityanimal;
                        males.add(entityanimal);
                        entityanimal.inLove = 200;
                    }
                }
            }
        }
        return null;
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
        inLove = rand.nextInt(600) - getBreedingDelay(null);
        lover = null;
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
    public void setLover(Entity lover)
    {
        this.lover = lover;
    }

    @Override
    public int getLoveTimer()
    {
        return inLove;
    }

    @Override
    public void setLoveTimer(int value)
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

    public static class MateTask
    {
        public MateTask(final EntitySexedPokemob pokemob, final IBreedingMob male)
        {
            Runnable run = new Runnable()
            {
                public void run()
                {
                    pokemob.mate(male);
                    System.out.println(Thread.currentThread());
                }
            };
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(run);
        }
    }
}
