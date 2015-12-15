package pokecube.core.ai.thread;

public interface ILogicRunnable
{
    /** Runs this logic for the entity. */
    void doLogic();
    
    /** Runs this logic on the server thread */
    void doServerTick();
}
