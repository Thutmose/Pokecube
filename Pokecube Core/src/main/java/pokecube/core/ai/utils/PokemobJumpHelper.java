package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityJumpHelper;
import pokecube.core.entity.pokemobs.helper.EntityAiPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public class PokemobJumpHelper extends EntityJumpHelper
{

    private final IPokemob        pokemob;
    private final EntityAiPokemob living;

    public PokemobJumpHelper(EntityLiving entityIn)
    {
        super(entityIn);
        pokemob = (IPokemob) entityIn;
        living = (EntityAiPokemob) entityIn;
    }

    /** Called to actually make the entity jump if isJumping is true. */
    @Override
    public void doJump()
    {
        super.doJump();
        if (pokemob.getPokemonAIState(IMoveConstants.JUMPING))
        {
            living.jump();
            pokemob.setPokemonAIState(IMoveConstants.JUMPING, false);
        }
    }

}
