package pokecube.core.ai.thread;

import net.minecraft.world.World;

public interface IAIRunnable
{
    /** @return the priority of this AIRunnable. Lower numbers run first. */
    int getPriority();

    /** Will only run an AI if it is higher priority (ie lower number) or a
     * bitwise AND of the two mutex is 0.
     * 
     * @return */
    int getMutex();

    /** Sets the priority.
     * 
     * @param prior
     * @return */
    IAIRunnable setPriority(int prior);

    /** Sets the mutex.
     * 
     * @param mutex
     * @return */
    IAIRunnable setMutex(int mutex);

    /** Should the task start running. if true, will call run next.
     * 
     * @return */
    boolean shouldRun();

    /** runs the task */
    void run();

    /** Resets the task. */
    void reset();

    /** called to execute the needed non-threadsafe tasks on the main thread. */
    void doMainThreadTick(World world);
}
