package pokecube.core.network;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import pokecube.core.interfaces.IEntityProvider;

public class EntityProvider implements IEntityProvider
{
    final EntityProvider defaults;

    public EntityProvider(EntityProvider defaults)
    {
        this.defaults = defaults;
    }

    @Override
    public Entity getEntity(World world, int id, boolean expectsPokemob)
    {
        Entity ret = world.getEntityByID(id);
        if (ret == null && defaults != null) return defaults.getEntity(world, id, expectsPokemob);
        return ret;
    }

}
