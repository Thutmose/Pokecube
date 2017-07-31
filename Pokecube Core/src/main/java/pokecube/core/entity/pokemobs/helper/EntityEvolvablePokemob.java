/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.World;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    public EntityEvolvablePokemob(World world)
    {
        super(world);
    }

    /** Returns whether the entity is in a server world */
    @Override
    public boolean isServerWorld()
    {
        return getEntityWorld() != null && super.isServerWorld();
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.ticksExisted > 100) forceSpawn = false;
        super.onLivingUpdate();
    }
}
