package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.interfaces.IPokemob;

/** This logic deals with pokemobs that have situational forme changes which are not based on abilities. */
public class LogicFormes extends LogicBase
{
    final EntityPokemobBase pokemob;

    public LogicFormes(IPokemob pokemob_)
    {
        super(pokemob_);
        pokemob = (EntityPokemobBase) pokemob_;
    }

    @Override
    public void doLogic()
    {
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        
    }

}
