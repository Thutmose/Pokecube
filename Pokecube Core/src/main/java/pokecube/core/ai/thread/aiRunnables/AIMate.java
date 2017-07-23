package pokecube.core.ai.thread.aiRunnables;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

/** This IAIRunnable is responsible for most of the breeding AI for the
 * pokemobs. It finds the mates, initiates the fighting over a mate (if
 * applicable), then tells the mobs to breed if they should. */
public class AIMate extends AIBase
{
    final IPokemob     pokemob;
    final EntityAnimal entity;
    int                cooldown       = 0;
    int                spawnBabyDelay = 0;

    public AIMate(EntityAnimal par1EntityAnimal)
    {
        entity = par1EntityAnimal;
        pokemob = CapabilityPokemob.getPokemobFor(par1EntityAnimal);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        if (pokemob.getSexe() != IPokemob.MALE)
        {
            int diff = 1 * PokecubeMod.core.getConfig().mateMultiplier;
            if (pokemob.getLoveTimer() > 0) diff = 1;
            pokemob.setLoveTimer(pokemob.getLoveTimer() + diff);
        }
        IPokemob loverMob = CapabilityPokemob.getPokemobFor(pokemob.getLover());
        if (pokemob.getPokemonAIState(IMoveConstants.MATING)
                && (pokemob.getLover() == null || pokemob.getLover().isDead || loverMob != pokemob))
        {
            pokemob.setPokemonAIState(IMoveConstants.MATING, false);
        }
        if (cooldown-- > 0) { return; }
        super.doMainThreadTick(world);
        if (entity.isInLove() && pokemob.getLover() == null)
        {
            findLover();
        }
        boolean transforms = false;
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if (transforms && pokemob.getLover() != null)
        {
            pokemob.setTransformedTo(pokemob.getLover());
        }
        if ((pokemob.getLover() != null || !pokemob.getMalesForBreeding().isEmpty())
                && (transforms || pokemob.getSexe() != IPokemob.MALE))
        {
            if (pokemob.getMalesForBreeding().size() == 1 && pokemob.getLover() == null)
                pokemob.setLover((Entity) pokemob.getMalesForBreeding().get(0));
            if (pokemob.getMalesForBreeding().size() <= 1) tryFindMate();
            else initiateMateFight();
        }
        if (!pokemob.tryToBreed())
        {
            cooldown = 20;
        }
    }

