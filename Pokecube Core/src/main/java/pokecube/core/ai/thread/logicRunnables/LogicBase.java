package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.ILogicRunnable;

public abstract class LogicBase implements ILogicRunnable, IMoveConstants
{
    protected final IPokemob     pokemob;
    protected final EntityLiving entity;
    protected IBlockAccess       world;

    public LogicBase(IPokemob pokemob_)
    {
        pokemob = pokemob_;
        entity = pokemob.getEntity();
    }

    @Override
    public void doServerTick(World world)
    {
    }
}
