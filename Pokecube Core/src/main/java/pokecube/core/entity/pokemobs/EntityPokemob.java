/**
 *
 */
package pokecube.core.entity.pokemobs;

import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;

public class EntityPokemob extends EntityPokemobBase
{
    public EntityPokemob(World world)
    {
        super(world);
        init(pokemobCap.getPokedexNb());
    }
}
