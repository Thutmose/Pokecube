package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.passive.EntityAnimal;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.entity.IBreedingMob;

public class AIMate extends AIBase
{
    IBreedingMob breedingMob;
    IPokemob     pokemob;
    EntityAnimal entity;

    int cooldown = 0;

    public AIMate(EntityAnimal par1EntityAnimal)
    {
        breedingMob = (IBreedingMob) par1EntityAnimal;
        pokemob = (IPokemob) breedingMob;
        entity = (EntityAnimal) pokemob;
    }

    @Override
    public void reset()
    {
        if(cooldown > 0) return;
        cooldown = 100;
    }

    @Override
    public void run()
    {

    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null) return false;
        if (cooldown-- > 0) return false;
        if (breedingMob.getLover() != null) if (breedingMob.tryToBreed() && !breedingMob.getLover().isDead) return true;

        boolean isMating = pokemob.getPokemonAIState(IMoveConstants.MATING);
        if ((breedingMob.getLover() != null && breedingMob.getLover() instanceof IBreedingMob
                && !((IBreedingMob) breedingMob.getLover()).tryToBreed())) { return false; }

        if (isMating) return true;
        if (breedingMob.getLover() != null)
        {
            pokemob.setPokemonAIState(IMoveConstants.MATING, true);
            return true;
        }
        if (pokemob.getSexe() == IPokemob.MALE || !breedingMob.tryToBreed()) return false;
        if (breedingMob.getMalesForBreeding().size() == 0) return false;
        pokemob.setPokemonAIState(IMoveConstants.MATING, true);
        return true;
    }
}