    public void initiateMateFight()
    {
        if (pokemob.getSexe() == IPokemob.MALE && pokemob.getLover() != null)
        {
            IPokemob loverMob = CapabilityPokemob.getPokemobFor(pokemob.getLover());
            entity.getLookHelper().setLookPositionWithEntity(pokemob.getLover(), 10.0F, entity.getVerticalFaceSpeed());
            if (loverMob.getMalesForBreeding().size() > 1)
            {
                IPokemob[] males = loverMob.getMalesForBreeding().toArray(new IPokemob[0]);
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
                    IPokemob mob = males[i];
                    if (mob.getLevel() < level || mob.getEntity().getHealth() < mob.getEntity().getMaxHealth() / 1.5f)
                    {
                        loverMob.getMalesForBreeding().remove(mob);
                        mob.resetLoveStatus();
                        n++;
                    }
                }
                if (n == 0 && loverMob.getMalesForBreeding().size() > 1)
                {

                    IPokemob mob0 = (IPokemob) loverMob.getMalesForBreeding().get(0);
                    IPokemob mob1 = (IPokemob) loverMob.getMalesForBreeding().get(1);
                    mob0.resetLoveStatus();
                    mob1.resetLoveStatus();
                    mob0.setPokemonAIState(IMoveConstants.MATEFIGHT, true);
                    mob1.setPokemonAIState(IMoveConstants.MATEFIGHT, true);
                    mob0.getEntity().setAttackTarget(mob1.getEntity());
                }
            }

            if (loverMob.getMalesForBreeding().size() > 1) return;
            else if (loverMob.getMalesForBreeding().size() == 0)
            {
                loverMob.resetLoveStatus();
                pokemob.resetLoveStatus();
            }
        }
        else if (pokemob.getMalesForBreeding().size() == 1)
        {
            IBreedingMob loverMob = pokemob.getMalesForBreeding().get(0);
            pokemob.setLover(loverMob.getLover());
            loverMob.setLover(entity);
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
        if (pokemob.isType(PokeType.getType("ghost")) && !pokemob.getPokemonAIState(IMoveConstants.TAMED)) return null;
        if (!(pokemob.getPokemonOwner() instanceof EntityPlayer)
                && !Tools.isAnyPlayerInRange(PokecubeMod.core.getConfig().maxSpawnRadius,
                        PokecubeMod.core.getConfig().maxSpawnRadius / 4, entity))
            return null;
        if (pokemob.getLover() != null) { return pokemob.getLover(); }
        if ((pokemob.getSexe() == IPokemob.MALE && !transforms)
                || pokemob.getMalesForBreeding().size() > 0) { return null; }

        if (pokemob.getLoveTimer() > 0)
        {
            float searchingLoveDist = 5F;
            AxisAlignedBB bb = entity.getEntityBoundingBox().expand(searchingLoveDist, searchingLoveDist,
                    searchingLoveDist);// grow in 1.12
            List<Entity> list = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    return input instanceof EntityAnimal && CapabilityPokemob.getPokemobFor(input) != null;
                }
            });
            bb = entity.getEntityBoundingBox().expand(PokecubeMod.core.getConfig().maxSpawnRadius,
                    2 * searchingLoveDist, PokecubeMod.core.getConfig().maxSpawnRadius);// grow
                                                                                        // in
                                                                                        // 1.12
            List<Entity> list2 = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    return input instanceof EntityAnimal && CapabilityPokemob.getPokemobFor(input) != null;
                }
            });
            float multiplier = pokemob.isPlayerOwned() ? 3 : 2;
            if (list2.size() >= PokecubeMod.core.getConfig().mobSpawnNumber * multiplier)
            {
                pokemob.resetLoveStatus();
                return null;
            }
            for (int i = 0; i < list.size(); i++)
            {
                IPokemob entityanimal = CapabilityPokemob.getPokemobFor(list.get(i));
                EntityAnimal animal = (EntityAnimal) list.get(i);
                if (entityanimal == this || entityanimal.getPokemonAIState(IMoveConstants.TAMED) != pokemob
                        .getPokemonAIState(IMoveConstants.TAMED) || !entityanimal.getPokedexEntry().breeds)
                    continue;

                boolean otherTransforms = false;
                for (String s : entityanimal.getMoves())
                {
                    if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
                }

                if (transforms && otherTransforms) continue;

                boolean validMate = pokemob.canMate((EntityAnimal) entityanimal);

                if (!validMate
                        || entity.getDistanceSqToEntity((Entity) entityanimal) > searchingLoveDist * searchingLoveDist)
                    continue;

                if (!Vector3.isVisibleEntityFromEntity(entity, (Entity) entityanimal)
                        || entityanimal.getPokemonAIState(IMoveConstants.ANGRY))
                    continue;

                if (entityanimal != this && animal.getHealth() > animal.getMaxHealth() / 1.5f)
                {
                    if (!pokemob.getMalesForBreeding().contains(entityanimal))
                    {
                        entityanimal.setLover(entity);
                        if (transforms) pokemob.setLover(animal);
                        pokemob.getMalesForBreeding().add(entityanimal);
                        entityanimal.setLoveTimer(200);
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
        if (pokemob.getLover() != null) if (pokemob.tryToBreed() && !pokemob.getLover().isDead) return true;
        if (pokemob.getPokemonAIState(IMoveConstants.MATING)) return true;
        if (pokemob.getLover() != null) { return true; }
        if (pokemob.getSexe() == IPokemob.MALE || !pokemob.tryToBreed()) return false;
        if (pokemob.getPokemonAIState(IMoveConstants.ANGRY) || entity.getAttackTarget() != null) return false;
        return true;
    }

    public void tryFindMate()
    {
        if (pokemob.getLover() == null) return;
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING)) pokemob.setPokemonAIState(IMoveConstants.SITTING, false);

        double dist = entity.width * entity.width + pokemob.getLover().width * pokemob.getLover().width;
        dist = Math.max(dist, 1);
        entity.getNavigator().tryMoveToEntityLiving(pokemob.getLover(), 1.5);
        spawnBabyDelay++;
        pokemob.setPokemonAIState(IMoveConstants.MATING, true);
        IPokemob loverMob = CapabilityPokemob.getPokemobFor(pokemob.getLover());
        if (loverMob != null)
        {
            loverMob.setPokemonAIState(IMoveConstants.MATING, true);
            loverMob.setLover(entity);
            if (this.spawnBabyDelay >= 50)
            {
                if (pokemob.getLover() instanceof IHasMobAIStates)
                    ((IHasMobAIStates) pokemob.getLover()).setPokemonAIState(IMoveConstants.MATING, false);
                pokemob.mateWith(loverMob);
                pokemob.setPokemonAIState(IMoveConstants.MATING, false);
                this.spawnBabyDelay = 0;
                pokemob.resetLoveStatus();
                loverMob.resetLoveStatus();
            }
        }
    }
}
