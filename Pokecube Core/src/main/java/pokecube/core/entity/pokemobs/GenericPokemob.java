package pokecube.core.entity.pokemobs;

import net.minecraft.world.World;

/** This class is copied by ByteClassLoader to make seperate classes for each
 * pokemob */
public class GenericPokemob extends EntityPokemob
{
    public static int nb = 0;

    public GenericPokemob(World world)
    {
        super(world);
    }

    @Override
    public Integer getPokedexNb()
    {
        if (nb == 0)
        {
            try
            {
                nb = (int) getClass().getField("nb").get(null);
            }
            catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
            {
                e.printStackTrace();
            }
        }
        return nb;
    }
}
