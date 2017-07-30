/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import thut.api.entity.IBreedingMob;

/** @author Manchou */
public abstract class EntitySexedPokemob extends EntityStatsPokemob
{
    protected Entity               egg   = null;
    private Entity                 lover;
    protected int                  loveTimer;
    protected Vector<IBreedingMob> males = new Vector<>();

    /** @param par1World */
    public EntitySexedPokemob(World world)
    {
        super(world);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return this.spawnBabyAnimal((EntityAnimal) var1);
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        resetInLove();
    }

    @Override
    public boolean isInLove()
    {
        return loveTimer > 0 || lover != null;
    }

    @Override
    public void resetInLove()
    {
    }

    // @Override
    public EntityAnimal spawnBabyAnimal(EntityAnimal entityanimal)
    {
        return null;
    }
}
