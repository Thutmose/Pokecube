package pokecube.core.ai.thread.aiRunnables.idle;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/** This IAIRunnable results in the mother of an egg always staying within 4
 * blocks of it. It also prevents the mother from breeding, as well as prevents
 * the mother's breeding cooldown from dropping while an egg is being
 * guarded. */
public class AIGuardEgg extends AIBase
{
    IPokemob         pokemob;
    EntityLiving     entity;
    EntityPokemobEgg egg            = null;
    int              cooldown       = 0;
    int              spawnBabyDelay = 0;

    public AIGuardEgg(IPokemob entity2)
    {
        entity = entity2.getEntity();
        pokemob = entity2;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        // Cooldown and have egg, nothing to do here.
        if (cooldown-- > 0 || egg != null) { return; }
        // Only the female (or neutral) will guard the eggs.
        if (pokemob.getSexe() == IPokemob.MALE) return;
        cooldown = 20;
        AxisAlignedBB bb = entity.getEntityBoundingBox().grow(16, 8, 16);
        // Search for valid eggs.
        List<Entity> list2 = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, bb, new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity input)
            {
                return input instanceof EntityPokemobEgg
                        && entity.getUniqueID().equals(((EntityPokemobEgg) input).getMotherId()) && !input.isDead;
            }
        });
        // Select first egg found to guard, remove target, set not angry
        if (!list2.isEmpty())
        {
            egg = (EntityPokemobEgg) list2.get(0);
            egg.mother = pokemob;
            pokemob.getEntity().setAttackTarget(null);
            pokemob.setCombatState(CombatStates.ANGRY, false);
        }
    }

    @Override
    public void reset()
    {
        egg = null;
    }

    @Override
    public void run()
    {
        // No breeding while guarding egg.
        pokemob.resetLoveStatus();
        // If too close to egg, don't bother moving.
        if (entity.getDistanceSq(egg) < 4) return;
        // Path to the egg.
        Path path = entity.getNavigator().getPathToEntityLiving(egg);
        this.addEntityPath(entity, path, pokemob.getMovementSpeed());
    }

    @Override
    public boolean shouldRun()
    {
        // Only run if we have a live egg to watch.
        if (egg != null) return egg.isDead ? false : true;
        return false;
    }

}
