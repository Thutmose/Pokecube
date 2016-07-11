package pokecube.core.ai.thread;

import net.minecraft.world.World;

public interface ILogicRunnable
{
    /** Runs this logic for the entity. */
    void doLogic();
    
    /** Runs this logic on the server thread */
    void doServerTick(World world);
}
