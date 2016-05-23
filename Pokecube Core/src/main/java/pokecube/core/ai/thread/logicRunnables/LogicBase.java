package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.IBlockAccess;
import pokecube.core.ai.thread.ILogicRunnable;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public abstract class LogicBase implements ILogicRunnable, IMoveConstants
{
    protected final IPokemob     pokemob;
    protected final EntityLiving entity;
    protected IBlockAccess       world;

    public LogicBase(IPokemob pokemob_)
    {
        pokemob = pokemob_;
        entity = (EntityLiving) pokemob;
    }

    @Override
    public void doServerTick()
    {
    }

    protected void setPokemobAIState(int state, boolean value)
    {
        pokemob.setPokemonAIState(state, value);// dataManager says it is
                                                // threadsafe, if this doesn't
                                                // work, will use what AIBase
                                                // uses.
    }
}
