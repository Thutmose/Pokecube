/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.world.World;

/** @author Manchou */
public abstract class EntitySexedPokemob extends EntityStatsPokemob
{
    /** @param par1World */
    public EntitySexedPokemob(World world)
    {
        super(world);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        // TODO possibly check the IPokemob and return and egg?
        return null;
    }

    @Override
    public boolean isInLove()
    {
        return pokemobCap.getLoveTimer() > 0 || pokemobCap.getLover() != null;
    }
}
