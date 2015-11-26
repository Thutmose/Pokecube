package pokecube.core.ai.thread;

public interface IAIRunnable
{
	public int getPriority();
	public int getMutex();
	
	public IAIRunnable setPriority(int prior);
	public IAIRunnable setMutex(int mutex);
	
	public boolean shouldRun();
	public void run();
	public void reset();
	
	public void runServerThread();
}
