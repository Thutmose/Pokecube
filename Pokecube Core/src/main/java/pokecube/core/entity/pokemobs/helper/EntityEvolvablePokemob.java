/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    private ItemStack stack = CompatWrapper.nullStack;

    public EntityEvolvablePokemob(World world)
    {
        super(world);
    }

    @Override
    public void setEvolutionStack(ItemStack stack)
    {
        this.stack = stack;
    }

    @Override
    public ItemStack getEvolutionStack()
    {
        return stack;
    }

    /** @return the evolutionTicks */
    @Override
    public int getEvolutionTicks()
    {
        return dataManager.get(EVOLTICKDW);
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

        int num = getEvolutionTicks();
        if (num > 0)
        {
            setEvolutionTicks(getEvolutionTicks() - 1);
        }
        boolean evolving = this.getPokemonAIState(EVOLVING);
        if (num <= 0 && evolving)
        {
            this.setPokemonAIState(EVOLVING, false);
        }
        if (num <= 50 && evolving)
        {
            this.evolve(false, false, stack);
            this.setPokemonAIState(EVOLVING, false);
        }
        if (PokecubeSerializer.getInstance().getPokemob(getPokemonUID()) == null)
            PokecubeSerializer.getInstance().addPokemob(this);
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (Tools.isSameStack(getHeldItemMainhand(), PokecubeItems.getStack("everstone")))
        {
            setPokemonAIState(TRADED, false);
        }
    }

    /** @param evolutionTicks
     *            the evolutionTicks to set */
    @Override
    public void setEvolutionTicks(int evolutionTicks)
    {
        dataManager.set(EVOLTICKDW, new Integer(evolutionTicks));
    }
}
