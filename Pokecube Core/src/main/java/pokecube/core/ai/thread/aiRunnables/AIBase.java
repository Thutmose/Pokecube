package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockAccess;
import pokecube.core.ai.thread.IAIRunnable;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.interfaces.IPokemob;

public abstract class AIBase implements IAIRunnable
{
	IBlockAccess				world;
	int priority = 0;
	int mutex = 0;
	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public int getMutex()
	{
		return mutex;
	}

	@Override
	public IAIRunnable setPriority(int prior)
	{
		priority = prior;
		return this;
	}

	@Override
	public IAIRunnable setMutex(int mutex)
	{
		this.mutex = mutex;
		return this;
	}
	
	protected void setPokemobAIState(IPokemob pokemob, int state, boolean value)
	{
		PokemobAIThread.addStateInfo(pokemob.getPokemonUID(), state, value);
	}
	
	protected void addTargetInfo(Entity attacker, Entity target)
	{
		int targetId = target==null? -1: target.getEntityId();
		PokemobAIThread.addTargetInfo(attacker.getEntityId(), targetId, attacker.dimension);
	}
	
}
