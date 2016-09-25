package pokecube.core.ai.thread.aiRunnables;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

public class AIMate extends AIBase
{
    IBreedingMob breedingMob;
    IPokemob     pokemob;
    EntityAnimal entity;
    int          cooldown       = 0;
    int          spawnBabyDelay = 0;

    public AIMate(EntityAnimal par1EntityAnimal)
    {
        breedingMob = (IBreedingMob) par1EntityAnimal;
        pokemob = (IPokemob) breedingMob;
        entity = (EntityAnimal) pokemob;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        if (breedingMob.getSexe() != IPokemob.MALE)
        {
            int diff = 1 * PokecubeMod.core.getConfig().mateMultiplier;
            if (breedingMob.getLoveTimer() > 0) diff = 1;
            breedingMob.setLoveTimer(breedingMob.getLoveTimer() + diff);
        }
        if (pokemob.getPokemonAIState(IMoveConstants.MATING) && (breedingMob.getLover() == null
                || breedingMob.getLover().isDead || ((IBreedingMob) breedingMob.getLover()).getLover() != breedingMob))
        {
            pokemob.setPokemonAIState(IMoveConstants.MATING, false);
        }
        if (cooldown-- > 0) { return; }
        super.doMainThreadTick(world);
        if (entity.isInLove() && breedingMob.getLover() == null)
        {
            findLover();
        }
        boolean transforms = false;
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if ((breedingMob.getLover() != null || !breedingMob.getMalesForBreeding().isEmpty())
                && (transforms || pokemob.getSexe() != IPokemob.MALE))
        {
            if (breedingMob.getMalesForBreeding().size() == 1 && breedingMob.getLover() == null)
                breedingMob.setLover((Entity) breedingMob.getMalesForBreeding().get(0));
            if (breedingMob.getMalesForBreeding().size() <= 1) tryFindMate();
            else initiateMateFight();
        }
        if (!breedingMob.tryToBreed())
        {
            cooldown = 20;
        }
    }

