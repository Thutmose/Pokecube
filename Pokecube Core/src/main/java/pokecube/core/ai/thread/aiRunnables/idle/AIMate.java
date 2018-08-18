package pokecube.core.ai.thread.aiRunnables.idle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.PokeType;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

/** This IAIRunnable is responsible for most of the breeding AI for the
 * pokemobs. It finds the mates, initiates the fighting over a mate (if
 * applicable), then tells the mobs to breed if they should. */
public class AIMate extends AIBase
{
    final IPokemob     pokemob;
    final EntityLiving entity;
    int                cooldown       = 0;
    int                spawnBabyDelay = 0;

    public AIMate(IPokemob entity2)
    {
        entity = entity2.getEntity();
        pokemob = entity2;
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
        if (pokemob.getGeneralState(GeneralStates.MATING)
                && (pokemob.getLover() == null || pokemob.getLover().isDead || loverMob != pokemob))
        {
            pokemob.setGeneralState(GeneralStates.MATING, false);
        }
        if (cooldown-- > 0) { return; }
        super.doMainThreadTick(world);
        if (pokemob.getLoveTimer() > 0 && pokemob.getLover() == null)
        {
            findLover();
        }
        if (pokemob.getLover() == null && pokemob.getMalesForBreeding().isEmpty()) return;
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
            if (pokemob.getMalesForBreeding().size() == 1 && pokemob.getLover() == null
                    && pokemob.getMalesForBreeding().get(0) instanceof IPokemob)
                pokemob.setLover(((IPokemob) pokemob.getMalesForBreeding().get(0)).getEntity());
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
                    mob0.setCombatState(CombatStates.MATEFIGHT, true);
                    mob1.setCombatState(CombatStates.MATEFIGHT, true);
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
        if (pokemob.getLover() != null) { return pokemob.getLover(); }
        boolean transforms = false;
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        }
        if (!pokemob.getPokedexEntry().breeds && !transforms) return null;
        if (pokemob.isType(PokeType.getType("ghost")) && !pokemob.getGeneralState(GeneralStates.TAMED)) return null;
        if ((pokemob.getSexe() == IPokemob.MALE && !transforms)
                || pokemob.getMalesForBreeding().size() > 0) { return null; }

        float searchingLoveDist = 5F;
        AxisAlignedBB bb = makeBox(searchingLoveDist, searchingLoveDist, searchingLoveDist,
                entity.getEntityBoundingBox());
        List<Entity> targetMates = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb,
                new Predicate<Entity>()
                {
                    @Override
                    public boolean apply(Entity input)
                    {
                        return input instanceof EntityAnimal && pokemob.canMate((EntityAnimal) input);
                    }
                });
        bb = makeBox(PokecubeMod.core.getConfig().maxSpawnRadius, searchingLoveDist,
                PokecubeMod.core.getConfig().maxSpawnRadius, entity.getEntityBoundingBox());
        List<Entity> otherMobs = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity input)
            {
                return input instanceof EntityAnimal && CapabilityPokemob.getPokemobFor(input) != null;
            }
        });
        float multiplier = pokemob.isPlayerOwned() ? PokecubeMod.core.getConfig().mateDensityPlayer
                : PokecubeMod.core.getConfig().mateDensityWild;
        if (otherMobs.size() >= PokecubeMod.core.getConfig().mobSpawnNumber * multiplier)
        {
            pokemob.resetLoveStatus();
            return null;
        }
        boolean gendered = pokemob.getSexe() == IPokemob.MALE || pokemob.getSexe() == IPokemob.FEMALE;
        for (int i = 0; i < targetMates.size(); i++)
        {
            IPokemob otherPokemob = CapabilityPokemob.getPokemobFor(targetMates.get(i));
            EntityAnimal animal = (EntityAnimal) targetMates.get(i);
            if (gendered && !transforms && otherPokemob.getSexe() == pokemob.getSexe()) continue;
            if (otherPokemob == this.pokemob || otherPokemob.getGeneralState(GeneralStates.TAMED) != pokemob
                    .getGeneralState(GeneralStates.TAMED) || !otherPokemob.getPokedexEntry().breeds)
                continue;
            boolean otherTransforms = false;
            for (String s : otherPokemob.getMoves())
            {
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
            }

            if (transforms && otherTransforms || !(otherPokemob.getEntity() instanceof EntityAnimal)) continue;

            boolean validMate = pokemob.canMate((EntityAnimal) otherPokemob.getEntity());
            if (!validMate || entity.getDistanceSq(otherPokemob.getEntity()) > searchingLoveDist * searchingLoveDist)
                continue;
            if (!Vector3.isVisibleEntityFromEntity(entity, otherPokemob.getEntity())
                    || otherPokemob.getCombatState(CombatStates.ANGRY))
                continue;

            if (otherPokemob != this && animal.getHealth() > animal.getMaxHealth() / 1.5f)
            {
                if (!pokemob.getMalesForBreeding().contains(otherPokemob))
                {
                    otherPokemob.setLover(entity);
                    if (transforms) pokemob.setLover(animal);
                    pokemob.getMalesForBreeding().add(otherPokemob);
                    otherPokemob.setLoveTimer(200);
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
        if (!pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (cooldown > 0) { return false; }
        if (pokemob.getLover() != null) if (pokemob.tryToBreed() && !pokemob.getLover().isDead) return true;
        if (pokemob.getGeneralState(GeneralStates.MATING)) return true;
        if (pokemob.getLover() != null) { return true; }
        if (pokemob.getSexe() == IPokemob.MALE || !pokemob.tryToBreed()) return false;
        if (pokemob.getCombatState(CombatStates.ANGRY) || entity.getAttackTarget() != null) return false;
        return true;
    }

    public void tryFindMate()
    {
        if (pokemob.getLover() == null) return;
        if (pokemob.getLogicState(LogicStates.SITTING)) pokemob.setLogicState(LogicStates.SITTING, false);

        double dist = entity.width * entity.width + pokemob.getLover().width * pokemob.getLover().width;
        dist = Math.max(dist, 1);
        entity.getNavigator().tryMoveToEntityLiving(pokemob.getLover(), pokemob.getMovementSpeed());
        spawnBabyDelay++;
        pokemob.setGeneralState(GeneralStates.MATING, true);
        IPokemob loverMob = CapabilityPokemob.getPokemobFor(pokemob.getLover());
        if (loverMob != null)
        {
            loverMob.setGeneralState(GeneralStates.MATING, true);
            loverMob.setLover(entity);
            if (this.spawnBabyDelay >= 50)
            {
                if (pokemob.getLover() instanceof IHasMobAIStates)
                    ((IHasMobAIStates) pokemob.getLover()).setGeneralState(GeneralStates.MATING, false);
                pokemob.mateWith(loverMob);
                pokemob.setGeneralState(GeneralStates.MATING, false);
                this.spawnBabyDelay = 0;
                pokemob.resetLoveStatus();
                loverMob.resetLoveStatus();
            }
        }
    }

    private AxisAlignedBB makeBox(double dx, double dy, double dz, AxisAlignedBB centre)
    {
        return centre.grow(dx, dy, dz);
    }
}
