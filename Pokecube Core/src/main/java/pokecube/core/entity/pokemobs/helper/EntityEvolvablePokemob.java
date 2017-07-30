/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    public EntityEvolvablePokemob(World world)
    {
        super(world);
    }

    @Override
    public void setEvolutionStack(ItemStack stack)
    {
        pokemobCap.setEvolutionStack(stack);
    }

    @Override
    public ItemStack getEvolutionStack()
    {
        return pokemobCap.getEvolutionStack();
    }

    /** @return the evolutionTicks */
    @Override
    public int getEvolutionTicks()
    {
        return pokemobCap.getEvolutionTicks();
    }

    /** Returns whether the entity is in a server world */
    @Override
    public boolean isServerWorld()
    {
        return worldObj != null && super.isServerWorld();
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.ticksExisted > 100) forceSpawn = false;
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /** @param evolutionTicks
     *            the evolutionTicks to set */
    @Override
    public void setEvolutionTicks(int evolutionTicks)
    {
        pokemobCap.setEvolutionTicks(evolutionTicks);
    }
}