    public void initiateMateFight()
    {
        if (pokemob.getSexe() == IPokemob.MALE && breedingMob.getLover() != null)
        {
            Entity targetMate = breedingMob.getLover();
            entity.getLookHelper().setLookPositionWithEntity(breedingMob.getLover(), 10.0F,
                    entity.getVerticalFaceSpeed());
            if (((IBreedingMob) targetMate).getMalesForBreeding().size() > 1)
            {
                IPokemob[] males = ((IBreedingMob) targetMate).getMalesForBreeding().toArray(new IPokemob[0]);
                Arrays.sort(males, new Comparator<IPokemob>()
                {
                    @Override
                    public int compare(IPokemob o1, IPokemob o2)
                    {
                        if (o2.getLevel() == o1.getLevel()) return (o1.getPokemonDisplayName().getFormattedText()
                                .compareTo(o2.getPokemonDisplayName().getFormattedText()));
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
                    ((IBreedingMob) targetMate).getMalesForBreeding().get(0).resetLoveStatus();
                    ((IBreedingMob) targetMate).getMalesForBreeding().get(1).resetLoveStatus();
                    ((IPokemob) ((IBreedingMob) targetMate).getMalesForBreeding().get(0))
                            .setPokemonAIState(IMoveConstants.MATEFIGHT, true);
                    ((IPokemob) ((IBreedingMob) targetMate).getMalesForBreeding().get(1))
                            .setPokemonAIState(IMoveConstants.MATEFIGHT, true);
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
    }

    public Entity findLover()
    {
        boolean transforms = false;
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if (!pokemob.getPokedexEntry().breeds && !transforms) return null;
        if (pokemob.isType(PokeType.ghost) && !pokemob.getPokemonAIState(IMoveConstants.TAMED)) return null;
        if (!(pokemob.getPokemonOwner() instanceof EntityPlayer)
                && !Tools.isAnyPlayerInRange(PokecubeMod.core.getConfig().maxSpawnRadius,
                        PokecubeMod.core.getConfig().maxSpawnRadius / 4, entity))
            return null;
        if (breedingMob.getLover() != null) { return breedingMob.getLover(); }

        if ((pokemob.getSexe() == IPokemob.MALE && !transforms)
                || breedingMob.getMalesForBreeding().size() > 0) { return null; }

        if (breedingMob.getLoveTimer() > 0)
        {
            float searchingLoveDist = 5F;
            AxisAlignedBB bb = entity.getEntityBoundingBox().expand(searchingLoveDist, searchingLoveDist,
                    searchingLoveDist);
            List<Entity> list = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    return input instanceof IPokemob && input instanceof EntityAnimal && input instanceof IBreedingMob;
                }
            });
            bb = entity.getEntityBoundingBox().expand(PokecubeMod.core.getConfig().maxSpawnRadius,
                    2 * searchingLoveDist, PokecubeMod.core.getConfig().maxSpawnRadius);
            List<Entity> list2 = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    return input instanceof IPokemob && input instanceof EntityAnimal && input instanceof IBreedingMob;
                }
            });
            float multiplier = pokemob.isPlayerOwned() ? 3 : 2;
            if (list2.size() >= PokecubeMod.core.getConfig().mobSpawnNumber * multiplier)
            {
                breedingMob.resetLoveStatus();
                return null;
            }
            for (int i = 0; i < list.size(); i++)
            {
                IPokemob entityanimal = (IPokemob) list.get(i);
                EntityAnimal animal = (EntityAnimal) list.get(i);
                if (entityanimal == this || entityanimal.getPokemonAIState(IMoveConstants.TAMED) != pokemob
                        .getPokemonAIState(IMoveConstants.TAMED) || !entityanimal.getPokedexEntry().breeds)
                    continue;

                boolean validMate = breedingMob.canMate((EntityAnimal) entityanimal);

                if (!validMate
                        || entity.getDistanceSqToEntity((Entity) entityanimal) > searchingLoveDist * searchingLoveDist)
                    continue;

                if (!Vector3.isVisibleEntityFromEntity(entity, (Entity) entityanimal)
                        || entityanimal.getPokemonAIState(IMoveConstants.ANGRY))
                    continue;

                if (entityanimal != this && animal.getHealth() > animal.getMaxHealth() / 1.5f)
                {
                    if (!breedingMob.getMalesForBreeding().contains(entityanimal))
                    {
                        ((IBreedingMob) animal).setLover(entity);
                        if (transforms) breedingMob.setLover(animal);
                        breedingMob.getMalesForBreeding().add((IBreedingMob) entityanimal);
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

    @Override
    public void reset()
    {
        if (cooldown > 0) return;
        cooldown = 20;
    }

    @Override
    public void run()
    {
    }

    @Override
    public boolean shouldRun()
    {
        if (cooldown > 0) { return false; }
        if (breedingMob.getLover() != null) if (breedingMob.tryToBreed() && !breedingMob.getLover().isDead) return true;
        if (pokemob.getPokemonAIState(IMoveConstants.MATING)) return true;
        if (breedingMob.getLover() != null) { return true; }
        if (pokemob.getSexe() == IPokemob.MALE || !breedingMob.tryToBreed()) return false;
        if (pokemob.getPokemonAIState(IMoveConstants.ANGRY) || entity.getAttackTarget() != null) return false;
        return true;
    }

    public void tryFindMate()
    {
        if (breedingMob.getLover() == null) return;
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING)) pokemob.setPokemonAIState(IMoveConstants.SITTING, false);

        double dist = entity.width * entity.width + breedingMob.getLover().width * breedingMob.getLover().width;
        dist = Math.max(dist, 1);
        entity.getNavigator().tryMoveToEntityLiving(breedingMob.getLover(), 1.5);
        spawnBabyDelay++;
        pokemob.setPokemonAIState(IMoveConstants.MATING, true);
        if (breedingMob.getLover() instanceof IPokemob)
        {
            ((IPokemob) breedingMob.getLover()).setPokemonAIState(IMoveConstants.MATING, true);
        }
        if (breedingMob.getLover() instanceof EntityLiving)
        {
            ((EntityLiving) breedingMob.getLover()).getNavigator().tryMoveToEntityLiving(entity,
                    pokemob.getMovementSpeed());
        }
        if (this.spawnBabyDelay >= 50)
        {
            if (breedingMob.getLover() instanceof IPokemob)
                ((IPokemob) breedingMob.getLover()).setPokemonAIState(IMoveConstants.MATING, false);
            breedingMob.mateWith((IBreedingMob) breedingMob.getLover());
            pokemob.setPokemonAIState(IMoveConstants.MATING, false);
            this.spawnBabyDelay = 0;
        }
    }
}
