package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityJumpHelper;
import pokecube.core.entity.pokemobs.helper.EntityAiPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

/** Overriden to allow setting the pokemob's specific jumping state, and calling
 * the custom jump implementation. */
public class PokemobJumpHelper extends EntityJumpHelper
{

    private final IPokemob        pokemob;
    private final EntityAiPokemob living;

    public PokemobJumpHelper(EntityLiving entityIn)
    {
        super(entityIn);
        pokemob = CapabilityPokemob.getPokemobFor(entityIn);
        living = (EntityAiPokemob) entityIn;
    }

    /** Called to actually make the entity jump if isJumping is true. */
    @Override
    public void doJump()
    {
        super.doJump();
        if (pokemob.getLogicState(LogicStates.JUMPING))
        {
            living.jump();
            pokemob.setLogicState(LogicStates.JUMPING, false);
        }
    }

}
