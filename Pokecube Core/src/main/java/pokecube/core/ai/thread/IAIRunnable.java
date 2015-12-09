package pokecube.core.ai.thread;

public interface IAIRunnable
{
    /** @return the priority of this AIRunnable. Lower numbers run first. */
    public int getPriority();

    /** Will only run an AI if it is higher priority (ie lower number) or a
     * bitwise AND of the two mutex is 0.
     * 
     * @return */
    public int getMutex();

    /** Sets the priority.
     * 
     * @param prior
     * @return */
    public IAIRunnable setPriority(int prior);

    /** Sets the mutex.
     * 
     * @param mutex
     * @return */
    public IAIRunnable setMutex(int mutex);

    /** Should the task start running. if true, will call run next.
     * 
     * @return */
    public boolean shouldRun();

    /** runs the task */
    public void run();

    /** Resets the task. */
    public void reset();
}
