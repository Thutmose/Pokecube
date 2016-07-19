package pokecube.core.ai.thread.aiRunnables;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.IBreedingMob;

public class AIGuardEgg extends AIBase
{
    IBreedingMob     breedingMob;
    IPokemob         pokemob;
    EntityAnimal     entity;
    EntityPokemobEgg egg            = null;
    int              cooldown       = 0;
    int              spawnBabyDelay = 0;

    public AIGuardEgg(EntityAnimal par1EntityAnimal)
    {
        breedingMob = (IBreedingMob) par1EntityAnimal;
        pokemob = (IPokemob) breedingMob;
        entity = (EntityAnimal) pokemob;
    }

    @Override
    public void reset()
    {
        egg = null;
    }

    @Override
    public void run()
    {
        breedingMob.setLoveTimer(0);
        if (entity.getDistanceSqToEntity(egg) < 4) return;
        Path path = entity.getNavigator().getPathToEntityLiving(egg);
        this.addEntityPath(entity, path, pokemob.getMovementSpeed());
    }

    @Override
    public boolean shouldRun()
    {
        if (egg != null) return egg.isDead ? false : true;
        if (pokemob.getSexe() == IPokemob.MALE) return false;
        AxisAlignedBB bb = entity.getEntityBoundingBox().expand(16, 8, 16);
        List<Entity> list2 = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity input)
            {
                return input instanceof EntityPokemobEgg
                        && entity.getUniqueID().equals(((EntityPokemobEgg) input).getMotherId()) && !input.isDead;
            }
        });
        if (!list2.isEmpty())
        {
            egg = (EntityPokemobEgg) list2.get(0);
            return true;
        }
        return false;
    }

}
