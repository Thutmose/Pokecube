package pokecube.core.ai.thread.aiRunnables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import pokecube.core.handlers.ConfigHandler;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIMate extends AIBase
{
    IBreedingMob                breedingMob;
    IPokemob                    pokemob;
    EntityAnimal                entity;
    public Vector<IBreedingMob> males = new Vector<IBreedingMob>();

    /** Delay preventing a baby from spawning immediately when two mate-able
     * animals find each other. */
    int spawnBabyDelay;

    public AIMate(EntityAnimal par1EntityAnimal)
    {
        breedingMob = (IBreedingMob) par1EntityAnimal;
        pokemob = (IPokemob) breedingMob;
        entity = (EntityAnimal) pokemob;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null) return false;

        if (breedingMob.getLover() != null)
            if (this.spawnBabyDelay < 60 && breedingMob.tryToBreed() && !breedingMob.getLover().isDead) return true;

        boolean isMating = pokemob.getPokemonAIState(IPokemob.MATING);
        if ((breedingMob.getLover() != null && breedingMob.getLover() instanceof IBreedingMob
                && !((IBreedingMob) breedingMob.getLover()).tryToBreed()) || (isMating && males.isEmpty()))
            breedingMob.resetLoveStatus();

        if (isMating) return true;
        if (breedingMob.getLover() != null)
        {
            pokemob.setPokemonAIState(IPokemob.MATING, true);
            return true;
        }
        findLover();
        if (pokemob.getSexe() == IPokemob.MALE || !breedingMob.tryToBreed()) return false;

        if (males.size() == 0) return false;

        ArrayList<IBreedingMob> toRemove = new ArrayList<IBreedingMob>();
        for (IBreedingMob m : males)
        {
            if (((Entity) m).isDead)
            {
                toRemove.add(m);
            }
        }
        males.removeAll(toRemove);
        if (males.isEmpty())
        {
            breedingMob.resetLoveStatus();
            return false;
        }
        pokemob.setPokemonAIState(IPokemob.MATING, true);
        return true;
    }

    @Override
    public void run()
    {
        boolean rePath = true;

        if (pokemob.getSexe() == IPokemob.MALE && breedingMob.getLover() != null)
        {
            Entity targetMate = breedingMob.getLover();
            ((EntityLiving) entity).getLookHelper().setLookPositionWithEntity(breedingMob.getLover(), 10.0F,
                    entity.getVerticalFaceSpeed());
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
                breedingMob.resetLoveStatus();
            }
        }
        else if (breedingMob.getMalesForBreeding().size() == 1)
        {
            IBreedingMob lover = breedingMob.getMalesForBreeding().get(0);
            breedingMob.setLover((Entity) lover);
            lover.setLover(entity);
        }

        if (breedingMob.getLover() == null) return;

        if (entity.getNavigator().getPath() != null)
        {
            Vector3 temp = Vector3.getNewVector();
            Vector3 temp1 = Vector3.getNewVector().set(breedingMob.getLover());
            temp.set(entity.getNavigator().getPath().getFinalPathPoint());
            if (temp.distToSq(temp1) < 4) rePath = false;
        }
        if (rePath) this.entity.getNavigator().tryMoveToEntityLiving(breedingMob.getLover(), entity.getAIMoveSpeed());

        this.spawnBabyDelay++;

        if (this.spawnBabyDelay >= 60 && this.entity.getDistanceSqToEntity(breedingMob.getLover()) < 2.0D)
        {
            breedingMob.mateWith((IBreedingMob) breedingMob.getLover());
            this.spawnBabyDelay = 0;
        }
        if (this.spawnBabyDelay > 200)
        {
            breedingMob.resetLoveStatus();
            this.spawnBabyDelay = 0;
        }
    }

    @Override
    public void reset()
    {
    }

    public Entity findLover()
    {
        boolean transforms = false;
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }

        if (pokemob.getSexe() == IPokemob.SEXLEGENDARY && !transforms) return null;

        if (breedingMob.getLover() != null) { return breedingMob.getLover(); }

        if ((pokemob.getSexe() == IPokemob.MALE && !transforms) || males.size() > 0) { return null; }

        breedingMob.setLoveTimer(breedingMob.getLoveTimer() + 1 * ConfigHandler.mateMultiplier);

        if (breedingMob.getLoveTimer() > 0)
        {
            float searchingLoveDist = 5F;
            List<Object> list = getEntitiesWithinDistance(entity, searchingLoveDist, IBreedingMob.class, IPokemob.class,
                    EntityAnimal.class);

            if (list.size() >= 30)
            {
                breedingMob.resetLoveStatus();
                return null;
            }
            for (int i = 0; i < list.size(); i++)
            {
                IPokemob entityanimal = (IPokemob) list.get(i);
                EntityAnimal animal = (EntityAnimal) list.get(i);
                if (entityanimal == breedingMob
                        || entityanimal.getPokemonAIState(IPokemob.TAMED) != pokemob.getPokemonAIState(IPokemob.TAMED))
                    continue;

                boolean validMate = breedingMob.canMate((EntityAnimal) entityanimal);

                if (!validMate
                        || entity.getDistanceSqToEntity((Entity) entityanimal) > searchingLoveDist * searchingLoveDist)
                    continue;

                if (!Vector3.isVisibleEntityFromEntity(entity, (Entity) entityanimal)
                        || entityanimal.getPokemonAIState(IPokemob.ANGRY))
                    continue;

                if (entityanimal != pokemob && animal.getHealth() > animal.getMaxHealth() / 1.5f)
                {
                    if (!males.contains(entityanimal))
                    {
                        ((IBreedingMob) animal).setLover(entity);
                        if (transforms) breedingMob.setLover(animal);
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
}
